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
        //old.jsonData["CounterState"] = 0
        return AppState(id = "ResetCounterState", data = "LinkedHashMap(old.jsonData)")
    }

    companion object Id {
        val description = "Reset Counter Action"
    }
}

// WAY 2: assign return directly to method - very nice!
class DecrementCounterAction(val eventName: String) : Action<AppState> {
    // does the same as way 1 but in one liner
    override fun reduce(old: AppState): AppState {

        //when (old.jsonData["CounterState"]) {
        //    null -> old.jsonData["CounterState"] = 0.minus(1)
        //    else -> old.jsonData["CounterState"] = old.jsonData["CounterState"].toString().toInt().minus(1)
        //}

        return AppState(id = "DecrementCounterState", data = "LinkedHashMap(old.jsonData)")
    }

    companion object Id {
        val description = "Decrement Counter Action"
    }
}

// WAY 3: implement a typealias and assign it to the method! - also nice whenever needed!
class IncrementCounterAction(val eventName: String) : Action<AppState> {

    // reducer logic implemented here
    private val reducer: Reducer<AppState> = {

        //when (it.jsonData["CounterState"]) {
        //    null -> it.jsonData["CounterState"] = 0.plus(1)
        //    else -> it.jsonData["CounterState"] = it.jsonData["CounterState"].toString().toInt().plus(1)
        //}

        AppState(id = "IncrementCounterState", data = "LinkedHashMap(it.jsonData)")
    }

    // reducer result assigned to the method as return type
    override fun reduce(old: AppState) = reducer.invoke(old)

    companion object Id {
        val description = "Increment Counter Action"
    }
}

// ------------------------------------------------------------------------------------------------
// ---------------------------------- Middleware Actions ------------------------------------------
// ------------------------------------------------------------------------------------------------
//
// Side effects, api calls, db operations etc. - actions with delayed response
//
// ------------------------------------------------------------------------------------------------

class SearchingAction(private val eventDescription: String) : Action<AppState> {

    override fun reduce(old: AppState): AppState {
        //old.jsonData["SearchingState"] = true
        return AppState(id = "SearchingState", data = "LinkedHashMap(old.jsonData)")
    }

    companion object Id {
        val description = "Searching"
    }
}

class SearchResultAction(private val eventDescription: String, private val keywordToSearchFor: String) : Action<AppState> {

    override fun reduce(old: AppState): AppState {
        // simulating long search process in database
        Thread.sleep(1000 * 5)
        //old.jsonData["SearchResultState"] = keywordToSearchFor // imagine: here would be the real result
        return AppState(id = "SearchResultState", data = "LinkedHashMap(old.jsonData)")
    }

    companion object Id {
        val description = "Search Result"
    }
}

class SearchForKeywordAction(private val eventDescription: String, private val keyword: String) : Action<AppState> {

    override fun reduce(old: AppState): AppState {
        //old.jsonData["SearchForKeywordState"] = keyword
        return AppState(id = description, data = "LinkedHashMap(old.jsonData)")
    }

    companion object Id {
        val description = "Search For Keyword Action"
    }
}

class WaitingForUserInputAction(private val eventDescription: String) : Action<AppState> {

    override fun reduce(old: AppState): AppState {
        //old.jsonData["WaitingForUserInputState"] = true
        return AppState(id = "WaitingForUserInputState", data = "LinkedHashMap(old.jsonData)")
    }

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
        val stateName = it.child!!::class.java.simpleName
        //Log.d(stateName, it.jsonData[stateName].toString())
        AppState(id = description, data = "LinkedHashMap(it.jsonData)")
    }

    override fun reduce(old: AppState) = reducer(old)

    companion object Id {
        val description = "Debug Action"
    }
}

class LogAction() : Action<AppState> {

    private val reducer: Reducer<AppState> = {
        Log.d("logging", "AppData: ${it.id}")
        Log.d("logging", "AppData>Internal: ${it.child}")
        it
    }

    override fun reduce(old: AppState) = reducer(old)

    companion object Id {
        val description = "Log Action"
    }
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

    companion object Id {
        val description = "Log Middleware Action"
    }
}

enum class LogOption {
    MID_BEFORE_CHANGE, MID_AFTER_CHANGE, APP_BEFORE_CHANGE, APP_AFTER_CHANGE // add other option as they emerge...
}