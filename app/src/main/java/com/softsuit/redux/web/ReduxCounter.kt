package com.softsuit.redux.web

// 3. implement the entities for our needs - actions can contain various states as a group
data class CounterState(val value: Int = 0): State

// 3.1 implement counter state actions as a group of possible state changes
sealed class CounterActions: Action {
    object Init: CounterActions()
    object Increment: CounterActions()
    object Decrement: CounterActions()
}

// 3.2 thank to typealias, any reducer can be typed and implemented on the fly acc. to your needs
val CounterStateReducer: Reducer<CounterState> = { old, action ->
    when (action) {
        is CounterActions.Init -> CounterState()
        is CounterActions.Increment -> old.copy(value = old.value + 1)
        is CounterActions.Decrement -> old.copy(value = old.value - 1)
        else -> old
    }
}