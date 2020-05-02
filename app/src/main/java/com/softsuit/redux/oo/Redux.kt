package com.softsuit.redux.oo

// 1. define redux pattern in oo-manner - defined ONCE!
interface State
interface ActionReducer<S: State>{
    fun reduce(old: S): S
}

typealias StateChangeListener <T> = (T) -> Unit

interface Store <S: State> {
    fun dispatch(action: ActionReducer<S>)
    fun addStateChangeListener(listener: StateChangeListener<S>): Boolean
    fun removeStateChangeListener(listener: StateChangeListener<S>): Boolean
    fun getCurrentState(): S
}

// 2. store implementation to be used everywhere in your app - implemented ONCE!
class DefaultStore<S : State>(
    val initialState: S,
    val reducers: List<ActionReducer<S>> = listOf()
) : Store<S> {

    private val listeners = mutableSetOf<StateChangeListener<S>>()

    private var currentState: S = initialState
        set(value) {
            field = value
            // this is where the "magic" happens. Every time a state
            // gets updated over this setter method, all registered
            // components is updated receiving this state with the
            // new value inside of it.
            listeners.forEach { it(value) }
        }

    override fun dispatch(action: ActionReducer<S>) {
        currentState = action.reduce(currentState) // setter gets invoked here (implicitly)
    }

    override fun addStateChangeListener(listener: StateChangeListener<S>) = listeners.add(element = listener)

    override fun removeStateChangeListener(listener: StateChangeListener<S>) = listeners.remove(element = listener)

    override fun getCurrentState(): S = currentState

}

// 3. implement actions with its reducer - see ReduxActions.kt

// 4. use store in our app over dependency injection. This is one way to do it.
//    but acc. to a droidcon 2018 video presentation, it should be avoid to have
//    multiple stores, since you may run into troubles while trying to maintain
//    it in sync.
object DI {
    val counterStore = DefaultStore( initialState = CounterState())
    // define other stores here as soon as they are defined and needed ...
}