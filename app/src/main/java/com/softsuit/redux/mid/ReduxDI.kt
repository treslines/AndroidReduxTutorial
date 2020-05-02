package com.softsuit.redux.mid

/** Redux Store dependency Injection to be used everywhere in app. This is the place where you setup you store.
 *  Everytime you implement a new middleware, that's the place to update it by adding it to the list.
 * */
object DI {
    private val middles: List<Middleware<AppState>> = listOf(SearchApiMiddleware(), DebugMiddleware())
    private val internalState = AppState("App Internal State")
    private val appStateTree = AppState("Initial State", internalState)
    val reduxStore = AppStore(initialState = appStateTree, middles = middles)
}




