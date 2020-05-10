package at.sunilson.unidirectionalviewmodel.extensions

import at.sunilson.unidirectionalviewmodel.core.UniDirectionalViewModel

fun <State : Any, Event> UniDirectionalViewModel<State, Event>.registerPureMiddleWare(block: (State) -> Unit) {
    registerMiddleWare {
        block(it)
        it
    }
}