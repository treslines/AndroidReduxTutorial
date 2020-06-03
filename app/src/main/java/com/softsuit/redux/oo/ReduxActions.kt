package com.softsuit.redux.oo

import android.util.Log

// ------------------------------------------------------------------------------------------------
// UI Actions
// ------------------------------------------------------------------------------------------------
//
// Trivial UI actions - Direct user actions with immediately response
// You are gonna see 3 ways you could do the same thing in kotlin but in slightly different ways
//
// ------------------------------------------------------------------------------------------------

// WAY 1: most programmers are used to. just implement the method's signature, no magic!
class ResetCounterAction(val eventName: String) : Action<AppState> {
    override fun reduce(old: AppState): AppState {
        if (old.hasData()) {
            old.getDataModel(CounterStateModel::class.java)?.run {
                counter = 0
                old.data = old.toDataModelJsonString(this)
            }
            return old
        }
        return old
    }
    companion object Id {
        val description = "ResetCounterAction"
    }
}

class DecrementCounterAction(val eventName: String) : Action<AppState> {
    // does the same as way 1 but in one liner
    override fun reduce(old: AppState): AppState {
        if (old.hasData()) {
            old.getDataModel(CounterStateModel::class.java)?.run {
                counter--
                old.data = old.toDataModelJsonString(this)
            }
            return old
        }
        return old
    }
    companion object Id {
        val description = "DecrementCounterAction"
    }
}

// WAY 2: implement a typealias and assign it to the method! - also nice whenever needed!
class IncrementCounterAction(val eventName: String) : Action<AppState> {
    // reducer logic implemented here or outside this class in another file. Ex: counter reducer file
    private val reducer: Reducer<AppState> = {
        if (it.hasData()) {
            it.getDataModel(CounterStateModel::class.java)?.run {
                counter--
                it.data = it.toDataModelJsonString(this)
            }
        }
        it
    }
    // reducer result assigned to the method as return type
    override fun reduce(old: AppState) = reducer.invoke(old)
    companion object Id {
        val description = "IncrementCounterAction"
    }
}

// ------------------------------------------------------------------------------------------------
// Middleware Actions
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
//  Util Actions
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

    override fun reduce(old: AppState) = reducer.invoke(old)
    companion object Id {
        val description = "DebugAction"
    }
}

class LogAction() : Action<AppState> {
    private val reducer: Reducer<AppState> = {
        Log.d("logging", "AppData: ${it.id}")
        Log.d("logging", "AppData>Internal: ${it.child}")
        it
    }

    override fun reduce(old: AppState) = reducer.invoke(old)
    companion object Id {
        val description = "LogAction"
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
        val description = "LogMiddlewareAction"
    }
}

enum class LogOption {
    MID_BEFORE_CHANGE, MID_AFTER_CHANGE, APP_BEFORE_CHANGE, APP_AFTER_CHANGE // add other option as they emerge...
}