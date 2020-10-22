package at.sunilson.unidirectionalviewmodel.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

@ExperimentalCoroutinesApi
abstract class UniDirectionalViewModel<State : Any, Event>(initialState: State) : ViewModel() {

    /**
     * The state of this ViewModel. Observe to get changes or use [getState] to get a snapshot
     */
    private val _state = MutableStateFlow(initialState)
    val state: Flow<State>
        get() = _state.distinctUntilChanged { old, new -> stateDiff(old, new) }

    /**
     * Channel used for one-time-events
     */
    private val eventsChannel = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    /**
     * Channel used to queue all actions so they are execute sequentially
     */
    private val setStateChannel = Channel<SetState<State>>(Channel.UNLIMITED)
    private val getStateChannel = Channel<GetState<State>>(Channel.UNLIMITED)

    /**
     * Flow that emits one-time-events emitted by the ViewModel
     */
    val events: Flow<Event>
        get() = eventsChannel.asSharedFlow()

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
                    setStateChannel.onReceive { _state.value = runMiddleWares(_state.value.it()) }

                    // Handle getStates only after all setStates are done
                    getStateChannel.onReceive { it(_state.value) }
                }
            }
        }
    }

    /**
     * Override this method to have control over when a new state is different to the old one. Only
     * when this returns true will a new state be emitted. Default implementation: `oldState == newState`
     */
    protected open fun stateDiff(oldState: State, newState: State): Boolean {
        return oldState == newState
    }

    /**
     * Use this method to safely access the state. All [setState] calls that are queued will be
     * executed before this [block]
     *
     * @param block In this block the current state can be accessed
     */
    fun getState(block: GetState<State>) {
        getStateChannel.offer(block)
    }

    /**
     * Use this method to update the state of the ViewModel. All [setState] blocks will be queued
     * and executed before any [getState] block will be executed.
     *
     * @param block Use this block to manipulate the current state by copying and returning it
     */
    protected fun setState(block: SetState<State>) {
        setStateChannel.offer(block)
    }

    /**
     * Emits a one-time [Event] to all subscribers of the [event] flow
     */
    protected fun sendEvent(event: Event) {
        eventsChannel.tryEmit(event)
    }

    /**
     * Use this method to add a [MiddleWare] to this ViewModel. All [MiddleWare] will be called when
     * the state changes, in the order of their addition.
     */
    fun registerMiddleWare(middleWare: MiddleWare<State>) = middleWares.add(middleWare)

    /**
     * Builder method to add multiple [MiddleWare] at once
     */
    fun registerMiddleWares(builder: MiddleWareBuilder.() -> Unit) {
        middleWares.addAll(MiddleWareBuilder().apply(builder).build())
    }

    private fun runMiddleWares(state: State) =
        middleWares.fold(state, { acc, middleWare -> middleWare(acc) })

    inner class MiddleWareBuilder {
        private val middleWares = mutableListOf<MiddleWare<State>>()

        fun middleWare(block: MiddleWare<State>) {
            middleWares.add(block)
        }

        internal fun build() = middleWares
    }
}
