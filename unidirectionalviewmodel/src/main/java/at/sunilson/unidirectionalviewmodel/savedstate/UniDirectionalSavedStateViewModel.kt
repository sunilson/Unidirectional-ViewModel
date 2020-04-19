package at.sunilson.unidirectionalviewmodel.savedstate

import androidx.lifecycle.SavedStateHandle
import at.sunilson.unidirectionalviewmodel.UniDirectionalViewModel

abstract class UniDirectionalSavedStateViewModel<State : Any, Event>(
    private val initialState: State,
    private val savedStateHandle: SavedStateHandle
) : UniDirectionalViewModel<State, Event>(initialState) {

    init {
        setState { this.initializeStateFromSavedState(savedStateHandle) }
        registerMiddleWare { savedStateHandle.updateStateHandle(it) }
    }

    abstract fun State.initializeStateFromSavedState(savedStateHandle: SavedStateHandle): State
    abstract fun SavedStateHandle.updateStateHandle(state: State)
}
