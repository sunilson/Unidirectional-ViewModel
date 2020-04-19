package at.sunilson.unidirectionalviewmodel

import androidx.lifecycle.SavedStateHandle
import kotlin.reflect.KProperty1
import kotlin.reflect.full.hasAnnotation
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
    initialState: State,
    savedStateHandle: SavedStateHandle
) : UniDirectionalSavedStateViewModel<State, Event>(initialState, savedStateHandle) {

    @ExperimentalStdlibApi
    override fun State.initializeStateFromSavedState(savedStateHandle: SavedStateHandle): State {
        assertPersistableState(this)
        assertDataClass(this)

        val copy = this::class.memberFunctions.firstOrNull { it.name == "copy" }
        checkNotNull(copy) { "Copy constructor instance was null!" }

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

    private fun assertDataClass(state: State) {
        try {
            check(this::class.isData)
        } catch (error: Exception) {
            error("The state must be a data class!")
        }
    }

    @ExperimentalStdlibApi
    private fun assertPersistableState(state: State) {
        try {
            check(state::class.hasAnnotation<PersistableState>())
        } catch (error: Exception) {
            error("Please make sure your State class is annotated with @PersistableState")
        }
    }
}
