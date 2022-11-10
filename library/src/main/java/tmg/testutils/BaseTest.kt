package tmg.testutils

import androidx.annotation.CallSuper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import tmg.testutils.rules.CoroutineRule
import tmg.testutils.taskexecutor.TestTaskExecutor
import tmg.testutils.taskexecutor.TestingTaskExecutor

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(TestingTaskExecutor::class)
open class BaseTest {

    @get:Rule
    val coroutineScope = CoroutineRule()

    protected val testDispatcher = coroutineScope.testDispatcher

    @BeforeEach
    @CallSuper
    open fun beforeAll() {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Run a test with test coroutine scope
     * - advanceUntilIdle()
     */
    fun coroutineTest(block: TestScope.() -> Unit) {
        runTest(testDispatcher) {
            block(this)
        }
    }
}