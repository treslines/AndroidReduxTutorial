package com.softsuit.redux

import com.fasterxml.jackson.databind.ObjectMapper
import com.softsuit.redux.example.CounterStateModel
import com.softsuit.redux.example.SampleStateModel
import com.softsuit.redux.example.SearchResultState
import com.softsuit.redux.oo.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test


/**
 * OO Redux Implementation Test
 */
class StoreUnitTest {

    @Test
    fun getDeepCopy_success() {
        val store = DI.store
        val expected = AppState(
            id = "RootState",
            isRoot = true,
            subStates = mutableListOf(
                AppState(id = "CounterState", data = ObjectMapper().writeValueAsString(CounterStateModel())),
                SearchResultState()
            )
        )
        assertEquals(true, store.isDeepEquals(expected))
    }

    @Test
    fun getDeepCopy_isNotEqualsOriginal_success() {
        val store = DI.store
        val actual = store.getAppState()
        val expected = AppState(id = "ReduxTutorialApp", isRoot = true)
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
        val c7 = AppState(id = "Child 7", subStates = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", subStates = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = AppState(id = "Child 0", data = "Child 0 data")
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "ReduxTutorialApp", isRoot = true, subStates = children)
        val store = AppStore(initialState = rootState)
        var actual = store.getAppState()
        assertEquals(6, actual.subStates.size)
        assertEquals(0, actual.subStates[0].subStates.size)
        assertEquals(0, actual.subStates[1].subStates.size)
        assertEquals(0, actual.subStates[2].subStates.size)
        assertEquals("Child 3 data", actual.subStates[2].data)
        assertEquals(1, actual.subStates[3].subStates.size)
        assertEquals(3, actual.subStates[3].subStates[0].subStates.size)
        assertEquals(0, actual.subStates[3].subStates[0].subStates[0].subStates.size)
        assertEquals(0, actual.subStates[3].subStates[0].subStates[1].subStates.size)
        assertEquals(0, actual.subStates[3].subStates[0].subStates[2].subStates.size)
        assertEquals(0, actual.subStates[4].subStates.size)
        assertEquals(0, actual.subStates[5].subStates.size)
        assertEquals("Child 0 data", actual.subStates[5].data)

        // hold reference, change something on it
        c3.data = "my new data"
        // get app state and check for different content
        actual = store.getAppState()
        assertNotEquals(c3.data, actual.subStates[2].data)
    }

    @Test
    fun createSpecificNestedState_lookUpForItOverId_success() {
        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", subStates = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", subStates = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "ReduxTutorialApp", isRoot = true, subStates = children)
        val store = AppStore(initialState = rootState)
        var actual = store.lookUpBy(c3)
        assertNotEquals(c3, actual)
        assertEquals(0, actual.subStates.size)
        assertEquals("Child 3 data", actual.data)

        actual = store.lookUpBy(c0)
        assertNotEquals(c0, actual)
        assertEquals(0, actual.subStates.size)
        assertEquals("", actual.data)
        assertEquals("SearchResultState", actual.id)
    }

    @Test
    fun registerConditionalObserver_changeSomething_observerTriggered_success() {
        val c1 = AppState(id = "Child 1")
        val c2 = AppState(id = "Child 2")
        val c3 = AppState(id = "Child 3", data = "Child 3 data")
        val c4 = AppState(id = "Child 4")
        val c5 = AppState(id = "Child 5")
        val c6 = AppState(id = "Child 6")
        val c7 = AppState(id = "Child 7", subStates = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", subStates = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "ReduxTutorialApp", isRoot = true, subStates = children)
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
                if (old.isRoot && old.hasSubStates()) {
                    old.subStates[1].data = "child 2 changed"
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
        val c7 = AppState(id = "Child 7", subStates = mutableListOf(c4, c5, c6 /* MyState will be added here */))
        val c8 = AppState(id = "Child 8", subStates = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "ReduxTutorialApp", isRoot = true, subStates = children)
        val store = AppStore(initialState = rootState)

        val aSimpleSearchResultStateObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = MyState()
            override fun onChange(state: AppState) {
                assertEquals("MyState", state.id)
            }
        }

        store.subscribe(aSimpleSearchResultStateObserver)
        store.dispatch(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.isRoot && old.hasSubStates()) {
                    old.subStates[3].subStates[0].subStates[2].subStates.add(MyState())
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
        val c7 = AppState(id = "Child 7", subStates = mutableListOf(c4, c5, c6))
        val c8 = AppState(id = "Child 8", subStates = mutableListOf(c7))
        val c9 = AppState(id = "Child 9")
        val c0 = SearchResultState()
        var children: MutableList<AppState> = mutableListOf(c1, c2, c3, c8, c9, c0)
        val rootState = AppState(id = "ReduxTutorialApp", isRoot = true, subStates = children)
        val store = AppStore(initialState = rootState)

        val appStateBeforeChange = store.getAppState()

        store.reduce(object : Action<AppState> {
            override fun reduce(old: AppState): AppState {
                if (old.subStates[3].subStates[0].subStates[1].id == "Child 5") {
                    old.subStates[3].subStates[0].subStates[1].data = old.updateDataModel(SampleStateModel())
                }
                return old
            }
        })

        val appStateAfterChange = store.getAppState()
        assertNotEquals(appStateBeforeChange, appStateAfterChange)
        assertEquals(appStateAfterChange.subStates[3].subStates[0].subStates[1].data, ObjectMapper().writeValueAsString(SampleStateModel()))
    }

    @Test
    fun inputJsonAppString_parseIt_searchForTarget_success() {

        val rootState = AppState(
            id = "RootState",
            isRoot = true,
            subStates = mutableListOf(
                AppState(id = "CounterState", data = ObjectMapper().writeValueAsString(CounterStateModel())),
                SearchResultState()
            )
        )
        val a = ObjectMapper().writeValueAsString(rootState)

        ObjectMapper().readValue<AppState>(a, AppState::class.java)?.let {
            val found = it.find("CounterState")
            if (found.id == "CounterState") {
                println(found)
            }
        }
    }


}
