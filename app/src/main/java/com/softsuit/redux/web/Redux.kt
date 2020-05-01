package com.softsuit.redux.web

// tutorial inspired by:
// https://medium.com/swlh/how-to-implement-redux-in-kotlin-part-1-the-basics-db2854613079

// 1. define the entities we're working with - redux pattern defined ONCE!
interface State
interface Action

typealias Reducer <T> = (T, Action) -> T
typealias StoreSubscriber <T> = (T) -> Unit

interface Store <S: State> {
    fun dispatch(action: Action)
    fun subscribe(subscriber: StoreSubscriber<S>): Boolean
    fun unsubscribe(subscriber: StoreSubscriber<S>): Boolean
    fun getCurrentState(): S
}

// 2. default store implementation to be used everywhere in your app - implemented ONCE!
class DefaultStore <S: State>(initialState: S, private val reducer: Reducer<S>):
    Store<S> {

    private val subscribers = mutableSetOf<StoreSubscriber<S>>()

    private var currentState: S = initialState
        set(value) {
            field = value
            subscribers.forEach { it(value) }
        }

    override fun dispatch(action: Action) {
        currentState = reducer(currentState, action)
    }

    override fun subscribe(subscriber: StoreSubscriber<S>) = subscribers.add(element = subscriber)

    override fun unsubscribe(subscriber: StoreSubscriber<S>) = subscribers.remove(element = subscriber)

    override fun getCurrentState(): S = currentState
}

// 3. implement the entities for our needs - see ReduxActions.kt as a sample implementation

// 4. use store in our app over dependency injection
object DI {
    val counterStore = DefaultStore(initialState = CounterState(),reducer = CounterStateReducer)
    // define other stores here as soon as they are defined and needed ...
}

