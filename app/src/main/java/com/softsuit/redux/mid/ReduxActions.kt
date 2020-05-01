package com.softsuit.redux.mid

import android.util.Log

// ------------------------------------------------------------------------------------------------
// --------------------------------------- UI Actions ---------------------------------------------
// ------------------------------------------------------------------------------------------------
//
// Trivial UI actions - Direct user actions with immediately response
// You are gonna see 3 ways you could do the same thing in kotlin but in slightly different ways
//
// ------------------------------------------------------------------------------------------------

// WAY 1: most programmers are used to. just implement the method's signature, no magic!
class ResetCounterAction(val eventName: String) : Action<AppState> {
    override fun reduce(old: AppState): AppState {
        return AppState(description = "Reset Counter State", internal = ResetCounterState(), data = old.data.copy())
    }
}

// WAY 2: assign return type directly to method - very nice!
class DecrementCounterAction(val eventName: String) : Action<AppState> {
    // does the same as way 1 but in one liner
    override fun reduce(old: AppState) = AppState(description = "Decrement Counter State", internal = DecrementCounterState(), data = old.data.copy())
}

// WAY 3: implement a typealias and assign it to the method! - also nice whenever needed!
class IncrementCounterAction(val eventName: String) : Action<AppState> {

    // reducer logic implemented here
    private val reducer: Reducer<AppState> = {
        AppState(description = "Increment Counter State", internal = IncrementCounterState(), data = it.data.copy())
    }

    // reducer result assigned to the method as return type
    override fun reduce(old: AppState) = reducer.invoke(old)
}

// ------------------------------------------------------------------------------------------------
// ---------------------------------- Middleware Actions ------------------------------------------
// ------------------------------------------------------------------------------------------------
//
// Side effects, api calls, db operations etc. - actions with delayed response
//
// ------------------------------------------------------------------------------------------------

class SearchingAction(private val eventDescription: String) : Action<AppState> {

    override fun reduce(old: AppState) = AppState(description, SearchingState(), old.data.copy())

    companion object Id {
        val description = "Searching"
    }
}

class SearchResultAction(private val eventDescription: String, private val resultData: Data) :
    Action<AppState> {

    override fun reduce(old: AppState): AppState {
        // simulating long search process
        Thread.sleep(1000 * 5)
        return AppState(description, SearchResultState(), resultData)
    }

    companion object Id {
        val description = "Search Result"
    }
}

class SearchForKeywordAction(private val eventDescription: String) : Action<AppState> {

    override fun reduce(old: AppState) = AppState(description, SearchForKeywordState(), old.data.copy())

    companion object Id {
        val description = "Search For Keyword Action"
    }
}

class WaitingForUserInputAction(private val eventDescription: String) : Action<AppState> {

    override fun reduce(old: AppState) = AppState(description, WaitingForUserInputState(), old.data.copy())

    companion object Id {
        val description = "WaitingForUserInputAction"
    }
}

// ------------------------------------------------------------------------------------------------
// ------------------------------------- Util Actions ---------------------------------------------
// ------------------------------------------------------------------------------------------------
//
// log, debug, perform common tasks, analytics etc.
//
// ------------------------------------------------------------------------------------------------

class DebugAction() : Action<AppState> {

    private val reducer: Reducer<AppState> = {
        when (it.description) {
            "Searching" -> {
                Log.d("debugging", it.data.toString())
            }
        }
        it
    }

    override fun reduce(old: AppState) = reducer(old)
}

class LogMiddlewareAction(val stateDesc: String, val actionDesc: String, val option: LogOption) : Action<AppState> {

    override fun reduce(old: AppState): AppState {
        when (option) {
            LogOption.MID_BEFORE_CHANGE -> Log.d("mid", "state <-- $stateDesc, action IN <-- $actionDesc")
            LogOption.MID_AFTER_CHANGE -> Log.d("mid", "state --> $stateDesc, action OUT --> $actionDesc")
            LogOption.APP_BEFORE_CHANGE -> Log.d("app", "state <-- $stateDesc, action IN <-- $actionDesc")
            LogOption.APP_AFTER_CHANGE -> Log.d("app", "state --> $stateDesc, action OUT --> $actionDesc")
        }
        return old
    }
}

enum class LogOption {
    MID_BEFORE_CHANGE, MID_AFTER_CHANGE, APP_BEFORE_CHANGE, APP_AFTER_CHANGE // add other option as they emerge...
}