package com.softsuit.redux.example

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.softsuit.redux.oo.Action
import com.softsuit.redux.oo.AppState
import com.softsuit.redux.oo.Reducer

// +------------------------------------------------------------------------------------------------+
// | Simple Actions, transitions, navigation, clicks etc.                                           |
// +------------------------------------------------------------------------------------------------+
class ResetCounterAction(val eventName: String) : Action<AppState> {
    override fun reduce(old: AppState): AppState {
        val state = old.find("CounterState")
        if (state.hasData()) {
            state.getDataModel(CounterStateModel::class.java, state.data)?.run {
                counter = 0
                state.data = ObjectMapper().writeValueAsString(this)
                old.data = old.getDataModelString(state)
            }
            return old
        }
        return old
    }
}
class DecrementCounterAction(val eventName: String) : Action<AppState> {
    override fun reduce(old: AppState): AppState {
        val state = old.find("CounterState")
        if (state.hasData()) {
            state.getDataModel(CounterStateModel::class.java, state.data)?.run {
                counter--
                state.data = ObjectMapper().writeValueAsString(this)
                old.insertOrUpdate(state)
            }
            return old
        }
        return old
    }
}
class IncrementCounterAction(val eventName: String) : Action<AppState> {
    private val reducer: Reducer<AppState> = {
        val state = it.find("CounterState")
        if (state.hasData()) {
            it.getDataModel(CounterStateModel::class.java, state.data)?.run {
                counter++
                state.data = ObjectMapper().writeValueAsString(this)
                it.insertOrUpdate(state)
            }
        }
        it
    }
    // reducer result assigned to the method as return type
    override fun reduce(old: AppState) = reducer.invoke(old)
}

// +------------------------------------------------------------------------------------------------+
// | Middleware Actions: Side effects, api calls, db operations etc. actions with delayed response  |
// +------------------------------------------------------------------------------------------------+
class SearchingAction(private val eventDescription: String) : Action<AppState> {
    override fun reduce(old: AppState): AppState {
        //old.jsonData["SearchingState"] = true
        return AppState(id = "SearchingState", data = "LinkedHashMap(old.jsonData)")
    }
    companion object Id {
        val description = "Searching"
    }
}

class SearchResultAction(private val eventDescription: String, private val keywordToSearchFor: String) :
    Action<AppState> {
    override fun reduce(old: AppState): AppState {
        // simulating long search process in database
        Thread.sleep(1000 * 5)
        //old.jsonData["SearchResultState"] = keywordToSearchFor // imagine: here would be the real result
        return AppState(id = "SearchResultState", data = "LinkedHashMap(old.jsonData)")
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
}

// +------------------------------------------------------------------------------------------------+
// | Simple Actions: log, debug, perform common tasks, analytics etc.                               |
// +------------------------------------------------------------------------------------------------+
class DebugAction : Action<AppState> {
    private val reducer: Reducer<AppState> = {
        val stateName = it.subStates!!::class.java.simpleName
        AppState(id = description, data = "LinkedHashMap(it.jsonData)")
    }
    override fun reduce(old: AppState) = reducer.invoke(old)
    companion object Id {
        val description = "DebugAction"
    }
}

class LogAction : Action<AppState> {
    private val reducer: Reducer<AppState> = {
        Log.d("logging", "AppData: ${it.id}")
        Log.d("logging", "AppData>Internal: ${it.subStates}")
        it
    }
    override fun reduce(old: AppState) = reducer.invoke(old)
}

class LogMiddlewareAction(val stateDesc: String, val actionDesc: String, val option: LogOption) :
    Action<AppState> {
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