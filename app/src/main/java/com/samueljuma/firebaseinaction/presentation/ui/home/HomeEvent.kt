import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface HomeEvent {
    data object NavigateToLogin : HomeEvent
    data object NavigateToCreateNote : HomeEvent
    data class NavigateToNoteDetail(val noteId: String) : HomeEvent
    data class ShowSnackbar(val message: UiText) : HomeEvent
}