package at.sunilson.unidirectionalviewmodel

import androidx.lifecycle.SavedStateHandle
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Persist

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PersistableState

abstract class UniDirectionalSavedStateViewModelReflection<State : Any, Event>(
    private val initialState: State,
    private val savedStateHandle: SavedStateHandle
) : UniDirectionalSavedStateViewModel<State, Event>(initialState, savedStateHandle) {

    override fun State.initializeStateFromSavedState(savedStateHandle: SavedStateHandle): State {
        val copy = this::class.memberFunctions.firstOrNull { it.name == "copy" }
        if (copy == null || !this::class.isData) {
            timber.log.Timber.e("Can't restore state for non data-class!")
            return this
        }

        val instanceParam = copy.instanceParameter
        checkNotNull(instanceParam) { "Copy constructor instance was null!" }

        val params = this::class
            .memberProperties
            .filter { property ->
                property.annotations.any { it is Persist } &&
                        savedStateHandle.get<Any>(property.name) != null
            }
            .associate { property ->
                copy
                    .parameters
                    .first { it.name == property.name } to savedStateHandle.get<Any>(property.name)
            }
            .toMutableMap()

        params[instanceParam] = this
        return copy.callBy(params) as State
    }

    override fun SavedStateHandle.updateStateHandle(state: State) {
        state::class.memberProperties.forEach { property ->
            if (property.annotations.any { it is Persist }) {
                set(
                    property.name,
                    (property as KProperty1<State, Any>).get(state)
                )
            }
        }
    }
}
