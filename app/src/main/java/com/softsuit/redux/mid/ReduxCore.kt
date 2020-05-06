package com.softsuit.redux.mid

/** state as a marker interface to enforce contract. */
interface State

/** every action implements this contract. it know how to reduce itself. */
interface Action<S : State> {
    fun reduce(old: S): S
}

interface SingleStateObserver<T, S> {
    fun triggerProperty(): T
    fun triggerState(): S
    fun stateChanged(state: S)
}

interface MultiStateObserver<T, S> {
    fun triggerStates(): List<S>
    fun stateChanged(state: S)
}

interface StatePropertyObserver<S> {
    fun triggerProperty(): Any
    fun triggerState(): S
    fun stateChanged(state: S)
}

/** pure function. used in conjunction(or not) with actions and subscribers reducing the state. */
typealias Reducer <T> = (T) -> T

/** pure function. any android component that wants to get notified if a specific state changes. */
typealias Subscriber <T> = (T) -> Unit

/** store implements this contract. */
interface Store<S : State> {
    fun reduce(action: Action<S>): S
    fun dispatch(action: Action<S>)
    fun subscribe(subscriber: Subscriber<S>): Boolean
    fun unsubscribe(subscriber: Subscriber<S>): Boolean
    fun getAppState(): S
}

/** the app's state tree. In this case only a description and its data. */
open class AppState(open var description: String, var internal: AppState? = null, var data: MutableMap<String, Any> = mutableMapOf()) : State

/** represents the single source of truth in your andorid app. */
class AppStore<S : AppState>(initialState: S, private val chain: List<Middleware<S>> = listOf()) : Store<S> {

    /** android component subscriptions. */
    private val subscribers = mutableSetOf<Subscriber<S>>()

    private val observers = mutableSetOf<StatePropertyObserver<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = initialState
        // state change happens most of the time sequentially.
        // Synchronized just to be aware of middleware
        @Synchronized set(newState) {
            if (appState != newState) {
                field = newState

                // this is where the notification happens
                subscribers.forEach { it(newState) }

                // notify only observers that match the state and property
                observers.forEach {
                    if (newState == it.triggerState()) {
                        if (it.triggerProperty() == newState.internal?.data?.get(it.triggerProperty()) ?: false) {
                            it.stateChanged(newState)
                        }
                    }
                }

                // notify only obersers interested in one or more states
                // TODO
            }
        }

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        appState = action.reduce(appState)
        return appState
    }

    public fun addObserver(observer: StatePropertyObserver<S>) = observers.add(element = observer)

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) = applyMiddleware(action)

    /** way android components subscribe to a state they are interested in */
    override fun subscribe(subscriber: Subscriber<S>) = subscribers.add(element = subscriber)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribe(subscriber: Subscriber<S>) = subscribers.remove(element = subscriber)

    /** the app's current state tree */
    override fun getAppState() = appState

    /** whenever dispatch is called, all registered middlewares get the chance to react to actions */
    private fun applyMiddleware(action: Action<S>) = chain[0].apply(appState, action, chain, 0, this)
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




