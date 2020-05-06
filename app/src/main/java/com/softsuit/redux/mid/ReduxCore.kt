package com.softsuit.redux.mid

/** state as a marker interface to enforce contract. */
interface State

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

/** pure function. any android component that wants to get notified if a specific state changes. */
typealias Subscriber <T> = (T) -> Unit

/** store implements this contract. */
interface Store<S : State> {
    fun reduce(action: Action<S>): S
    fun dispatch(action: Action<S>)

    fun addMultiStateObserver(observer: MultiStateObserver<S>): Boolean
    fun addSimpleStateObserver(observer: SimpleStateObserver<S>): Boolean
    fun addConditionalStateObserver(observer: ConditionalStateObserver<S>): Boolean

    fun removeMultiStateObserver(observer: MultiStateObserver<S>): Boolean
    fun removeSimpleStateObserver(observer: SimpleStateObserver<S>): Boolean
    fun removeConditionalStateObserver(observer: ConditionalStateObserver<S>): Boolean

    fun getAppState(): S
}

/** the app's state tree. In this case only a description and its data. */
open class AppState(open var description: String, var internal: AppState? = null, var data: MutableMap<String, Any> = mutableMapOf()) : State

/** represents the single source of truth in your andorid app. */
class AppStore<S : AppState>(initialState: S, private val chain: List<Middleware<S>> = listOf()) : Store<S> {

    /** android component subscriptions. */
    private val subscribers = mutableSetOf<Subscriber<S>>()

    private val conditionalStateObservers = mutableSetOf<ConditionalStateObserver<S>>()
    private val simpleStateObservers = mutableSetOf<SimpleStateObserver<S>>()
    private val multiStateObservers = mutableSetOf<MultiStateObserver<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = initialState
        // state change happens most of the time sequentially.
        // Synchronized just to be aware of middleware
        @Synchronized set(newState) {
            if (appState != newState) {
                field = newState

                // notify only observers that match the state and condition
                conditionalStateObservers.forEach {
                    if (newState::class.java.simpleName == it.observe()::class.java.simpleName) {
                        if (it.match().invoke(newState)) {
                            it.onChange(newState)
                        }
                    }
                }

                // notify only observers that match the state
                simpleStateObservers.forEach {
                    if (newState::class.java.simpleName == it.observe()::class.java.simpleName) {
                        it.onChange(newState)
                    }
                }

                // notify observers that match one of the states
                multiStateObservers.forEach { outter ->
                    outter.observe().forEach { inner ->
                        if (inner::class.java.simpleName == newState::class.java.simpleName) {
                            outter.onChange(newState)
                        }
                    }
                }

            }
        }

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        appState = action.reduce(appState)
        return appState
    }

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) = applyMiddleware(action)

    /** the app's current state tree */
    override fun getAppState() = appState

    /** whenever dispatch is called, all registered middlewares get the chance to react to actions */
    private fun applyMiddleware(action: Action<S>) = chain[0].apply(appState, action, chain, 0, this)

    /** way android components subscribe to a state they are interested in */
    override fun addMultiStateObserver(observer: MultiStateObserver<S>) = multiStateObservers.add(element = observer)
    override fun addSimpleStateObserver(observer: SimpleStateObserver<S>) = simpleStateObservers.add(element = observer)
    override fun addConditionalStateObserver(observer: ConditionalStateObserver<S>) = conditionalStateObservers.add(element = observer)

    /** whenever a component wants to unsubscribe */
    override fun removeMultiStateObserver(observer: MultiStateObserver<S>) = multiStateObservers.remove(element = observer)
    override fun removeSimpleStateObserver(observer: SimpleStateObserver<S>) = simpleStateObservers.remove(element = observer)
    override fun removeConditionalStateObserver(observer: ConditionalStateObserver<S>) = conditionalStateObservers.remove(element = observer)
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




