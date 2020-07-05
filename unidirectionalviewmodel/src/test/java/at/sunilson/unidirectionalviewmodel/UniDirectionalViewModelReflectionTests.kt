package at.sunilson.unidirectionalviewmodel

import androidx.lifecycle.SavedStateHandle
import at.sunilson.unidirectionalviewmodel.savedstate.Persist
import at.sunilson.unidirectionalviewmodel.savedstate.PersistableState
import at.sunilson.unidirectionalviewmodel.savedstate.UniDirectionalSavedStateViewModelReflection
import io.mockk.every
import io.mockk.mockk
import java.io.Serializable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestCoroutineDispatcherExtension::class)
class UniDirectionalViewModelReflectionTests {

    @Test
    fun `State must be data class`() {
        assertThrows<IllegalStateException> {
            @PersistableState
            class State()
            class Events
            class TestViewModel :
                UniDirectionalSavedStateViewModelReflection<State, Events>(State(), mockk())
            TestViewModel()
        }
    }

    @Test
    fun `State is restored from handle`() = runBlockingTest {
        data class CustomValue(val customString: String, val customNumber: Float)

        @PersistableState
        data class State(
            @Persist val string: String = "Initial",
            @Persist val number: Int = 0,
            val number2: Int = 0,
            @Persist val customValue: CustomValue? = null
        )

        class Events

        class TestViewModel(handle: SavedStateHandle) :
            UniDirectionalSavedStateViewModelReflection<State, Events>(State(), handle)

        val handle = mockk<SavedStateHandle> {
            every { get<Any>("customValue") } returns CustomValue("customString", 1.2f)
            every { get<Any>("string") } returns "Restored"
            every { get<Any>("number") } returns 1
            every { set<Any>(any(), any()) } answers {}
        }

        val instance = TestViewModel(handle)
        val state = instance.state.first()

        assertEquals("Restored", state.string)
        assertEquals(1, state.number)
        assertEquals(0, state.number2)
        assertEquals("customString", state.customValue?.customString)
        assertEquals(1.2f, state.customValue?.customNumber)
    }

    @Test
    fun `State is saved to handle`() = runBlockingTest {
        data class CustomValue(val customString: String, val customNumber: Float) : Serializable

        @PersistableState
        data class State(
            @Persist val string: String = "Initial",
            @Persist val number: Int = 0,
            val number2: Int = 0,
            @Persist val customValue: CustomValue = CustomValue("customString", 1.2f)
        )

        class Events

        class TestViewModel(handle: SavedStateHandle) :
            UniDirectionalSavedStateViewModelReflection<State, Events>(State(), handle) {
            init {
                this@runBlockingTest.launch {
                    delay(1000L)
                    setState {
                        copy(
                            string = "New",
                            number = 1,
                            number2 = 2,
                            customValue = CustomValue("customValue2", 2f)
                        )
                    }
                }
            }
        }

        val handle = SavedStateHandle()

        TestViewModel(handle)

        assertEquals("Initial", handle.get<Any>("string"))
        assertEquals(0, handle.get<Any>("number"))
        assertEquals(null, handle.get<Any>("number2"))
        assertEquals(CustomValue("customString", 1.2f), handle.get<Any>("customValue"))

        advanceTimeBy(1000L)

        assertEquals("New", handle.get<Any>("string"))
        assertEquals(1, handle.get<Any>("number"))
        assertEquals(null, handle.get<Any>("number2"))
        assertEquals(CustomValue("customValue2", 2f), handle.get<Any>("customValue"))
    }
}
