package tmg.testutils

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import tmg.testutils.rules.CoroutineRule
import tmg.testutils.taskexecutor.TestTaskExecutor
import tmg.testutils.taskexecutor.TestingTaskExecutor

@ExtendWith(TestingTaskExecutor::class)
open class BaseTest {

    @get:Rule
    val coroutineScope = CoroutineRule()

    private val testDispatcher = coroutineScope.testDispatcher
    private val testScope = coroutineScope.testScope

    @BeforeEach
    @CallSuper
    open fun beforeAll() {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Run a test with test coroutine scope
     * - advanceUntilIdle()
     */
    fun coroutineTest(block: TestCoroutineScope.() -> Unit) {
        runBlockingTest(testDispatcher) {
            block(this)
        }
    }
}