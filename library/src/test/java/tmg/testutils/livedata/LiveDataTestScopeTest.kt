package tmg.testutils.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
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

    @Test
    fun `assertValue throws null assertion error if no items emitted`() {
        initSUT()
        assertThrows<AssertionError> {
            sut.liveData.test {
                assertValue(Model("my-id"))
            }
        }
    }

    @Test
    fun `assertValue throws equal assertion error if item doesnt match expected`() {
        initSUT()
        sut.liveData.value = Model("hey")
        assertThrows<AssertionError> {
            sut.liveData.test {
                assertValue(Model("my-id"))
            }
        }
    }
}