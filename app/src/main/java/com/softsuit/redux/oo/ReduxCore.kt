package com.softsuit.redux.oo

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.Serializable

/** state as a marker interface to enforce contract. */
interface State : Serializable

/** pure function. used in conjunction(or not) with actions and subscribers reducing the state. */
typealias Reducer <T> = (T) -> T

/** every action implements this contract. it know how to reduce itself. */
interface Action<S : State> {
    /** place where android components places its reducers */
    fun reduce(old: S): S
    /** for traceability and debugging only */
    fun getName(): String = this::class.java.name
}

/** Registration for components interested simply on a state change */
interface SimpleStateObserver<S : State> {
    /** state you are registering for */
    fun observe(): S
    /** place where the android component react to change */
    fun onChange(state: S)
}

/** for components interested in one or more state changes. If one OR another state changes, it gets notified */
interface MultiStateObserver<S : State> {
    /** states you are registering for */
    fun observe(): List<S>
    /** place where the android component reacts to change */
    fun onChange(state: S)
}

/** used while registering conditional observers */
typealias ConditionReducer <T> = (T) -> Boolean

/** for components interested in one specific state change and any condition of it. The state must match AND the property name */
interface ConditionStateObserver<S : State> {
    /** any condition you want to match besides state change. Property, name, id, string whatever you need */
    fun match(): ConditionReducer<S>
    /** state you are registering for */
    fun observe(): S
    /** place where the android component reacts to change */
    fun onChange(state: S)
}

/** store implements this contract. */
interface Store<S : State> {
    fun reduce(action: Action<S>): S
    fun dispatch(action: Action<S>)

    fun subscribe(observer: MultiStateObserver<S>): Boolean
    fun subscribe(observer: SimpleStateObserver<S>): Boolean
    fun subscribe(observer: ConditionStateObserver<S>): Boolean

    fun unsubscribe(observer: MultiStateObserver<S>): Boolean
    fun unsubscribe(observer: SimpleStateObserver<S>): Boolean
    fun unsubscribe(observer: ConditionStateObserver<S>): Boolean

    fun getAppState(): S

    /** for traceability and debugging only */
    fun getStateName(): String
    fun isLogModeOn(): Boolean
    fun setLogMode(onOff: Boolean)
}

/** the app's state tree in a serializable manner (easier to store and re-store it) */
open class AppState(
    var id: String = "EmptyState",
    var data: String = "",
    var subStates: MutableList<AppState> = mutableListOf(),
    var isRoot: Boolean = false
) : State {
    fun hasData(): Boolean = data.isNotEmpty()
    fun hasSubStates(): Boolean = subStates.isNotEmpty()
    override fun toString(): String = ObjectMapper().writeValueAsString(this)
    /**
     * each subscriber knows which state it subscribes for, so it can
     * retrieve the right data model from the state as soon as it gets notified
     */
    fun <T> getDataModel(modelType: Class<T>, data: String): T? {
        return try {
            ObjectMapper().readValue(data, modelType)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> toDataModelJsonString(model: T): String {
        return try {
            ObjectMapper().writeValueAsString(model)
        } catch (e: Exception) {
            ""
        }
    }

    fun getStateFromString(stateString: String): AppState = ObjectMapper().readValue(stateString, AppState::class.java)

    fun insertOrUpdate(toUpdate: AppState): String {
        val actualState = this.toString()
        return if (actualState.contains(toUpdate.id)) {
            // fast update by replacing whole object
            val appStateString = this.toString()
            val placeholder = appStateString.replace(find(toUpdate.id).toString(), "@ph@")
            placeholder.replace("@ph@", ObjectMapper().writeValueAsString(toUpdate))
        } else {
            // add
            val appStateString = this.toString()
            val target = find(toUpdate.id).toString()
            val placeholder = appStateString.replace(target, "@ph@")
            val targetPlaceholder = target.replaceFirst("[", "[@ph@")
            val inserted = targetPlaceholder.replace("@ph@", "${toUpdate.toString()},")
            val updated = placeholder.replace("@ph@", inserted)
            return updated.replace(",]", "]")
        }
    }

    fun remove(toRemove: AppState): String {
        val appStateString = this.toString()
        return if (appStateString.contains(toRemove.id)) {
            val found = find(toRemove.id).toString()
            if (appStateString.replace(found, "").length != appStateString.length) {
                // object to remove is identical, just remove it
                appStateString.replace(ObjectMapper().writeValueAsString(toRemove), "").replace(",,", ",")
            } else {
                // object exists but is not equals
                find(toRemove.id).toString()
            }
        } else {
            // nothing to remove
            appStateString
        }
    }

    fun find(id: String): AppState {
        if (id == this.id) {
            return this
        }
        while (this.hasSubStates()) {
            for (state in this.subStates) {
                if (id == state.id) {
                    return state
                }
            }
        }
        while (this.hasSubStates()) {
            for (state in this.subStates) {
                state.find(id)
            }
        }
        return AppState()
    }

}

/** represents the single source of truth in your andorid app. */
class AppStore<S : AppState>(initialState: S, private val chain: List<Middleware<S>> = listOf(), private var logMode: Boolean = false) : Store<S> {

    /** android component subscriptions. */
    private val conditionStateObservers = mutableSetOf<ConditionStateObserver<S>>()
    private val simpleStateObservers = mutableSetOf<SimpleStateObserver<S>>()
    private val multiStateObservers = mutableSetOf<MultiStateObserver<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = copyDeep(initialState)
        // state change happens most of the time sequentially. Synchronized just to be aware
        // of middleware asynchronous tasks that could potentially arrive at the same time
        @Synchronized set(state) {
            if (hasChanged(state)) {
                field = ObjectMapper().readValue(state.toString(), AppState::class.java) as S
                if (appState.hasSubStates()) {
                    appState.subStates.forEach { child ->
                        child?.let {
                            println(it.id) // TODO: remove it after tests!
                            notifySubscribers(it as S)
                        }
                    }
                }
            }
        }

    private fun notifySubscribers(state: S) {
        notifyMultiStateSubscribers(state)
        notifySimpleStateSubscribers(state)
        notifyConditionalStateSubscribers(state)
        if (state.hasSubStates()) {
            state.subStates.forEach { child ->
                child?.let { it ->
                    println(it.id)
                    notifySubscribers(it as S)
                }
            }
        }
    }

    private fun notifyMultiStateSubscribers(state: AppState) {
        multiStateObservers.forEach { outer ->
            for (inner in outer.observe()) {
                if (hasSameId(state, inner)) {
                    outer.onChange(copyDeep(state as S))
                    break // if matched, no need to keep running, go directly to next "outer" observer
                }
            }
        }
    }

    private fun notifySimpleStateSubscribers(state: AppState) {
        simpleStateObservers.forEach {
            if (hasSameId(state, it.observe())) {
                it.onChange(copyDeep(state as S))
            }
        }
    }

    private fun notifyConditionalStateSubscribers(state: AppState) {
        conditionStateObservers.forEach {
            if (hasSameId(state, it.observe())) {
                val copied = copyDeep(state as S)
                if (it.match().invoke(copied)) {
                    it.onChange(copied)
                }
            }
        }
    }

    private fun hasSameId(left: AppState, right: AppState) = left.id == right.id

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        appState = action.reduce(getAppState())
        return copyDeep(appState)
    }

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) {
        if (chain != null && chain.isNotEmpty()) {
            chain[0].apply(getAppState(), action, chain, 0, this)
        } else {
            reduce(action)
        }
    }

    /** the app's current state tree */
    override fun getAppState() = copyDeep(appState)

    /** way android components subscribe to a state they are interested in */
    override fun subscribe(observer: MultiStateObserver<S>) = multiStateObservers.add(element = observer)

    override fun subscribe(observer: SimpleStateObserver<S>) = simpleStateObservers.add(element = observer)
    override fun subscribe(observer: ConditionStateObserver<S>) = conditionStateObservers.add(element = observer)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribe(observer: MultiStateObserver<S>) = multiStateObservers.remove(element = observer)

    override fun unsubscribe(observer: SimpleStateObserver<S>) = simpleStateObservers.remove(element = observer)
    override fun unsubscribe(observer: ConditionStateObserver<S>) = conditionStateObservers.remove(element = observer)

    /** kotlin's copy method by data classes are only shallow copies and do not support deep copies */
    private fun copyDeep(toCopy: S): S = ObjectMapper().readValue(toCopy.toString(), AppState::class.java) as S

    fun isDeepEquals(incoming: S) = !hasChanged(incoming)

    /** return empty state if no match found */
    fun lookUpBy(state: S): S {
        return appState.find(state.id) as S
    }

    override fun getStateName(): String = appState.id
    override fun isLogModeOn() = logMode
    override fun setLogMode(onOff: Boolean) {
        logMode = onOff
    }

    private fun hasChanged(state: AppState): Boolean = state.toString() != appState.toString()

}

/**
 * Each middleware should implements this contract. It is designed to work forward recursively.
 * It decides by itself if it consumes the action, if it generates another, if it forwards
 * to the next middleware in the chain or simply pass it over to the store. Middlewares are
 * triggered over the dispatch() method in the store only. Single point of entrance.
 */
interface Middleware<S : AppState> {
    /**
     * Call this method inside you middleware implementation whenever you
     * think that it makes sense. It is totally up to your business logic
     */
    fun next(
        state: S,
        action: Action<S>,
        chain: List<Middleware<S>>,
        chainIndex: Int,
        store: AppStore<S>
    ) {
        val nextIndex = chainIndex + 1
        if (isEndOfChain(nextIndex, chain)) {
            store.reduce(action).also {
                if (store.isLogModeOn()) {
                    Log.i("Redux", "Store reduced action=${action.getName()}")
                }
            }
        } else {
            chain[nextIndex].apply(state, action, chain, nextIndex, store).also {
                if (store.isLogModeOn()) {
                    Log.i("Redux", "Middleware=${chain[nextIndex].getName()} dispatched state=${store.getStateName()} with action=${action.getName()}")
                }
            }
        }
    }

    /**
     * You should not call this method by yourself. This method gets called
     * automatically from the store whenever its dispatch() method is called
     */
    fun apply(
        state: S,
        action: Action<S>,
        chain: List<Middleware<S>>,
        chainIndex: Int,
        store: AppStore<S>
    )

    /**
     * You should not call this method by yourself. It was designed to be used by
     * next() method to decide if the middleware reached the end of the chain or not.
     */
    private fun isEndOfChain(nextIndex: Int, chain: List<Middleware<S>>) = nextIndex == chain.size

    /** for traceability and debugging only */
    fun getName(): String = this::class.java.name
}




