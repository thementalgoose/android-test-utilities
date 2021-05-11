package tmg.testutils.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmg.testutils.BaseTest
import tmg.utilities.lifecycle.DataEvent
import tmg.utilities.lifecycle.Event
import java.lang.AssertionError
import java.util.*

internal class LiveDataTestScopeTest: BaseTest() {

    data class Model(
        val id: String = UUID.randomUUID().toString()
    )
    inner class TestLD: ViewModel() {
        val liveData: MutableLiveData<Model> = MutableLiveData()
        val liveDataEvent: MutableLiveData<Event> = MutableLiveData()
        val liveDataDataEvent: MutableLiveData<DataEvent<String>> = MutableLiveData()
        val liveDataList: MutableLiveData<List<Model>> = MutableLiveData()
    }

    private lateinit var sut: TestLD

    private fun initSUT() {
        sut = TestLD()
    }

    @Nested
    inner class assertValue {

        @Test
        fun `throws null assertion error if no items emitted`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValue(Model("my-id"))
                }
            }
        }

        @Test
        fun `throws equal assertion error if item doesnt match expected`() {
            initSUT()
            sut.liveData.value = Model("hey")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValue(Model("my-id"))
                }
            }
        }

        @Test
        fun `doesnt throw error when models match`() {
            initSUT()
            sut.liveData.value = Model("hey")
            assertDoesNotThrow {
                sut.liveData.test {
                    assertValue(Model("hey"))
                }
            }
        }
    }

    @Nested
    inner class assertValueAt {

        @Test
        fun `throws exception is position is higher than list`() {
            initSUT()
            sut.liveData.value = Model("sup")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValueAt(Model("sup"), 1)
                }
            }
        }

        @Test
        fun `throws exception is model does not match`() {
            initSUT()
            sut.liveData.value = Model("sup")
            sut.liveData.value = Model("hey")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValueAt(Model("sup"), 1)
                }
            }
        }

        @Test
        fun `does not throw error when models match`() {
            initSUT()
            sut.liveData.value = Model("sup")
            sut.liveData.value = Model("hey")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValueAt(Model("sup"), 0)
                    assertValueAt(Model("hey"), 1)
                }
            }
        }
    }

    @Nested
    inner class assertValueExists {

        @Test
        fun `throws exception when item doesnt exist in list`() {
            initSUT()
            sut.liveData.value = Model("a")
            sut.liveData.value = Model("b")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValueExists(Model("c"))
                }
            }
        }

        @Test
        fun `does not throw exception when value is found in list`() {
            initSUT()
            sut.liveData.value = Model("a")
            sut.liveData.value = Model("b")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValueExists(Model("a"))
                    assertValueExists(Model("b"))
                }
            }
        }
    }
}