# Unidirectional-ViewModel

[![](https://jitpack.io/v/sunilson/Unidirectional-ViewModel.svg)](https://jitpack.io/#sunilson/Unidirectional-ViewModel)

To use the library you just need your ViewModel to extend the `UniDirectionalViewModel` or `UniDirectionalSavedStateViewModel` if you want to use `SavedStateHandle`.

Then you can use `setState` and `getState` in your ViewModel to mutate and/or access the state.

## State

When you inherit from `UniDirectionalViewModel` you will need to pass in a generic State type. This can be any class but I would recommend using a `data` class or a `sealed` class containing multiple `data` classes.

## SetState

In a `setState` block you can access the current state and return a new state. You have multiple options how to structure your state.

### Sealed class example

```
sealed class TestState {
    data class Data(val data: List<Any>): TestState()
    data class Error(val error: String): TestState()
    object Empty: TestState()
    object Loading: TestState()
}

class Test : UniDirectionalViewModel<TestState, Events>(TestState.Empty) {
    fun loadData() {
        setState { TestState.Loading }
        
        someAsyncWork()
            .onSuccess { setState { TestState.Data(result) } }
            .onError { setState { TestState.Error(message) } }
    }
}
```

### Single data class example

```
data class TestState(
    val loading: Boolean = false,
    val data: List<Any> = listOf(),
    val error: String? = null
)

class Test : UniDirectionalViewModel<TestState, Events>(TestState()) {
    fun loadData() {
        setState { copy(loading = true) }

        someAsyncWork()
            .onSuccess { setState { copy(loading = false, data = result, error = null) } }
            .onError { setState { copy(loading = false, error = message) } }
    }
}
```

## GetState
A `getState` block just provides you with the current state, but there is one special thing about it. All queued `setState` actions will be executed before any `getState` action is executed to always have the newest state inside a `getState` block. 

## Usage in consumer

To use the ViewModel in your Fragment or Activity you just need to subscribe to the state and/or events of your ViewModel.

```
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    //Subscribe to state
    lifecycleScope.launch {
        viewModel.state.collect {... }
    }

    //Subscribe to one-time events
    lifecycleScope.launch {
        viewModel.events.collect { ... }
    }
}
```

## SavedStateHandle

An Android ViewModel can take in a `SavedStateHandle` which will be kept across process death and be passed in to the ViewModel after the process recreation again. If you want to utilize this feature you need to extend either `UniDirectionalSavedStateViewModel` or `UniDirectionalSavedStateViewModelReflection`. 

When you implement the first one, you will have to implement two methods. `updateStateHandle(state: State)` will be called everytime the state changes. In there you need to save all data you want to persist into the `SavedStateHandle`. The second method is `initializeStateFromSavedState(savedStateHandle: SavedStateHandle)`. This method will be called once in the init block. In this method you need to populate a `State` instance with the values you previously saved into the `SavedStateHandle`.

```
override fun State.initializeStateFromSavedState(savedStateHandle: SavedStateHandle) =
    copy(myString = savedStateHandle.get<String>("myString"))

override fun SavedStateHandle.updateStateHandle(state: State) {
    set("myString", state.myString)
}
```

### Reflection

If you don't want to save your state properties manually you can use the `UniDirectionalSavedStateViewModelReflection`. If you use this class, your State needs to be a data class. Then you need to annotate your state with `@PersistableState` and all properties that should be persisted with `@Persist`. The ViewModel will then automatically save and restore those properties for you. 

```
//property2 will be persisted over the application process death

@PersistableState
data class State(val property1: String, @Persist val property2: String, val property3: String)
```

## Events

When you want to emit an action that is not kept in state, you can do that via `sendEvent(event)`. You can subscribe to those events via the `events` Flow. This flow acts like a Broadcast and will emit every `Event` exactly once. New subscribers don't get the latest event.

```
            someAsyncWork()
                .onSuccess { result -> setState { TestState.Data(result) } }
                .onError { error -> sendEvent(NetworkErrorHappened(error)) }
```

## Middleware 

If you want to do something everytime the state changes inside the ViewModel you can add a `MiddleWare` via the `registerMiddleWare(middleWare: MiddleWare<State>)` method. Everytime the state changes, all registered middlewares will be notified about the new state in the order they were added. This way you can for example attach a logger to the state of the ViewModel. Also, every `MiddleWare` needs to return a `State` object which will be passed to the next `MiddleWare`, so they could do some alteration before the next step is executed. Example:

```
fun <T> logger(): MiddleWare<T> {
    return { state ->
        //Execute your operations
        Log.d("Log", state.toString())
        
        //Return new or unchanged state
        state
    }
}

registerMiddleWare(logger())
```
