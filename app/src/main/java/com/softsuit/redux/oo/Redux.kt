package com.softsuit.redux.oo

// 1. define redux pattern in oo-manner - defined ONCE!
interface State
interface Action<S: State>{
    fun reducer(old: S): S
}

typealias StoreSubscriber <T> = (T) -> Unit

interface Store <S: State> {
    fun dispatch(action: Action<S>)
    fun subscribe(subscriber: StoreSubscriber<S>): Boolean
    fun unsubscribe(subscriber: StoreSubscriber<S>): Boolean
    fun getCurrentState(): S
}

// 2. store implementation to be used everywhere in your app - implemented ONCE!
class DefaultStore <S: State>(initialState: S): Store<S> {

    private val subscribers = mutableSetOf<StoreSubscriber<S>>()

    private var currentState: S = initialState
        set(value) {
            field = value
            subscribers.forEach { it(value) }
        }

    override fun dispatch(action: Action<S>) {
        currentState = action.reducer(currentState)
    }

    override fun subscribe(subscriber: StoreSubscriber<S>) = subscribers.add(element = subscriber)

    override fun unsubscribe(subscriber: StoreSubscriber<S>) = subscribers.remove(element = subscriber)

    override fun getCurrentState(): S = currentState

}

// 3. implement actions with its reducer - see ReduxCounter.kt

// 4. use store in our app over dependency injection
object DI {
    val counterStore = DefaultStore( initialState = CounterState())
    // define other stores here as soon as they are defined and needed ...
}