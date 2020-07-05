package at.sunilson.unidirectionalviewmodel

import at.sunilson.unidirectionalviewmodel.core.MiddleWare
import at.sunilson.unidirectionalviewmodel.core.UniDirectionalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestCoroutineDispatcherExtension::class)
class UniDirectionalViewModelTests {

    private data class State(val string: String)
    private sealed class Events {
        object Event1 : Events()
        object Event2 : Events()
    }


    @Test
    fun `Events are recieved`() = runBlockingTest {
        class TestViewModel : UniDirectionalViewModel<State, Events>(State("")) {
            init {
                this@runBlockingTest.launch {
                    delay(1000L)
                    sendEvent(Events.Event1)
                }
            }
        }

        val viewModel = TestViewModel()

        launch {
            val event = viewModel.events.first()
            assertTrue(event is Events.Event1)
        }

        advanceTimeBy(1000L)
    }

    @Test
    fun `Multiple subscribers all recieve events`() = runBlockingTest {
        class TestViewModel : UniDirectionalViewModel<State, Events>(State("string")) {
            init {
                this@runBlockingTest.launch {
                    delay(1000L)
                    sendEvent(Events.Event1)
                }
            }
        }

        val instance = TestViewModel()

        (0..2).forEach {
            launch {
                assertTrue(instance.events.first() is Events.Event1)
            }
        }

        advanceTimeBy(1000L)
    }

    @Test
    fun `State is recieved upon subscription`() = runBlockingTest {
        class TestViewModel : UniDirectionalViewModel<State, Events>(State("string"))
        val instance = TestViewModel()

        assertEquals("string", instance.state.first().string)
    }

    @Test
    fun `SetState and GetState are executed in correct order`() = runBlockingTest {

        val calls = mutableListOf<Int>()

        class TestViewModel : UniDirectionalViewModel<State, Events>(State("string")) {
            init {
                setState {
                    calls.add(0)
                    copy()
                }

                setState {
                    calls.add(1)
                    copy()
                }

                getState {
                    calls.add(2)

                    setState {
                        calls.add(3)
                        copy()
                    }

                    setState {
                        calls.add(4)
                        copy()
                    }
                }

                setState {
                    calls.add(5)
                    copy()
                }

                getState {
                    calls.add(6)
                }
            }
        }

        TestViewModel()

        assertEquals(7, calls.size)
        calls.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }

    @Test
    fun `All middlewares are executed and in correct order`() {

        val calls = mutableListOf<Int>()

        val middleWare1: MiddleWare<State> = {
            calls.add(0)
            it
        }

        val middleWare2: MiddleWare<State> = {
            calls.add(1)
            it
        }

        val middleWare3: MiddleWare<State> = {
            calls.add(2)
            it
        }

        class TestViewModel : UniDirectionalViewModel<State, Events>(State("")) {
            init {
                registerMiddleWare(middleWare1)
                registerMiddleWare(middleWare2)
                registerMiddleWare(middleWare3)
                setState { copy() }
            }
        }

        TestViewModel()

        assertEquals(3, calls.size)
        calls.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }

    @Test
    fun `Middleware can manipulate state`() = runBlockingTest {
        val middleWare: MiddleWare<State> = {
            it.copy(string = it.string.toLowerCase())
        }


        class TestViewModel : UniDirectionalViewModel<State, Events>(State("")) {
            init {
                registerMiddleWare(middleWare)
                setState { copy(string = "UPPERCASE") }
            }
        }

        val instance = TestViewModel()

        assertTrue("uppercase".equals(instance.state.first().string, ignoreCase = false))
    }
}
