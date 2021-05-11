package tmg.testutils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tmg.testutils.livedata.assertEventFired
import tmg.testutils.livedata.test
import tmg.utilities.lifecycle.Event
import java.lang.RuntimeException

internal class BaseTestSuperclassAssignmentTest {

    inner class TestViewModel: ViewModel() {
        val output: MutableLiveData<Event> = MutableLiveData()

        fun runSomeLogic() {
            viewModelScope.launch {
                output.value = Event()
            }
        }
    }

    @Nested
    inner class Assigned: BaseTest() {

        private lateinit var sut: TestViewModel

        private fun initSUT() {
            sut = TestViewModel()
        }

        @Test
        fun `base test causes coroutines inside test functions not to crash`() {

            initSUT()
            sut.runSomeLogic()

            sut.output.test {
                assertEventFired()
            }
        }
    }

    @Nested
    inner class NotAssigned {


        private lateinit var sut: TestViewModel

        private fun initSUT() {
            sut = TestViewModel()
        }

        @Test
        fun `running live data function with base test not extended throws runtime exception`() {

            initSUT()
            sut.runSomeLogic()

            org.junit.jupiter.api.assertThrows<RuntimeException> {
                sut.output.test {
                    assertEventFired()
                }
            }
        }
    }
}