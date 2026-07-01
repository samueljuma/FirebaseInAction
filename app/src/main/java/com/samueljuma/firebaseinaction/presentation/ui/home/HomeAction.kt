import com.samueljuma.firebaseinaction.domain.notes.model.Note
sealed interface HomeAction {
    data object OnSignOutClicked : HomeAction
    data class OnDeleteNote(val note: Note) : HomeAction
    data class OnPinNote(val note: Note) : HomeAction
    data object OnCreateNoteClicked : HomeAction
    data class OnNoteClicked(val noteId: String) : HomeAction
    data object OnNotificationsClicked : HomeAction
}