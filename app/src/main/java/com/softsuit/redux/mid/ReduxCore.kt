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
    fun getDeepCopy(original: S): S
}

/** the app's state tree. In this case only a description and its data. */
open class AppState(
    var id: String,
    var data: MutableMap<String, Any> = mutableMapOf(),
    var child: AppState? = null
) : State

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

                // notify only observers that match the state and condition
                conditionalStateObservers.forEach {
                    if (appState.child!!::class.java.simpleName == it.observe()::class.java.simpleName) {
                        // getDeepCopy() ensures immutability
                        if (it.match().invoke(getAppState())) {
                            it.onChange(getAppState())
                        }
                    }
                }

                // notify only observers that match the state
                simpleStateObservers.forEach {
                    if (appState.child!!::class.java.simpleName == it.observe()::class.java.simpleName) {
                        // getDeepCopy() ensures immutability
                        it.onChange(getAppState())
                    }
                }

                // notify observers that match one of the states
                multiStateObservers.forEach { outer ->
                    for (inner in outer.observe()) {
                        if (inner::class.java.simpleName == appState.child!!::class.java.simpleName) {
                            // getDeepCopy() ensures immutability
                            outer.onChange(getAppState())
                            break // if matched, no need to keep running, go directly to next "outer" observer
                        }
                    }
                }

            }
        }

    /** reduces any action passed in causing the current app state to change or not */
    override fun reduce(action: Action<S>): S {
        // setter gets called here implicit but it will set the state only if it has changed
        appState = getDeepCopy(action.reduce(getAppState()))
        // ensure immutability
        return getAppState()
    }

    /** dispatch causes that every middleware interested in that action, will decide by its own, which next action they want to perform */
    override fun dispatch(action: Action<S>) = chain[0].apply(getAppState(), action, chain, 0, this)

    /** the app's current state tree */
    override fun getAppState() = getDeepCopy(appState)

    /** way android components subscribe to a state they are interested in */
    override fun subscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.add(element = observer)
    override fun subscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.add(element = observer)
    override fun subscribeConditionalState(observer: ConditionalStateObserver<S>) = conditionalStateObservers.add(element = observer)

    /** whenever a component wants to unsubscribe */
    override fun unsubscribeMultiState(observer: MultiStateObserver<S>) = multiStateObservers.remove(element = observer)
    override fun unsubscribeSimpleState(observer: SimpleStateObserver<S>) = simpleStateObservers.remove(element = observer)
    override fun unsubscribeConditionalState(observer: ConditionalStateObserver<S>) = conditionalStateObservers.remove(element = observer)

    /** kotlin's copy method by data classes are only shallow copies and do not support deep copies */
    override fun getDeepCopy(original: S): S {
        // this method is one of the most powerful method in the redux concept of immutability.
        // it avoids wrong usage by programmers and critical, unexpected side effects besides
        // better usability while reducing states. You do not have to worry about copying them anymore.
        // TODO copy original state
        return original
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




