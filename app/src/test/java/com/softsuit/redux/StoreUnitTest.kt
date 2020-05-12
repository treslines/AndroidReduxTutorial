package com.softsuit.redux

import com.softsuit.redux.mid.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StoreUnitTest {
//        var id: String,
//        var data: String? = null,
//        var children: MutableList<AppState> = mutableListOf(),
//        var hasChildren: Boolean = children.isNotEmpty(),
//        var isRoot: Boolean = false,
//        var hasData: Boolean = data != null

//        DI.reduxStore.dispatch()
//        DI.reduxStore.getDeepCopy()
//        DI.reduxStore.lookUp()
//        DI.reduxStore.reduce()
//        DI.reduxStore.subscribeConditionalState()
//        DI.reduxStore.subscribeMultiState()
//        DI.reduxStore.subscribeSimpleState()

    @Test
    fun getDeepCopy_success() {

        val store = DI.reduxStore
        val actual = store.getAppState()
        val expected = AppState(id = "Redux Tutorial App", isRoot = true)
        assertEquals(true, store.isDeepEquals(actual, expected))
    }

    @Test
    fun getDeepCopy_isNotEqualsOriginal_success() {

        val store = DI.reduxStore
        val actual = store.getAppState()
        val expected = AppState(id = "Redux Tutorial App", isRoot = true)
        assertEquals(false, actual == expected)
    }

    @Test
    fun createComplexNestedState_checkCopiedStateContent_success() {

        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", children = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", children = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = AppState(id = "Child 0", data = "Child 0 data")
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, children = children)
        val store = AppStore(initialState = rootState)
        val actual = store.getAppState()
        assertEquals(6, actual.children.size)
        assertEquals(0, actual.children[0].children.size)
        assertEquals(0, actual.children[1].children.size)
        assertEquals(0, actual.children[2].children.size)
        assertEquals("Child 3 data", actual.children[2].data)
        assertEquals(1, actual.children[3].children.size)
        assertEquals(3, actual.children[3].children[0].children.size)
        assertEquals(0, actual.children[3].children[0].children[0].children.size)
        assertEquals(0, actual.children[3].children[0].children[1].children.size)
        assertEquals(0, actual.children[3].children[0].children[2].children.size)
        assertEquals(0, actual.children[4].children.size)
        assertEquals(0, actual.children[5].children.size)
        assertEquals("Child 0 data", actual.children[5].data)

    }

    @Test
    fun lookupForSomeNestedState_success() {
        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", children = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", children = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, children = children)
        val store = AppStore(initialState = rootState)
        var actual = store.lookUp("Child 3")
        assertNotEquals(c3, actual)
        assertEquals(0, actual.children.size)
        assertEquals("Child 3 data", actual.data)

        actual = store.lookUp(c0)
        assertNotEquals(c0, actual)
        assertEquals(0, actual.children.size)
        assertEquals(null, actual.data)
        assertEquals("Search Result State", actual.id)
    }

    @Test
    fun createInitialState_changeSomething_checkHasChanced_success() {
        class MyState() : AppState(id = "MyState")

        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", children = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", children = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()

        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, children = children)
        val store = AppStore(initialState = rootState)

        val aSimpleSearchResultStateObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = MyState()
            override fun onChange(state: AppState) {
                assertEquals("MyState", state.id)
            }
        }
        store.subscribeSimpleState(aSimpleSearchResultStateObserver)

        val aCondition: ConditionReducer<AppState> = {
            it.data != null && it.id == "Child 2"
        }
        val aConditionalCounterStateObserver = object : ConditionStateObserver<AppState> {
            override fun match() = aCondition
            override fun observe() = AppState(id = "Child 2")
            override fun onChange(state: AppState) {
                assertEquals("Child 2", state.id)
                assertEquals("child 2 changed", state.data)
            }
        }
        store.subscribeConditionalState(observer = aConditionalCounterStateObserver)

        store.reduce(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.isRoot && old.hasChildren) {
                    old.children[1].data = "child 2 changed"
                }
                return old
            }
        })

        store.reduce(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.isRoot && old.hasChildren) {
                    old.children.add(MyState())
                }
                return old
            }
        })

    }

}
