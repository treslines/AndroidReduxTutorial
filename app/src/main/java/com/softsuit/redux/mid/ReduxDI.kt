package com.softsuit.redux.mid

/** Redux Store dependency Injection to be used everywhere in app. This is the place where you setup you store.
 *  Everytime you implement a new middleware, that's the place to update it by adding it to the list.
 * */
object DI {
    private val middlewareChain: List<Middleware<AppState>> = listOf(
        SearchApiMiddleware(),
        DebugMiddleware()
    )
    private val rootState = AppState(id = "Redux Tutorial App", isRoot = true)
    val store = AppStore(initialState = rootState, chain = middlewareChain, logMode = true)
}




