package tmg.testutils.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.jupiter.api.Assertions.*
import tmg.utilities.lifecycle.DataEvent
import tmg.utilities.lifecycle.Event


/**
 * Assert method for live data
 */
fun <T> assertLiveData(liveData: LiveData<T>, block: LiveDataTestScope<T>.() -> Unit) {
    block(LiveDataTestScope(liveData))
}

/**
 * Assert method for live data
 */
fun <T> LiveData<T>.test(block: LiveDataTestScope<T>.() -> Unit) {
    block(LiveDataTestScope(this))
}

/**
 * Return the test scope observer to run custom checks on the observer
 */
fun <T> LiveData<T>.testObserve(): LiveDataTestScope<T> {
    return LiveDataTestScope(this)
}

/**
 * Live data testing scope class
 */
class LiveDataTestScope<T>(
    liveData: LiveData<T>
): Observer<T> {

    val listOfValues: MutableList<T> = mutableListOf()

    init {
        liveData.observeForever(this)
    }

    override fun onChanged(t: T) {
        listOfValues.add(t)
    }

    val latestValue: T?
        get() = listOfValues.lastOrNull()

    /**
     * Assert that the latest value emitted matches
     *  the live data matches exactly
     */
    fun assertValue(expected: T) {
        assertNotNull(latestValue)
        assertEquals(expected, latestValue)
    }

    /**
     * Given a number of items have been emitted, assert that the value at
     *  the given position matches what is expected
     */
    fun assertValueAt(expected: T, position: Int) {
        assertTrue(
            listOfValues.size > position,
            "Number of items emitted is less than position requested (${listOfValues.size} > $position)"
        )
        assertEquals(expected, listOfValues[position])
    }

    /**
     * Assert that of any of the values that have been emitted that one of them
     *  is the expected item
     */
    fun assertValueExists(expected: T) {
        listOfValues.forEach {
            if (it == expected) { return }
        }
        assertFalse(
            true,
            "All emitted items do not contain the expected item. $expected (${listOfValues.size} items emitted)"
        )
    }

    /**
     * Assert that the number of items emitted is equal to the expected value
     */
    fun assertEmittedCount(expected: Int) {
        assertEquals(expected, listOfValues.size)
    }

    /**
     * Assert that the items emitted match the expected list
     */
    fun assertEmittedItems(vararg expected: T) {
        assertEmittedItems(expected.toList())
    }

    /**
     * Assert that the items emitted match the expected list
     */
    fun assertEmittedItems(expected: List<T>) {
        assertEquals(expected, listOfValues)
    }
}

//region LiveDataTestScope<Event>

/**
 * Assert that an event has been fired in the latest value
 * @param exactly The number of times you expect the event to be fired. Null = at least 1 event is fired
 */
fun <T: Event> LiveDataTestScope<T>.assertEventFired(exactly: Int? = null) {
    exactly?.let {
        assertEquals(exactly, listOfValues.size, "Expected $exactly items to be emitted. Actually, ${listOfValues.size} was emitted")
    }
    assertNotNull(latestValue)
}

/**
 * Assert that an event has not been fired
 */
fun <T: Event> LiveDataTestScope<T>.assertEventNotFired() {
    assertNull(latestValue)
}

/**
 * Assert that a data event item has been fired
 *  and that the data contains the item
 * @param expected The item expected to be emitted
 * @param atIndex The index of the value you are asserting against. Default to first (0)
 */
fun <T> LiveDataTestScope<DataEvent<T>>.assertDataEventValue(expected: T, atIndex: Int = 0) {
    assertEventFired()
    assertEquals(expected, listOfValues[atIndex].data)
}

/**
 * Assert that a data event item has been fired
 *  and that the data contains the item
 * @param atIndex The index of the value you are asserting against. Default to first (0)
 * @param predicate Method to determine if item matches enough
 */
fun <T> LiveDataTestScope<DataEvent<T>>.assertDataEventMatches(atIndex: Int = 0, predicate: (item: T) -> Boolean) {
    assertEventFired()
    assertTrue(
        predicate(listOfValues[atIndex].data),
        "Item emitted does not match the predicate"
    )
}

//endregion

//region LiveDataTestScope<List<T>>

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListContainsItems(vararg item: T) {
    assertListContainsItems(item.toList())
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 * @param item the list of items to include within a list
 * @param atIndex The index in the emitted item in the live data
 */
fun <T> LiveDataTestScope<List<T>>.assertListContainsItems(item: List<T>, atIndex: Int = 0) {
    if (item.isEmpty()) {
        assertValue(emptyList())
    }
    item.forEach {
        assertListContainsItem(it, atIndex)
    }
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 * @param item The item to check is within a list
 * @param atIndex The index in the emitted item in the live data
 */
fun <T> LiveDataTestScope<List<T>>.assertListContainsItem(item: T, atIndex: Int = 0) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    listOfValues[atIndex].forEach {
        if (it == item) return
    }
    assertFalse(
        true,
        "List does not contain the expected item - $item (${listOfValues[atIndex].size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListMatchesItem(atIndex: Int = 0, predicate: (item: T) -> Boolean) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    listOfValues[atIndex].forEach {
        if (predicate(it)) return
    }
    assertFalse(
        true,
        "List does not contain an item that matches the predicate - (${listOfValues[atIndex].size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListDoesNotMatchItem(atIndex: Int = 0, predicate: (item: T) -> Boolean) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    var assertionValue = false
    listOfValues[atIndex].forEach {
        if (predicate(it)) assertionValue = true
    }
    assertFalse(
        assertionValue,
        "List does contains an item that matches the predicate - (${listOfValues[atIndex].size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListExcludesItem(item: T, atIndex: Int = 0) {
    return assertListDoesNotMatchItem(atIndex = atIndex) { it == item }
}

/**
 * Assert that the latest value emitted contains 0 items
 */
fun <T> LiveDataTestScope<List<T>>.assertListNotEmpty(atIndex: Int = 0) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    assertTrue(listOfValues[atIndex].isNotEmpty(), "List contains 0 items")
}

/**
 * Assert that the last item in the latest value emitted is equal to this item
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasLastItem(item: T, atIndex: Int = 0) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    assertEquals(item, listOfValues[atIndex].last())
}

/**
 * Assert that the first item in the latest value emitted is equal to this item
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasFirstItem(item: T, atIndex: Int = 0) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    assertEquals(item, listOfValues[atIndex].first())
}

/**
 * Assert that the list in the latest value contains this sublist of items in the order specified
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasSublist(list: List<T>, atIndex: Int = 0) {
    assertTrue(atIndex < listOfValues.size, "Index is out of bounds")
    assertNotNull(listOfValues[atIndex])
    assertTrue(
        list.isNotEmpty(),
        "Expected list is empty, which will match any value. Consider changing your assertion"
    )
    val filter = listOfValues[atIndex].filter { list.contains(it) }
    assertEquals(
        filter.size,
        list.size,
        "Expected list of size ${list.size} but can only find ${filter.size} items in common"
    )
    assertEquals(filter, list)
}

//endregion