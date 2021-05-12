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
        val liveDataNullable: MutableLiveData<Model?> = MutableLiveData()
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
        fun `throws null exception if no items emitted`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertValue(Model("my-id"))
                }
            }
        }

        @Test
        fun `throws equal exception if item doesnt match expected`() {
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

    @Nested
    inner class assertEmittedCount {

        @Test
        fun `throws exception if item count is different` () {
            initSUT()
            val observer = sut.liveData.testObserve()
            sut.liveData.value = Model("a")
            sut.liveData.value = Model("b")
            assertThrows<AssertionError> {
                observer.assertEmittedCount(1)
            }
        }

        @Test
        fun `does not throw exception when count matches`() {
            initSUT()
            val observer = sut.liveData.testObserve()
            sut.liveData.value = Model("a")
            sut.liveData.value = Model("b")
            observer.assertEmittedCount(2)
        }
    }

    @Nested
    inner class assertEmittedItems {

        @Test
        fun `throws exception when list of values does not match`() {
            initSUT()
            sut.liveData.value = Model("c")
            sut.liveData.value = Model("d")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertEmittedItems(listOf(Model("a"), Model("b")))
                }
            }
        }

        @Test
        fun `throws exception when vararg list of values does not match`() {
            initSUT()
            sut.liveData.value = Model("c")
            sut.liveData.value = Model("d")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertEmittedItems(Model("a"), Model("b"))
                }
            }
        }

        @Test
        fun `throws exception when items are in incorrect order`() {
            initSUT()
            sut.liveData.value = Model("c")
            sut.liveData.value = Model("d")
            assertThrows<AssertionError> {
                sut.liveData.test {
                    assertEmittedItems(Model("d"), Model("c"))
                }
            }
        }

        @Test
        fun `does not throw exception when list matches`() {
            initSUT()
            val observer = sut.liveData.testObserve()
            sut.liveData.value = Model("c")
            sut.liveData.value = Model("d")
            observer.assertEmittedItems(Model("c"), Model("d"))
        }
    }

    @Nested
    inner class assertEventFired {

        @Test
        fun `throws exception if event has not been fired`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataEvent.test {
                    assertEventFired()
                }
            }
        }

        @Test
        fun `throws exception if event has been fired but count is less than total events`() {
            initSUT()
            val observer = sut.liveDataEvent.testObserve()
            sut.liveDataEvent.value = Event()
            sut.liveDataEvent.value = Event()
            assertThrows<AssertionError> {
                observer.assertEventFired(exactly = 1)
            }
        }

        @Test
        fun `does not throw exception if event been fired`() {
            initSUT()
            sut.liveDataEvent.value = Event()
            sut.liveDataEvent.test {
                assertEventFired()
            }
        }

        @Test
        fun `does not throw exception if event been fired at least once`() {
            initSUT()
            sut.liveDataEvent.value = Event()
            sut.liveDataEvent.value = Event()
            sut.liveDataEvent.test {
                assertEventFired()
            }
        }

        @Test
        fun `does not throw exception if number of events been fired matches count`() {
            initSUT()
            val observer = sut.liveDataEvent.testObserve()
            sut.liveDataEvent.value = Event()
            sut.liveDataEvent.value = Event()
            observer.assertEventFired(exactly = 2)
        }
    }

    @Nested
    inner class assertEventNotFired {

        @Test
        fun `throws exception if event has been fired`() {
            initSUT()
            sut.liveDataEvent.value = Event()
            assertThrows<AssertionError> {
                sut.liveDataEvent.test {
                    assertEventNotFired()
                }
            }
        }

        @Test
        fun `does not throw exception if event hasnt been fired`() {
            initSUT()
            sut.liveDataEvent.test {
                assertEventNotFired()
            }
        }
    }

    @Nested
    inner class assertDataEventValue {

        @Test
        fun `throws exception if no event emitted`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataDataEvent.test {
                    assertDataEventValue("hey")
                }
            }
        }

        @Test
        fun `throws exception if data event value doesnt match`() {
            initSUT()
            sut.liveDataDataEvent.value = DataEvent("sup")
            assertThrows<AssertionError> {
                sut.liveDataDataEvent.test {
                    assertDataEventValue("hey")
                }
            }
        }

        @Test
        fun `throws exception if data event value is at different index`() {
            initSUT()
            val observer = sut.liveDataDataEvent.testObserve()
            sut.liveDataDataEvent.value = DataEvent("sup")
            sut.liveDataDataEvent.value = DataEvent("hey")
            assertThrows<AssertionError> {
                observer.assertDataEventValue("hey", atIndex = 0)
            }
        }

        @Test
        fun `does not throw exception if value matches`() {
            initSUT()
            sut.liveDataDataEvent.value = DataEvent("sup")
            sut.liveDataDataEvent.test {
                assertDataEventValue("sup")
            }
        }
    }

    @Nested
    inner class assertDataEventMatches {

        @Test
        fun `throws exception if no event emitted`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataDataEvent.test {
                    assertDataEventMatches {
                        true
                    }
                }
            }
        }

        @Test
        fun `throws exception if data event value doesnt match`() {
            initSUT()
            sut.liveDataDataEvent.value = DataEvent("sup")
            assertThrows<AssertionError> {
                sut.liveDataDataEvent.test {
                    assertDataEventMatches {
                        it == "hey"
                    }
                }
            }
        }

        @Test
        fun `throws exception if data event value is at different index`() {
            initSUT()
            val observer = sut.liveDataDataEvent.testObserve()
            sut.liveDataDataEvent.value = DataEvent("sup")
            sut.liveDataDataEvent.value = DataEvent("hey")
            assertThrows<AssertionError> {
                observer.assertDataEventMatches(atIndex = 0) {
                    it == "hey"
                }
            }
        }

        @Test
        fun `does not throw exception if value matches`() {
            initSUT()
            sut.liveDataDataEvent.value = DataEvent("sup")
            sut.liveDataDataEvent.test {
                assertDataEventMatches {
                    it == "sup"
                }
            }
        }
    }

    @Nested
    inner class assertListContainsItems {

        @Test
        fun `throws exception when live data has no value`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListContainsItems(emptyList())
                }
            }
        }

        @Test
        fun `throws exception when one item in list does not match`() {
            initSUT()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListContainsItems(listOf(Model("b"), Model("c")))
                }
            }
        }

        @Test
        fun `throws exception if list matches but at different position`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            assertThrows<AssertionError> {
                observer.assertListContainsItems(listOf(Model("b"), Model("c")), atIndex = 0)
            }
        }

        @Test
        fun `doesnt throw exception if list matches latest value`() {
            initSUT()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.test {
                assertListContainsItems(listOf(Model("a"), Model("b")))
            }
        }

        @Test
        fun `doesnt throw exception if list matches value at specified index`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            observer.assertListContainsItems(listOf(Model("a"), Model("b")), atIndex = 0)
        }
    }

    @Nested
    inner class assertListContainsItem {

        @Test
        fun `throws exception if live data has no values`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListContainsItem(Model("a"))
                }
            }
        }

        @Test
        fun `throws exception if live data item is not in the list`() {
            initSUT()
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListContainsItem(Model("a"))
                }
            }
        }

        @Test
        fun `throws exception if live data item is in list at different index`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            assertThrows<AssertionError> {
                observer.assertListContainsItem(Model("a"), atIndex = 1)
            }
        }

        @Test
        fun `does not throw exception exception if live data item is in the list`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            observer.assertListContainsItem(Model("a"))
        }

        @Test
        fun `does not throw exception exception if live data item is in the list at specified index`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("b"), Model("c"))
            observer.assertListContainsItem(Model("c"), atIndex = 1)
        }
    }

    @Nested
    inner class assertListMatchesItem {

        @Test
        fun `throws exception if live data has no value`() {
            initSUT()
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListMatchesItem {
                        true
                    }
                }
            }
        }

        @Test
        fun `throws exception if item cannot be matched`() {
            initSUT()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            assertThrows<AssertionError> {
                sut.liveDataList.test {
                    assertListMatchesItem {
                        it.id == "c"
                    }
                }
            }
        }

        @Test
        fun `throws exception if item is matched but in different index`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("a"), Model("c"))
            assertThrows<AssertionError> {
                observer.assertListMatchesItem(atIndex = 0) {
                    it.id == "c"
                }
            }
        }

        @Test
        fun `does not throws exception if list item is matched in list`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("a"), Model("c"))
            observer.assertListMatchesItem {
                it.id == "b"
            }
        }

        @Test
        fun `does not throws exception if list item is matched in list at specified index`() {
            initSUT()
            val observer = sut.liveDataList.testObserve()
            sut.liveDataList.value = listOf(Model("a"), Model("b"))
            sut.liveDataList.value = listOf(Model("a"), Model("c"))
            observer.assertListMatchesItem(atIndex = 1) {
                it.id == "c"
            }
        }
    }
}