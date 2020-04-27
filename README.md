# Android Redux - Get Started Tutorial - Minimalistic Guide! (Classic Web Implementation and Object Oriented)
Sample / example android redux kotlin implementation / tutorial as a get started guide 
## :dart: Without third party library, just plain kotlin!

### Core redux
Basic redux pattern definitions.

```kotlin
// 1. define the entities we're working with - redux pattern defined ONCE!
interface State
interface Action

typealias Reducer <T> = (T, Action) -> T
typealias StoreSubscriber <T> = (T) -> Unit

interface Store <S: State> {
    fun dispatch(action: Action)
    fun subscribe(subscriber: StoreSubscriber <S>): Boolean
    fun unsubscribe(subscriber: StoreSubscriber <S>): Boolean
    fun getCurrentState(): S
}
```
### Generic store implementation
A sample, generic store implementation to be used everywhere in your app.

```kotlin
// 2. default store implementation to be used everywhere in your app - implemented ONCE!
class DefaultStore <S: State>(initialState: S,private val reducer: Reducer<S>): Store<S> {

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

// 3. implement the entities for our needs - see ReduxCounter.kt as a sample implementation
//    for every new reducer implementation a new .kt file is created like ReduxCounter.kt


// 4. use store in our app over dependency injection (central place of definition)
//    new line is added, every time a new store is created. Single point of definition (lookup)
//    for better maintenance and faster learning process by new developers.
object DI {
    val counterStore = DefaultStore(initialState = CounterState(), reducer = CounterStateReducer)
    // define other stores here as soon as they are defined and needed ...
    // val storeMyNextNeed = DefaultStore(initialState = CounterState(), reducer = CounterStateReducer)
}
```

### Reducer implementation of bullet 3.
The actually implementation of a simple reducer with its states and actions.

```kotlin
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
```
