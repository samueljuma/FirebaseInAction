package com.samueljuma.firebaseinaction.core.notifications

/**
 * Single source of truth for the app's note deep link (used by reminder pushes to open the
 * specific note directly, rather than the generic notifications inbox).
 *
 * [PATTERN] (with the `{noteId}` placeholder) is registered on the NoteDetail destination via
 * `navDeepLink`, and must stay in sync with the `<intent-filter>` for MainActivity in
 * AndroidManifest.xml.
 *
 * [uri] builds the concrete link a reminder push's PendingIntent fires when tapped.
 */
object NoteDeepLinks {
    const val SCHEME = "notey"
    const val HOST = "note"

    const val PATTERN = "$SCHEME://$HOST?noteId={noteId}"

    fun uri(noteId: String) = "$SCHEME://$HOST?noteId=$noteId"
}
