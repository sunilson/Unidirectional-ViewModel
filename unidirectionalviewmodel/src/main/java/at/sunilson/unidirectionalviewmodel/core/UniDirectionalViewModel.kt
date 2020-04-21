package at.sunilson.unidirectionalviewmodel.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

abstract class UniDirectionalViewModel<State : Any, Event>(initialState: State) : ViewModel() {

    /**
     * The state of this ViewModel. Observe to get changes or use [getState] to get a snapshot (don't use [LiveData.getValue])
     */
    private val _state = MutableLiveData(initialState)
    val state: LiveData<State> get() = _state

    /**
     * Channel used for one-time-events
     */
    private val eventsChannel = BroadcastChannel<Event>(BUFFERED)

    /**
     * Channel used to queue all actions so they are execute sequentially
     */
    private val setStateChannel = Channel<SetState<State>>(Channel.UNLIMITED)
    private val getStateChannel = Channel<GetState<State>>(Channel.UNLIMITED)

    /**
     * Flow that emits one-time-events emitted by the ViewModel
     */
    val events: Flow<Event> = eventsChannel.asFlow()

    /**
     * A list of [MiddleWare] that will be called sequentially on every state update
     */
    private val middleWares: MutableList<MiddleWare<State>> = mutableListOf()

    init {
        runStateLoop()
    }

    private fun runStateLoop() {
        // Handle state updates/requests from get and set channels
        viewModelScope.launch {
            while (isActive) {
                // This allows us to recieve on multiple channels. First one has priority if not empty
                select<Unit> {
                    // Handle setStates first
                    setStateChannel.onReceive {
                        val currentState = state.value!!
                        val newState = runMiddleWares(currentState.it())
                        if (currentState != newState) {
                            _state.value = newState
                        }
                    }

                    // Handle getStates only after all setStates are done
                    getStateChannel.onReceive { it(state.value!!) }
                }
            }
        }
    }

    /**
     * @param block In this block the current state can be accessed
     */
    protected fun getState(block: GetState<State>) {
        getStateChannel.offer(block)
    }

    /**
     * @param block Use this block to manipulate the current state by copying and returning it
     */
    protected fun setState(block: SetState<State>) {
        setStateChannel.offer(block)
    }

    /**
     * Emits a one-time [Event] to all subscribers of the [event] flow
     */
    protected fun sendEvent(event: Event) {
        eventsChannel.offer(event)
    }

    fun registerMiddleWare(middleWare: MiddleWare<State>) = middleWares.add(middleWare)

    fun registerMiddleWares(builder: MiddleWareBuilder.() -> Unit) =
        MiddleWareBuilder().apply(builder)

    private fun runMiddleWares(state: State) =
        middleWares.fold(state, { acc, middleWare -> middleWare(acc) })

    inner class MiddleWareBuilder {
        fun middleWare(block: MiddleWare<State>) {
            middleWares.add(block)
        }
    }
}
