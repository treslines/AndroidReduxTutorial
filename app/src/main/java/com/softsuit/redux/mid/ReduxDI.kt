package com.softsuit.redux.mid

/** Redux Store dependency Injection to be used everywhere in app. */
object DI {
    private val middles: List<Middleware<AppState>> = listOf(SearchApiMiddleware(), DebugMiddleware())
    val reduxStore = AppStore(initialState = AppInitialState(), middles = middles)
}




