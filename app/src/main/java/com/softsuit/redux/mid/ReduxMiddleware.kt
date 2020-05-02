package com.softsuit.redux.mid

// ------------------------------------------------------------------------------------------------
// -------------------------------- Middleware Implementations ------------------------------------
// ------------------------------------------------------------------------------------------------
//
// Api calls, Asyc tasks, Computations etc.
//
// ------------------------------------------------------------------------------------------------

/** Imagine that this middleware performs some heavy weight db search tasks in background */
class SearchApiMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        middles: List<Middleware<AppState>>,
        middlesIndex: Int,
        store: AppStore<AppState>
    ) {
        store.reduce(LogAction()) // simulating creation of a new action just for logging
        when (action) {
            is SearchingAction -> {
                // log this middleware individually
                LogMiddlewareAction(state.description, SearchingAction.description, LogOption.MID_BEFORE_CHANGE).reduce(state)
                // instruct store to move to the next state which should be "Searching"
                store.reduce(SearchingAction("Searching Event"))
                /** Simulation of "heavy" db search in background for learning purpose only */
                Thread {
                    // keep in mind that appState in store is still "Searching" till this moment
                    val newState = store.reduce(SearchForKeywordAction("Search For Keyword Event", "Ricardo"))
                    // instruct store to move to the next state which should be "Search Result"
                    val result = newState.data["SearchForKeywordState"].toString()
                    store.reduce(SearchResultAction("Search Result Event", result))
                    // log this middleware individually
                    LogMiddlewareAction(state.description, SearchingAction.description, LogOption.MID_AFTER_CHANGE).reduce(state)
                }.run()
                // you may call next here, if you think it is necessary, useful or your process should continue...
                // next(state, action, middles, middleIndex, store)
            }
            else -> next(state, action, middles, middlesIndex, store)
        }
    }
}

// ------------------------------------------------------------------------------------------------
// ---------------------------- Util Middleware Implementations -----------------------------------
// ------------------------------------------------------------------------------------------------
//
// Log, perform common tasks, analytics etc.
//
// ------------------------------------------------------------------------------------------------

/** Simplest middleware implementation for learning purposes only */
class ExampleMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        middles: List<Middleware<AppState>>,
        middlesIndex: Int,
        store: AppStore<AppState>
    ) {
        // do something here... decide to call next or not
        next(state, action, middles, middlesIndex, store)
    }
}

/** This middleware is interested in DebugActions only */
class DebugMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        middles: List<Middleware<AppState>>,
        middlesIndex: Int,
        store: AppStore<AppState>
    ) {
        store.reduce(LogAction()) // simulating creation of a new action just for logging
        when (action) {
            is DebugAction -> {
                action.reduce(state)
                next(state, action, middles, middlesIndex, store)
            }
            else -> next(state, action, middles, middlesIndex, store)
        }
    }
}


