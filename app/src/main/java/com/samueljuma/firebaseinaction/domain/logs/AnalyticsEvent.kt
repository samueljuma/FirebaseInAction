package com.samueljuma.firebaseinaction.domain.logs

sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any>
        get() = emptyMap()

    // Auth events
    data class SignInCompleted(
        val method: SignInMethod
    ) : AnalyticsEvent {
        override val name = "sign_in_completed"
        override val params = mapOf("method" to method.value)
    }

    data class SignUpCompleted(
        val method: SignInMethod
    ) : AnalyticsEvent {
        override val name = "sign_up_completed"
        override val params = mapOf("method" to method.value)
    }

    // Note funnel events
    data object NoteCreateStarted : AnalyticsEvent {
        override val name = "note_create_started"
    }

    data class NoteSaved(
        val hasImage: Boolean
    ) : AnalyticsEvent {
        override val name = "note_saved"
        override val params = mapOf("has_image" to hasImage)
    }

    data class NoteImageAdded(
        val source: ImageSource
    ) : AnalyticsEvent {
        override val name = "note_image_added"
        override val params = mapOf("source" to source.value)
    }

    data class NoteDeleted(
        val hadImage: Boolean,
        val noteAgeDays: Long
    ) : AnalyticsEvent {
        override val name = "note_deleted"
        override val params = mapOf(
            "had_image" to hadImage,
            "note_age_days" to noteAgeDays
        )
    }

    // Supporting enums
    enum class SignInMethod(val value: String) {
        EMAIL("email"),
        GOOGLE("google")
    }

    enum class ImageSource(val value: String) {
        CREATE("create"),
        EDIT("edit")
    }
}
