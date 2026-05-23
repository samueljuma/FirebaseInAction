package com.samueljuma.firebaseinaction.presentation.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Action, Event>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // SharedFlow with UNLIMITED buffer — events are never dropped on rotation.
    // Edge case: if an event fires before the screen's LaunchedEffect collector
    // attaches (very early loads), it can still be missed regardless of buffer.
    // For those cases, fold the event into state as a nullable field instead
    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = Channel.UNLIMITED
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()

    abstract fun onAction(action: Action)

    protected fun updateState(transform: State.() -> State) {
        _state.update { it.transform() }
    }

    protected fun emitEvent(event: Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}