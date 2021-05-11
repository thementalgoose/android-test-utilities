package tmg.testutils.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.jupiter.api.Assertions
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
        Assertions.assertNotNull(latestValue)
        Assertions.assertEquals(expected, latestValue)
    }

    /**
     * Given a number of items have been emitted, assert that the value at
     *  the given position matches what is expected
     */
    fun assertValueAt(expected: T, position: Int) {
        Assertions.assertTrue(
            listOfValues.size > position,
            "Number of items emitted is less than position requested (${listOfValues.size} > $position)"
        )
        Assertions.assertEquals(expected, listOfValues[position])
    }

    /**
     * Assert that of any of the values that have been emitted that one of them
     *  is the expected item
     */
    fun assertValueExists(expected: T) {
        listOfValues.forEach {
            if (it == expected) { return }
        }
        Assertions.assertFalse(
            true,
            "All emitted items do not contain the expected item. $expected (${listOfValues.size} items emitted)"
        )
    }

    /**
     * Assert that the number of items emitted is equal to the expected value
     */
    fun assertItemCount(expected: Int) {
        Assertions.assertEquals(listOfValues.size, expected)
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
        Assertions.assertEquals(listOfValues, expected)
    }
}

//region LiveDataTestScope<Event>

/**
 * Assert that an event has been fired in the latest value
 */
fun <T: Event> LiveDataTestScope<T>.assertEventFired() {
    Assertions.assertNotNull(latestValue)
}

/**
 * Assert that an event has not been fired
 */
fun <T: Event> LiveDataTestScope<T>.assertEventNotFired() {
    Assertions.assertNull(latestValue)
}

/**
 * Assert that a data event item has been fired
 *  and that the data contains the item
 */
fun <T> LiveDataTestScope<DataEvent<T>>.assertDataEventValue(expected: T) {
    assertEventFired()
    Assertions.assertEquals(expected, latestValue!!.data)
}

/**
 * Assert that a data event item has been fired
 *  and that the data contains the item
 */
fun <T> LiveDataTestScope<DataEvent<T>>.assertDataEventMatches(predicate: (item: T) -> Boolean) {
    assertEventFired()
    Assertions.assertTrue(
        predicate(latestValue!!.data),
        "Item emitted does not match the predicate"
    )
}

//endregion

//region LiveDataTestScope<List<T>>

/**
 * If the scope allows nullable values, check that the item exposed is null
 */
fun <T> LiveDataTestScope<T?>.assertValueNull() {
    Assertions.assertNull(latestValue)
}

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
 */
fun <T> LiveDataTestScope<List<T>>.assertListContainsItems(item: List<T>) {
    item.forEach {
        assertListContainsItem(it)
    }
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListContainsItem(item: T) {
    Assertions.assertNotNull(latestValue)
    latestValue!!.forEach {
        if (it == item) return
    }
    Assertions.assertFalse(
        true,
        "List does not contain the expected item - $item (${latestValue!!.size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListMatchesItem(predicate: (item: T) -> Boolean) {
    Assertions.assertNotNull(latestValue)
    latestValue!!.forEach {
        if (predicate(it)) return
    }
    Assertions.assertFalse(
        true,
        "List does not contain an item that matches the predicate - (${latestValue!!.size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListDoesNotMatchItem(predicate: (item: T) -> Boolean) {
    Assertions.assertNotNull(latestValue)
    var assertionValue = false
    latestValue!!.forEach {
        if (predicate(it)) assertionValue = true
    }
    Assertions.assertFalse(
        assertionValue,
        "List does contains an item that matches the predicate - (${latestValue!!.size} items)"
    )
}

/**
 * Assert that given the subject is a list that only one list has been
 *  emitted and the list contains the following item
 */
fun <T> LiveDataTestScope<List<T>>.assertListExcludesItem(item: T) {
    Assertions.assertNotNull(latestValue)
    latestValue!!.forEach {
        if (it != item) return
    }
    Assertions.assertFalse(
        true,
        "List contains an item that matches the predicate when exclusion is required - $item (${latestValue!!.size} items)"
    )
}

/**
 * Assert that the latest value emitted contains 0 items
 */
fun <T> LiveDataTestScope<List<T>>.assertListNotEmpty() {
    Assertions.assertNotNull(latestValue)
    Assertions.assertTrue(latestValue!!.isNotEmpty(), "List contains 0 items")
}

/**
 * Assert that the last item in the latest value emitted is equal to this item
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasLastItem(item: T) {
    Assertions.assertNotNull(latestValue)
    Assertions.assertEquals(item, latestValue!!.last())
}

/**
 * Assert that the first item in the latest value emitted is equal to this item
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasFirstItem(item: T) {
    Assertions.assertNotNull(latestValue)
    Assertions.assertEquals(item, latestValue!!.first())
}

/**
 * Assert that the list in the latest value contains this sublist of items in the order specified
 */
fun <T> LiveDataTestScope<List<T>>.assertListHasSublist(list: List<T>) {
    Assertions.assertNotNull(latestValue)
    Assertions.assertTrue(
        list.isNotEmpty(),
        "Expected list is empty, which will match any value. Consider changing your assertion"
    )
    val filter = latestValue!!.filter { list.contains(it) }
    Assertions.assertEquals(
        filter.size,
        list.size,
        "Expected list of size ${list.size} but can only find ${filter.size} items in common"
    )
    Assertions.assertEquals(filter, list)
}

//endregion