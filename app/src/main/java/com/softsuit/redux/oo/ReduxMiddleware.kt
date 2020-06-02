package com.softsuit.redux.oo

// ------------------------------------------------------------------------------------------------
// Middleware Implementations
// ------------------------------------------------------------------------------------------------
//
// Api calls, Asyc tasks, Computations etc.
//
// ------------------------------------------------------------------------------------------------

/**
 * Imagine that this middleware performs some heavy weight db search tasks in background. The idea
 * here is to show how a middleware can perform different tasks causing a lot of side effects which
 * may change or may not change the current AppState.
 */
class SearchApiMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        chain: List<Middleware<AppState>>,
        chainIndex: Int,
        store: AppStore<AppState>
    ) {
        // simulating creation of a new action just for logging without changing AppState
        store.reduce(LogAction())

        // performing its work
        when (action) {
            is SearchingAction -> {
                // use another action to log states before and after change without actually changing the current AppState
                LogMiddlewareAction(state.id, SearchingAction.description, LogOption.MID_BEFORE_CHANGE).reduce(state)

                // instruct now store to move to the next state which should be "Searching for Keyword"
                // keep in mind that this will cause that all state subscribers are gonna be notified again, but since
                // nobody should be interested in an internal AppState, nothing is gonna happen. just be aware of it.
                val newAppState = store.reduce(SearchForKeywordAction("Search For Keyword Event", "Ricardo"))

                // log new state after change just for show case
                LogMiddlewareAction(newAppState.id, SearchForKeywordAction.description, LogOption.MID_AFTER_CHANGE).reduce(newAppState)

                /** Simulation of "heavy" db search in background for learning purpose only */
                Thread(Runnable {
                    // keep in mind that AppState in store changed now to "SearchingForKeywordState" and we have put the keyword
                    // we are looking for into this state's mapped data. So we are going to retrieve it for the next task.
                    val keywordToSearchFor = newAppState.data.toString()

                    // instruct store again to move now to state "Search Result" after this "loooong" task simulation is done!
                    // as soon as this "loooong" task is done, the store is going to notify all subscribers again and in the
                    // main activity the subscriber in bullet "3. register for SearchResultState" is going to react to it by
                    // showing a message toast on the screen, since it is interested in those state changes only.
                    store.reduce(SearchResultAction("Search Result Event", keywordToSearchFor))

                    // log this middleware individually
                    LogMiddlewareAction(state.id, SearchingAction.description, LogOption.MID_AFTER_CHANGE).reduce(state)
                }).start()

                // you may call next here, if you think it is necessary, useful or your process should continue...
                // in this show case, the middleware is just consuming the actions after all the work is done.
                // next(state, action, middles, middleIndex, store)
            }

            // just forward it to the next middleware in chain if no action was performed
            else -> next(state, action, chain, chainIndex, store)
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Util Middleware Implementations
// ------------------------------------------------------------------------------------------------
//
// Log, perform common tasks, analytics etc.
//
// ------------------------------------------------------------------------------------------------

/**
 * This middleware is interested in DebugActions only.
 * When dispatched it will log out the last current state only.
 * It will neither change the current state nor it will dispatch
 * other action. It will consume it and go no further.
 * */
class DebugMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        chain: List<Middleware<AppState>>,
        chainIndex: Int,
        store: AppStore<AppState>
    ) {
        // simulating creation of a new action just for logging purposes and to show
        // a core concept of redux which says that middleware may create another actions
        // consume it or may forward it or not to the next middleware in chain. In this
        // example it does create another action, than consume its incoming action and
        // does not forward it anywhere else.

        // Important: keep in mind that the actual AppState is not being changed here
        LogAction().reduce(state)
        when (action) {
            is DebugAction -> {
                // performs its incoming action, do not change appState and do
                // not forward it. Just consume it and done.
                action.reduce(state)
                // next(store.reduce(action), action, middles, middlesIndex, store)
            }
            // else -> next(state, action, middles, middlesIndex, store)
        }
    }
}


