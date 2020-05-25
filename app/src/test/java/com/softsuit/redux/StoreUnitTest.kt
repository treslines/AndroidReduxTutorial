package com.softsuit.redux

import com.google.gson.Gson
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

        val store = DI.store
        val actual = store.getAppState()
        val expected = AppState(id = "Redux Tutorial App", isRoot = true)
        assertEquals(true, store.isDeepEquals(actual, expected))
    }

    @Test
    fun getDeepCopy_isNotEqualsOriginal_success() {

        val store = DI.store
        val actual = store.getAppState()
        val expected = AppState(id = "Redux Tutorial App", isRoot = true)
        assertEquals(false, actual == expected)
    }

    @Test
    fun createComplexNestedState_copyDeep_success() {

        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", child = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", child = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = AppState(id = "Child 0", data = "Child 0 data")
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, child = children)
        val store = AppStore(initialState = rootState)
        val actual = store.getAppState()
        assertEquals(6, actual.child.size)
        assertEquals(0, actual.child[0].child.size)
        assertEquals(0, actual.child[1].child.size)
        assertEquals(0, actual.child[2].child.size)
        assertEquals("Child 3 data", actual.child[2].data)
        assertEquals(1, actual.child[3].child.size)
        assertEquals(3, actual.child[3].child[0].child.size)
        assertEquals(0, actual.child[3].child[0].child[0].child.size)
        assertEquals(0, actual.child[3].child[0].child[1].child.size)
        assertEquals(0, actual.child[3].child[0].child[2].child.size)
        assertEquals(0, actual.child[4].child.size)
        assertEquals(0, actual.child[5].child.size)
        assertEquals("Child 0 data", actual.child[5].data)

    }

    @Test
    fun createSpecificNestedState_lookUpForItOverId_success() {
        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", child = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", child = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, child = children)
        val store = AppStore(initialState = rootState)
        var actual = store.lookUp("Child 3")
        assertNotEquals(c3, actual)
        assertEquals(0, actual.child.size)
        assertEquals("Child 3 data", actual.data)

        actual = store.lookUp(c0)
        assertNotEquals(c0, actual)
        assertEquals(0, actual.child.size)
        assertEquals(null, actual.data)
        assertEquals("Search Result State", actual.id)
    }

    @Test
    fun registerConditionalObserver_changeSomething_observerTriggered_success() {

        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", child = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", child = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()

        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, child = children)
        val store = AppStore(initialState = rootState)

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
        store.subscribe(observer = aConditionalCounterStateObserver)

        store.dispatch(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.isRoot && old.hasChild()) {
                    old.child[1].data = "child 2 changed"
                }
                return old
            }
        })
    }

    @Test
    fun registerSimpleObserver_addNewStateToTree_observerTriggered_success() {
        class MyState() : AppState(id = "MyState")

        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", child = mutableListOf(c4, c5, c6 /* MyState will be added here */))
        val c8 = AppState(id = "Child 8", child = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()

        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, child = children)
        val store = AppStore(initialState = rootState)

        val aSimpleSearchResultStateObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = MyState()
            override fun onChange(state: AppState) {
                assertEquals("MyState", state.id)
            }
        }
        store.subscribe(aSimpleSearchResultStateObserver)

        // and new state to AppState tree, diese action weiss wo sich einfuegen soll
        store.dispatch(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.isRoot && old.hasChild()) {
                    old.child[3].child[0].child[2].child.add(MyState())
                }
                return old
            }
        })
    }

    @Test
    fun createComplexState_changeSomething_UpdateAppState_success() {
        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", child = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", child = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "Redux Tutorial App", isRoot = true, child = children)

        val appStateString =
            "{\"id\":\"Redux Tutorial App\",\"child\":[{\"id\":\"Child 1\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 2\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 3\",\"data\":\"Child 3 data\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 8\",\"child\":[{\"id\":\"Child 7\",\"child\":[{\"id\":\"Child 4\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 5\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 6\",\"child\":[],\"isRoot\":false}],\"isRoot\":false}],\"isRoot\":false},{\"id\":\"Child 9\",\"child\":[],\"isRoot\":false},{\"id\":\"Search Result State\",\"child\":[],\"isRoot\":false}],\"isRoot\":true}"

        if (appStateString.contains(c3.id)) { // annahme remove cmd
            // get all ids, remove one by one, the remaining one(s) is the one to remove
            if (appStateString.replace(Gson().toJson(c3), "").length != appStateString.length) {
                // can remove
            }
        }

        if (appStateString.contains(c3.id)) { // in state enthalten (annahme c3 hat sich geändert)
            val c3String = Gson().toJson(c3)
            val original = appStateString.length
            val r = appStateString.replace(c3String, "")
            if ((r.length - original) == c3String.length) {
                // hat sich nicht geändert
            }
            if (r.length == original) {
                // hat sich geändert, ersetze ganzes objekt
            }
        }
        println(c3.toString())


    }

    @Test
    fun insert() {
        var appStateString =
            "{\"id\":\"Redux Tutorial App\",\"child\":[{\"id\":\"Child 1\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 2\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 3\",\"data\":\"Child 3 data\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 8\",\"child\":[{\"id\":\"Child 7\",\"child\":[{\"id\":\"Child 4\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 5\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 6\",\"child\":[],\"isRoot\":false}],\"isRoot\":false}],\"isRoot\":false},{\"id\":\"Child 9\",\"child\":[],\"isRoot\":false},{\"id\":\"Search Result State\",\"child\":[],\"isRoot\":false}],\"isRoot\":true}"
        val res = insert(appStateString, "Child 1", "{\"id\":\"Child A\",\"child\":[],\"isRoot\":false}")
        println(res)
    }


    fun remove(toRemove: AppState): String {
        var appStateString =
            "{\"id\":\"Redux Tutorial App\",\"child\":[{\"id\":\"Child 1}\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 2\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 3\",\"data\":\"Child 3 data\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 8\",\"child\":[{\"id\":\"Child 7\",\"child\":[{\"id\":\"Child 4\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 5\",\"child\":[],\"isRoot\":false},{\"id\":\"Child 6\",\"child\":[],\"isRoot\":false}],\"isRoot\":false}],\"isRoot\":false},{\"id\":\"Child 9\",\"child\":[],\"isRoot\":false},{\"id\":\"Search Result State\",\"child\":[],\"isRoot\":false}],\"isRoot\":true}"
        return if (appStateString.contains(toRemove.id)) { // app state contains state to remove
            if (appStateString.replace(Gson().toJson(toRemove), "").length != appStateString.length) {
                appStateString.replace(Gson().toJson(toRemove), "").replace(",,", ",")
            } else { // exist but is not equals
                val target = findTarget(appStateString, toRemove.id)
                appStateString.replace(target, "").replace(",,", ",")
            }
        } else {
            appStateString
        }
    }

    private fun findTarget(appStateString: String, targetId: String): String {
        val startPosOffset = 7
        val idIndex = appStateString.indexOf(targetId)
        val searchString = appStateString.substring((idIndex - startPosOffset), appStateString.length)
        var end = 0
        var start = 0
        var endIndex = 0
        var currentIndex = 0
        for (c in searchString) {
            when (c) {
                '{' -> start++
                '}' -> end++
            }
            if (start == end) {
                endIndex = currentIndex + 1
                break
            }
            currentIndex++
        }
        return searchString.substring(0, endIndex)
    }

    private fun insert(appStateString: String, targetId: String, newState: String): String {
        val placeholder = appStateString.replace(findTarget(appStateString, targetId), "@ph@")
        val targetPlaceholder = findTarget(appStateString, targetId).replaceFirst("[", "[@ph@")
        return placeholder.replace("@ph@", targetPlaceholder.replace("@ph@", "$newState,")).replace(",]", "]")
    }

}
