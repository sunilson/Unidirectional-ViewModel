package at.sunilson.unidirectionalviewmodel

import at.sunilson.unidirectionalviewmodel.core.UniDirectionalViewModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestCoroutineDispatcherExtension::class)
class UniDirectionalViewModelTests {

    private data class State(val string: String)
    private sealed class Events
    private class TestViewModel : UniDirectionalViewModel<State, Events>(State(""))

    private lateinit var viewModel: TestViewModel

    @BeforeEach
    fun before() {
        viewModel = TestViewModel()
    }

    @Test
    fun `Events are only recieved once`() {
        TODO()
    }

    @Test
    fun `Multiple subscribers all recieve events`() {
        TODO()
    }

    @Test
    fun `State is recieved upon subscription`() {
        TODO()
    }

    @Test
    fun `SetState and GetState are executed in correct order`() {
        TODO()
    }

    @Test
    fun `All middlewares are executed and in correct order`() {
        TODO()
    }
}
