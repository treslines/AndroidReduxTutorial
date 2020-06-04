package com.softsuit.redux.oo

import com.google.gson.Gson

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
        child = mutableListOf(AppState(id = "CounterState", data = Gson().toJson(CounterStateModel())), SearchResultState())
    )
    val store = AppStore(initialState = rootState, chain = middlewareChain, logMode = true)
}




