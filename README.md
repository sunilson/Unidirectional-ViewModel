# Unidirectional-ViewModel

To use the library you just need your ViewModel to extend the `UniDirectionalViewModel` or `UniDirectionalSavedStateViewModel` if you want to use `SavedStateHandle`.

Then you can use `setState` and `getState` in your ViewModel to mutate and/or access the state.

## State

TODO

## SetState

In a `setState` block you can access the current state and return a new state. You have multiple options how to structure your state.

### Sealed class example

TODO

### Single data class example

TODO

## GetState
A `getState` block just provides you with the current state, but there is one special thing about it. All queued `setState` actions will be executed before any `getState` action is executed to always have the newest state inside a `getState` block. 

## SavedStateHandle

TODO

## Events

TODO

## Middleware 

TODO


[![](https://jitpack.io/v/sunilson/Unidirectional-ViewModel.svg)](https://jitpack.io/#sunilson/Unidirectional-ViewModel)
