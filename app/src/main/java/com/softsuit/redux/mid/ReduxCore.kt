package com.softsuit.redux.mid

import android.util.Log
import com.google.gson.Gson
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

    fun subscribeMultiState(observer: MultiStateObserver<S>): Boolean
    fun subscribeSimpleState(observer: SimpleStateObserver<S>): Boolean
    fun subscribeConditionalState(observer: ConditionStateObserver<S>): Boolean

    fun unsubscribeMultiState(observer: MultiStateObserver<S>): Boolean
    fun unsubscribeSimpleState(observer: SimpleStateObserver<S>): Boolean
    fun unsubscribeConditionalState(observer: ConditionStateObserver<S>): Boolean

    fun lookUp(state: S): S
    fun getAppState(): S
    fun getDeepCopy(source: S, destination: S): S

    /** for traceability and debugging only */
    fun getStateName(): String

    fun isLogModeOn(): Boolean
    fun setLogMode(onOff: Boolean)
}

/** the app's state tree in a serializable manner (easier to store and re-store it) */
open class AppState(
    var id: String,
    var data: String? = null,
    var children: MutableList<AppState> = mutableListOf(),
    var isRoot: Boolean = false
) : State {
    fun hasData(): Boolean = data != null
    fun hasChildren(): Boolean = children.isNotEmpty()
    /**
     * each subscriber knows which state it subscribes for, so it can
     * retrieve the right data model from the state as soon as it gets notified
     */
    fun <T> getData(modelType: Class<T>): T? = Gson().fromJson(Gson().toJson(data).toString(), modelType)
}

/** state returned by lookUpFor(state) if not match found */
class EmptyState() : AppState(id = "EmptyState")

/** represents the single source of truth in your andorid app. */
class AppStore<S : AppState>(initialState: S, private val chain: List<Middleware<S>> = listOf(), private var logMode: Boolean = false) : Store<S> {

    /** android component subscriptions. */
    private val conditionStateObservers = mutableSetOf<ConditionStateObserver<S>>()
    private val simpleStateObservers = mutableSetOf<SimpleStateObserver<S>>()
    private val multiStateObservers = mutableSetOf<MultiStateObserver<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = initialState
        // state change happens most of the time sequentially. Synchronized just to be aware
        // of middleware asynchronous tasks that could potentially arrive at the same time
        @Synchronized set(state) {
            if (hasStateChanged(state)) {
                updateDeep(state)
                field = appState
                if (appState.hasChildren()) {
                    appState.children.forEach { child ->
                        child?.let {
                            println(it.id)
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
        if (state.hasChildren()) {
            state.children.forEach { child ->
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
                if (hasSameName(state, inner) || hasSameId(state, inner)) {
                    val copied = getDeepCopy(state as S, AppState("EmptyState") as S)
                    outer.onChange(copied)
                    break // if matched, no need to keep running, go directly to next "outer" observer
                }
            }
        }
    }

    private fun notifySimpleStateSubscribers(state: AppState) {
        simpleStateObservers.forEach {
            if (hasSameName(state, it.observe()) || hasSameId(state, it.observe())) {
                val copied = getDeepCopy(state as S, AppState("EmptyState") as S)
                it.onChange(copied)
            }
        }
    }

    private fun notifyConditionalStateSubscribers(state: AppState) {
        conditionStateObservers.forEach {
            if (hasSameName(state, it.observe()) || hasSameId(state, it.observe())) {
                val copied = getDeepCopy(state as S, AppState("EmptyState") as S)
                if (it.match().invoke(copied)) {
                    it.onChange(copied)
                }
            }
        }
    }

    private fun hasSameName(left: AppState, right: AppState) = left::class.java.name == right::class.java.name
    private fun hasSameId(left: AppState, right: AppState) = left.id == right.id

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        appState = action.reduce(getAppState())
        return getDeepCopy(appState, EmptyState() as S)
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
    override fun getAppState() = getDeepCopy(appState, AppState("EmptyState") as S)

    /** way android components subscribe to a state they are interested in */
    override fun subscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.add(element = observer)

    override fun subscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.add(element = observer)
    override fun subscribeConditionalState(observer: ConditionStateObserver<S>) = conditionStateObservers.add(element = observer)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.remove(element = observer)

    override fun unsubscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.remove(element = observer)
    override fun unsubscribeConditionalState(observer: ConditionStateObserver<S>) = conditionStateObservers.remove(element = observer)

    /** kotlin's copy method by data classes are only shallow copies and do not support deep copies */
    override fun getDeepCopy(source: S, destination: S): S {
        copyDeep(source, destination)
        return destination
    }

    private fun assignDeep(toAssign: S, appState: S) {
        appState.id = toAssign.id
        appState.isRoot = toAssign.isRoot

        if (toAssign.hasData()) {
            appState.data = toAssign.data
        }
        if (toAssign.hasChildren()) {
            // check if contains not, if not, add

            var updated = false
            // else just update values
            toAssign.children.forEach {
                updated = false
                appState.children.forEach { inner ->
                    if (it.id == inner.id) {
                        updated = true
                        assignDeep(it as S, inner as S)
                    }
                }
                if (!updated) {
                    appState.children.add(it)
                }
            }
        }
    }

    private fun copyDeep(original: S, copy: S) {
        copy.id = original.id
        copy.isRoot = original.isRoot

        if (original.hasData()) {
            copy.data = original.data
        }
        if (original.hasChildren()) {
            original.children.forEach {
                val empty = AppState("EmptyState")
                copy.children.add(empty)
                copyDeep(it as S, empty as S)
            }
        }
    }

    // TODO: test it
    fun hasStateChanged(incoming: S): Boolean {
        match.clear()
        return isNotDeepEquals(appState, incoming)
    }

    var match = mutableListOf<Boolean>()
    fun isNotDeepEquals(state: S, incoming: S): Boolean {
        val noEquals = (
                state.id != incoming.id ||
                        state.isRoot != incoming.isRoot ||
                        state.hasData() != incoming.hasData() ||
                        state.data != incoming.data ||
                        state.hasChildren() != incoming.hasChildren() ||
                        state.children.size != incoming.children.size
                )
        when (noEquals) {
            true -> {
                match.add(true)
                return noEquals
            }
            else -> {
                if (incoming.hasChildren()) {
                    if (match.isNotEmpty()) return match[0]
                    incoming.children.forEachIndexed { index, outer ->
                        if (match.isNotEmpty()) return match[0]
                        isNotDeepEquals(outer as S, state.children[index] as S)
                    }
                }
            }
        }
        if (match.isNotEmpty()) return match[0]
        return noEquals
    }

    fun isDeepEquals(state: S, incoming: S): Boolean {
        match.clear()
        return !isNotDeepEquals(state, incoming)
    }

    // TODO: test it
    private fun updateDeep(toUpdate: S) {
        assignDeep(toUpdate, appState) // update references reusing deepCopy
    }

    /** when your app depends on other state, lookup for it in the app state tree and return a copy of it or an EmptyState */
    override fun lookUp(state: S): S {
        traverseEnd.clear()
        return when (val found = traverse(appState, state::class.java.name)) {
            is EmptyState -> found
            else -> getDeepCopy(found, EmptyState() as S)
        }
    }

    fun lookUp(stateId: String): S {
        traverseEnd.clear()
        val found = traverse(appState, stateId)
        return when (found.id) {
            stateId -> getDeepCopy(found, EmptyState() as S)
            else -> found
        }
    }

    /** look up for a specific state in app state tree and return a reference to it or an EmptyState */
    private fun lookUpReference(state: S): S {
        traverseEnd.clear()
        return traverse(appState, state::class.java.name)
    }

    /**
     * traverses the whole state tree returning the matched state
     * with its underlying states if any or an EmptyState
     */
    private val traverseEnd = mutableListOf<S>()
    private fun traverse(state: S, name: String): S {
        if (state::class.java.name == name || state.id == name) {
            traverseEnd.add(state)
            return state
        } else if (state.hasChildren()) {
            state.children.forEach {
                if (traverseEnd.isNotEmpty()) return traverseEnd[0]
                traverse(it as S, name)
            }
        }
        if (traverseEnd.isNotEmpty()) return traverseEnd[0]
        return EmptyState() as S
    }

    override fun getStateName(): String = appState::class.java.name
    override fun isLogModeOn() = logMode
    override fun setLogMode(onOff: Boolean) {
        logMode = onOff
    }
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
                Log.i("ReduxCore", "Store reduced action=${action.getName()}")
            }
        } else {
            chain[nextIndex].apply(state, action, chain, nextIndex, store).also {
                if (store.isLogModeOn()) {
                    Log.i("ReduxCore", "Middleware=${chain[nextIndex].getName()} dispatched state=${store.getStateName()} with action=${action.getName()}")
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




