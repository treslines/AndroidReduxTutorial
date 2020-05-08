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

/** used while registering conditonal obersers */
typealias ConditionReducer <T> = (T) -> Boolean

/** for components interested in one specific state change and any condition of it. The state must match AND the property name */
interface ConditionalStateObserver<S : State> {
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
    fun subscribeConditionalState(observer: ConditionalStateObserver<S>): Boolean

    fun unsubscribeMultiState(observer: MultiStateObserver<S>): Boolean
    fun unsubscribeSimpleState(observer: SimpleStateObserver<S>): Boolean
    fun unsubscribeConditionalState(observer: ConditionalStateObserver<S>): Boolean

    fun getAppState(): S
    fun lookUpFor(state: S): S
    fun getDeepCopy(source: S, destination: S): S
}

/** the app's state tree. In this case only a description and its data. */
open class AppState(
    var id: String,
    var data: String? = null,
    var dataType: String? = null,
    var child: AppState? = null,
    var hasChild: Boolean = child != null,
    var isRoot: Boolean = false,
    var hasData: Boolean = data != null
) : State {

    fun getData(): Any? {
        dataType?.let {
            try {
                return Gson().fromJson(Gson().toJson(data).toString(), Class.forName(it))
            } catch (e: Exception) {
                Log.i("ReduxCore", "Specified data type: $it does not exist or has a wrong class path!")
            }
        }
        return null
    }
}

/** state returned by lookUpForState() if not match found */
class EmptyState() : AppState(id = "empty")

/** represents the single source of truth in your andorid app. */
class AppStore<S : AppState>(initialState: S, private val chain: List<Middleware<S>> = listOf()) : Store<S> {

    /** android component subscriptions. */
    private val conditionalStateObservers = mutableSetOf<ConditionalStateObserver<S>>()
    private val simpleStateObservers = mutableSetOf<SimpleStateObserver<S>>()
    private val multiStateObservers = mutableSetOf<MultiStateObserver<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = initialState
        // state change happens most of the time sequentially. Synchronized just to be aware
        // of middleware asynchronous tasks that could potentially arrive at the same time
        @Synchronized set(state) {
            if (appState != state) {
                field = state

                appState.child?.let { child ->
                    // notify only observers that match the state and condition
                    conditionalStateObservers.forEach {
                        if (child::class.java.name == it.observe()::class.java.name) {
                            if (it.match().invoke(getAppState())) {
                                it.onChange(getAppState())
                            }
                        }
                    }

                    // notify only observers that match the state
                    simpleStateObservers.forEach {
                        if (child::class.java.name == it.observe()::class.java.name) {
                            it.onChange(getAppState())
                        }
                    }

                    // notify observers that match one of the states
                    multiStateObservers.forEach { outer ->
                        for (inner in outer.observe()) {
                            if (inner::class.java.name == child::class.java.name) {
                                outer.onChange(getAppState())
                                break // if matched, no need to keep running, go directly to next "outer" observer
                            }
                        }
                    }
                }

            }
        }

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        val reduced = action.reduce(getAppState())
        // prevent null AppState in case a reducer does something wrong (intentionally or not)
        reduced?.child?.let { appState = getDeepCopy(reduced, EmptyState() as S) }
        return reduced
    }

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) = chain[0].apply(getAppState(), action, chain, 0, this)

    /** the app's current state tree */
    override fun getAppState() = getDeepCopy(appState, EmptyState() as S)

    /** way android components subscribe to a state they are interested in */
    override fun subscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.add(element = observer)

    override fun subscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.add(element = observer)
    override fun subscribeConditionalState(observer: ConditionalStateObserver<S>) = conditionalStateObservers.add(element = observer)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.remove(element = observer)

    override fun unsubscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.remove(element = observer)
    override fun unsubscribeConditionalState(observer: ConditionalStateObserver<S>) = conditionalStateObservers.remove(element = observer)

    /** kotlin's copy method by data classes are only shallow copies and do not support deep copies */
    override fun getDeepCopy(source: S, destination: S): S {
        // this method is one of the most powerful method in the redux concept of immutability.
        // it avoids wrong usage by programmers and critical, unexpected side effects besides
        // better usability while reducing states. You do not have to worry about copying them anymore.
        copyDeep(source, destination)
        return destination
    }

    private fun copyDeep(original: S, copy: S) {
        copy.id = original.id
        copy.isRoot = original.isRoot
        copy.hasChild = original.hasChild
        if (original.hasData) {
            copy.data = original.data
            copy.dataType = original.dataType
        }
        if (original.hasChild) {
            copy.child = EmptyState()
            copyDeep(original.child as S, copy.child as S)
        }
    }

    /** when your app depends on other state, lookup for it in the app state tree */
    override fun lookUpFor(state: S): S {
        return traverse(appState, state::class.java.name)
    }

    private fun traverse(state: S, name: String): S {
        return when {
            state::class.java.name == name -> getDeepCopy(state, EmptyState() as S)
            state.hasChild -> traverse(state.child as S, name)
            else -> EmptyState() as S
        }
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
            store.reduce(action)
        } else {
            chain[nextIndex].apply(state, action, chain, nextIndex, store)
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
}




