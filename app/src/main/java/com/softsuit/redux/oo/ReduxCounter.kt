package com.softsuit.redux.oo

// 3. define your state
data class CounterState(val value: Int = 0) : State

// 3.1 Define your actions. Each of it hat its onw reducer which gets called by its store
class CounterIncrementAction : Action<CounterState> {
    override fun reducer(old: CounterState): CounterState {
        return CounterState(value = old.value + 1)
    }
}

class CounterDecrementAction : Action<CounterState> {
    override fun reducer(old: CounterState): CounterState {
        return CounterState(value = old.value - 1)
    }
}

class CounterInitialAction : Action<CounterState> {
    override fun reducer(old: CounterState): CounterState {
        return CounterState()
    }
}
