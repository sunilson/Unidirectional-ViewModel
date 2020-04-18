package at.sunilson.unidirectionalviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

internal typealias GetState<State> = (State) -> Unit
internal typealias SetState<State> = State.() -> State
internal typealias MiddleWare<State> = (State) -> Unit

abstract class UniDirectionalViewModel<State : Any, Event>(initialState: State) : ViewModel() {

    /**
     * The state of this ViewModel. Observe to get changes or use [getState] to get a snapshot (don't use [LiveData.getValue])
     */
    private val _state = MutableLiveData(initialState)
    val state: LiveData<State> get() = _state
    val stateFlow: Flow<State>
        get() = channelFlow {
            val observer = Observer<State> { t -> offer(t) }
            _state.observeForever(observer)
            awaitClose { _state.removeObserver(observer) }
        }

    /**
     * You probably don't want to use this value is not updated in sync,
     * only use the state available to you in [setState] or [getState] or subscribe to [state]
     */
    var currentState: State = initialState
        private set

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
                        val newState = currentState.it()
                        if (currentState != newState) {
                            runMiddleWares(newState)
                            currentState = newState
                            _state.postValue(newState)
                        }
                    }

                    // Handle getStates only after all setStates are done
                    getStateChannel.onReceive { it(currentState) }
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
     * Adds the given [middleWare] to a list. On every state update the middleWares will be notified
     * sequentially about the new state and can perform side-effects
     */
    fun registerMiddleWare(middleWare: MiddleWare<State>) {
        middleWares.add(middleWare)
    }

    private fun runMiddleWares(state: State) {
        middleWares.forEach { it(state) }
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
}
