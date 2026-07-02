"""Cloud Functions for Notey.

Reminder producer: a scheduled scan finds notes with a due, unfired reminder and
delivers a data-only FCM push for each, backed by a server-authored Firestore
notification doc. See notes/firebase_cloud_functions.md for the full design.
"""
from datetime import datetime, timezone

from firebase_admin import initialize_app, firestore, messaging
from firebase_functions import scheduler_fn, logger

initialize_app()

REMINDER_TITLE = "Reminder"


def _now_millis() -> int:
    return int(datetime.now(timezone.utc).timestamp() * 1000)


@scheduler_fn.on_schedule(schedule="* * * * *")
def check_due_reminders(event: scheduler_fn.ScheduledEvent) -> None:
    """Runs every minute. Finds notes whose reminder is due and not yet fired,
    across every user, and delivers each one exactly once.
    """
    db = firestore.client()
    now = _now_millis()

    due_notes = (
        db.collection_group("notes")
        .where(filter=firestore.FieldFilter("reminderFiredAt", "==", None))
        .where(filter=firestore.FieldFilter("reminderAt", "<=", now))
        .stream()
    )

    processed = 0
    for note_doc in due_notes:
        processed += 1
        _deliver_reminder(db, note_doc)

    logger.info(f"check_due_reminders: processed {processed} due reminder(s)")


def _deliver_reminder(db, note_doc) -> None:
    note = note_doc.to_dict()
    note_id = note_doc.id
    user_id = note.get("userId")
    reminder_at = note.get("reminderAt")

    if not user_id or reminder_at is None:
        logger.warn(f"Skipping note {note_id}: missing userId/reminderAt")
        return

    # Mark fired *before* attempting delivery below. If delivery then fails (bad
    # token, transient error), we don't want next minute's scan to pick this note
    # back up and retry forever — "attempt once" semantics for a reminder.
    note_doc.reference.update({"reminderFiredAt": reminder_at})

    title = note.get("title") or "Untitled note"
    deep_link = f"notey://note?noteId={note_id}"
    # Deterministic id (not auto-generated) so re-processing the same note/reminder
    # pair — e.g. a retried function invocation — can't create a duplicate inbox entry.
    notification_id = f"{note_id}_{reminder_at}"

    try:
        deliver_notification(
            db,
            user_id=user_id,
            notification_id=notification_id,
            title=REMINDER_TITLE,
            body=title,
            deep_link=deep_link,
        )
    except Exception as e:
        logger.error(f"Failed to deliver reminder for note {note_id}: {e}")


def deliver_notification(
    db, *, user_id: str, notification_id: str, title: str, body: str, deep_link: str
) -> None:
    """Writes the notification's Firestore inbox doc, then sends a data-only FCM
    push carrying the same id and deep link.

    This is the one seam any future server-side notification producer (not just
    reminders) would call through — keeping Firestore (the source of truth) and
    the push (a delivery signal) written together, consistently.
    """
    db.document(f"users/{user_id}/notifications/{notification_id}").set(
        {
            "id": notification_id,
            "title": title,
            "body": body,
            "receivedAt": _now_millis(),
            "read": False,
            "deepLink": deep_link,
        }
    )

    user_doc = db.document(f"users/{user_id}").get()
    token = user_doc.to_dict().get("fcmToken") if user_doc.exists else None

    if not token:
        logger.warn(f"No fcmToken for user {user_id} — inbox doc written, push skipped")
        return

    messaging.send(
        messaging.Message(
            # Data-only: no `notification=` field. That's what guarantees
            # onMessageReceived fires on the client even while backgrounded —
            # a `notification` payload would route straight to the system tray
            # and skip our display/persistence code entirely.
            data={
                "notificationId": notification_id,
                "title": title,
                "body": body,
                "deepLink": deep_link,
            },
            token=token,
            android=messaging.AndroidConfig(priority="high"),
        )
    )
