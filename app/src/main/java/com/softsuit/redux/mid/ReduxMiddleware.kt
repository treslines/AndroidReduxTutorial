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
        middleIndex: Int,
        store: AppStore<AppState>
    ) {
        when (action) {
            is SearchingAction -> {
                // log this middleware individually
                store.reduce(LogMiddlewareAction(state.description, SearchingAction.description, LogOption.MID_BEFORE_CHANGE))
                // instruct store to move to the next state which should be "Searching"
                store.reduce(SearchingAction("Searching Event"))
                /** Simulation of "heavy" db search in background for learning purpuse only */
                Thread {
                    // keep in mind that appState in store is still "Searching" till this moment
                    val newState = action.reduce(state)
                    // instruct store to move to the next state which should be "Search Result"
                    store.reduce(SearchResultAction(newState.description, newState.data))
                    // log this middleware individually
                    store.reduce(LogMiddlewareAction(state.description, SearchingAction.description, LogOption.MID_AFTER_CHANGE))
                }.run()
                // you may call next here, if you think it is necessary, useful or your process should continue...
                // next(state, action, middles, middleIndex, store)
            }
            else -> next(state, action, middles, middleIndex, store)
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

/** This middleware is interested in DebugActions only */
class DebugMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        middles: List<Middleware<AppState>>,
        middleIndex: Int,
        store: AppStore<AppState>
    ) {
        when (action) {
            is DebugAction -> {
                store.reduce(action)
                next(state, action, middles, middleIndex, store)
            }
            else -> next(state, action, middles, middleIndex, store)
        }
    }
}


