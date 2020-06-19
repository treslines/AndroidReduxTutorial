package com.softsuit.redux.oo

import com.fasterxml.jackson.databind.ObjectMapper
import com.softsuit.redux.example.CounterStateModel
import com.softsuit.redux.example.DebugMiddleware
import com.softsuit.redux.example.SearchApiMiddleware
import com.softsuit.redux.example.SearchResultState

/** Redux Store dependency Injection to be used everywhere in app. This is the place where you setup you store.
 *  Everytime you implement a new middleware, that's the place to update it by adding it to the list.
 * */
object DI {
    // Central place to manage your middlewares and initial states
    private val middlewareChain: List<Middleware<AppState>> = listOf(
        SearchApiMiddleware(),
        DebugMiddleware()
    )
    private val rootState = AppState(
        id = "RootState",
        isRoot = true,
        subStates = mutableListOf(
            AppState(id = "CounterState", data = ObjectMapper().writeValueAsString(CounterStateModel())),
            SearchResultState()
        )
    )
    val store = AppStore(initialState = rootState, chain = middlewareChain, logMode = true)
}




