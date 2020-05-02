package com.softsuit.redux.mid

/** state as a marker interface to enforce contract. */
interface State

/** every action implements this contract. it know how to reduce itself. */
interface Action<S : State> {
    fun reduce(old: S): S
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
class AppStore<S : State>(initialState: S, private val middles: List<Middleware<S>> = listOf()) : Store<S> {

    /** android component subscriptions. */
    private val subscribers = mutableSetOf<Subscriber<S>>()

    /** current and only app state tree. single source of truth. */
    private var appState: S = initialState
        set(value) {
            field = value
            // this is where the notification happens
            subscribers.forEach { it(value) }
        }

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        appState = action.reduce(appState)
        return appState
    }

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) = applyMiddleware(action)

    /** way android components subscribe to a state they are interested in */
    override fun subscribe(subscriber: Subscriber<S>) = subscribers.add(element = subscriber)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribe(subscriber: Subscriber<S>) = subscribers.remove(element = subscriber)

    /** the app's current state tree */
    override fun getAppState() = appState

    /** whenever dispatch is called, all registered middlewares get the chance to react to actions */
    private fun applyMiddleware(action: Action<S>) = middles[0].apply(appState, action, middles, 0, this)
}

/**
 * Each middleware should implements this contract. It is designed to work forward recursively.
 * It decides by itself if it consumes the action, if it generates another, if it forwards
 * to the next middleware in the chain or simply pass it over to the store. Middlewares are
 * triggered over the dispatch() method in the store only. Single point of entrance.
 */
interface Middleware<S : State> {
    /**
     * Call this method inside you middleware implementation whenever you
     * think that it makes sense. It is totally up to your business logic
     */
    fun next(
        state: S,
        action: Action<S>,
        middles: List<Middleware<S>>,
        middleIndex: Int,
        store: AppStore<S>
    ) {
        val nextIndex = middleIndex + 1
        if (isEndOfChain(nextIndex, middles)) {
            store.reduce(action)
        } else {
            middles[nextIndex].apply(state, action, middles, nextIndex, store)
        }
    }

    /**
     * You should not call this method by yourself. This method gets called
     * automatically from the store whenever its dispatch() method is called
     */
    fun apply(
        state: S,
        action: Action<S>,
        middles: List<Middleware<S>>,
        middlesIndex: Int,
        store: AppStore<S>
    )

    /**
     * You should not call this method by yourself. It was designed to be used by
     * next() method to decide if the middleware reached the end of the chain or not.
     */
    private fun isEndOfChain(nextIndex: Int, middles: List<Middleware<S>>): Boolean {
        return nextIndex == middles.size
    }
}




