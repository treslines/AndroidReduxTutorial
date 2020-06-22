package com.softsuit.redux.example

import com.softsuit.redux.oo.Action
import com.softsuit.redux.oo.AppState
import com.softsuit.redux.oo.AppStore
import com.softsuit.redux.oo.Middleware
import java.lang.Thread.sleep

// +------------------------------------------------------------------------------------------------+
// | Middleware Implementations                                                                     |
// +------------------------------------------------------------------------------------------------+
/**
 * Imagine that this middleware performs some heavy weight db search tasks in background. The idea
 * here is to show how a middleware can perform different tasks causing a lot of side effects which
 * may change or may not change the current AppState.
 */
class SearchApiMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        store: AppStore<AppState>
    ) {
        when (action) {
            is SearchingAction -> {
                // ...
                // may do something here first...
                // ...
                /** Simulation of "heavy" db search in background for learning purpose only */
                Thread(Runnable {
                    // instruct store again to move now to state "Search Result" after this "loooong" task simulation is done!
                    sleep(1000 * 5)
                    store.reduce(action)
                }).start()
            }
            is SearchResultAction -> store.reduce(SearchResultAction("Search Result Event", "Ricardo"))
        }
    }
}
// +------------------------------------------------------------------------------------------------+
// | Util Middleware Implementations                                                                |
// +------------------------------------------------------------------------------------------------+
class DebugMiddleware : Middleware<AppState> {
    override fun apply(
        state: AppState,
        action: Action<AppState>,
        store: AppStore<AppState>
    ) {
        // Important: keep in mind that the actual AppState is not being changed here
        LogAction().reduce(state)
        when (action) {
            is DebugAction -> store.reduce(action)
        }
    }
}


