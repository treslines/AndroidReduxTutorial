package com.softsuit.redux.mid

/** Redux Store dependency Injection to be used everywhere in app. This is the place where you setup you store.
 *  Everytime you implement a new middleware, that's the place to update it by adding it to the list.
 * */
object DI {
    private val middlewareChain: List<Middleware<AppState>> = listOf(SearchApiMiddleware(), DebugMiddleware())
    private val internalState = AppState("Internal App State")
    private val appStateTree = AppState("Redux Tutorial App", internalState)
    val reduxStore = AppStore(initialState = appStateTree, chain = middlewareChain)
}




