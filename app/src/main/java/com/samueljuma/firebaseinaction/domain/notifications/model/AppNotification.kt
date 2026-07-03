package com.samueljuma.firebaseinaction.domain.notifications.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val receivedAt: Long = 0L,
    val read: Boolean = false,
    // Where tapping this notification should navigate, e.g. "notey://note?noteId=...".
    // Null falls back to the generic notifications inbox.
    val deepLink: String? = null,
    // Transient — never persisted (Room/Firestore rows are always DATA by construction,
    // since only server-authored, data-only pushes are ever written to the inbox). Only
    // meaningful for the in-memory object built at receive-time, to pick display/tap behavior.
    val kind: FcmPayloadKind = FcmPayloadKind.DATA
)

/**
 * Which FCM payload shape produced this notification.
 *
 * - [DATA]: a data-only message (no `notification` block) — what our own server (the reminder
 *   Cloud Function) always sends. Always reaches `onMessageReceived`, foreground or background.
 * - [DISPLAY]: a message carrying an FCM `notification` payload, e.g. a Firebase Console
 *   campaign. Android auto-displays these via the system tray and never calls
 *   `onMessageReceived` while the app is backgrounded — so this value only ever matters for the
 *   foreground path.
 */
enum class FcmPayloadKind { DATA, DISPLAY }