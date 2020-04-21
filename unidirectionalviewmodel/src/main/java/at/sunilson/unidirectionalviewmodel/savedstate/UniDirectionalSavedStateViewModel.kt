package at.sunilson.unidirectionalviewmodel.savedstate

import androidx.lifecycle.SavedStateHandle
import at.sunilson.unidirectionalviewmodel.core.UniDirectionalViewModel
import at.sunilson.unidirectionalviewmodel.extensions.registerPureMiddleWare

abstract class UniDirectionalSavedStateViewModel<State : Any, Event>(
    private val initialState: State,
    private val savedStateHandle: SavedStateHandle
) : UniDirectionalViewModel<State, Event>(initialState) {

    init {
        registerPureMiddleWare { savedStateHandle.updateStateHandle(it) }
        setState { this.initializeStateFromSavedState(savedStateHandle) }
    }

    abstract fun State.initializeStateFromSavedState(savedStateHandle: SavedStateHandle): State
    abstract fun SavedStateHandle.updateStateHandle(state: State)
}
