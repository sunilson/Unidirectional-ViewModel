package at.sunilson.unidirectionalviewmodel.extensions

import androidx.lifecycle.Observer
import at.sunilson.unidirectionalviewmodel.core.UniDirectionalViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

fun <State : Any, Event> UniDirectionalViewModel<State, Event>.registerPureMiddleWare(block: (State) -> Unit) {
    registerMiddleWare {
        block(it)
        it
    }
}

val <State : Any, Event> UniDirectionalViewModel<State, Event>.stateFlow: Flow<State>
    get() = channelFlow {
        val observer = Observer<State> { t -> offer(t) }
        state.observeForever(observer)
        awaitClose { state.removeObserver(observer) }
    }