[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)
# Android Redux - Get Started Tutorial - Minimalistic Guide! (Classic Web and OO-Implementation)
___
:dart: A complete implementation of redux for android apps using kotlin and including middlewares. No third party library, just plain kotlin!

## Overview Core Concept
Redux core concept and components
<p align="center">
  <img src="https://github.com/treslines/AndroidReduxTutorial/blob/master/app/src/main/res/drawable/overview.png">
</p>

## Usage
Redux in action in a simple flow
<p align="center">
  <img src="https://github.com/treslines/AndroidReduxTutorial/blob/master/app/src/main/res/drawable/usage.png">
</p>

## UML Diagram
Desing and class diagram with comments
<p align="center">
  <img src="https://github.com/treslines/AndroidReduxTutorial/blob/master/app/src/main/res/drawable/redux.png">
</p>


# Pros - Cons - Challanges - Good to Know
### Pros
- The app is still to small and it is too soon to judge anything at the moment, although i could imagine the benefits (so lets wait till this project grows - i am planing to implement a full payment flow to see how it feels)
- The idea of subscription and just value assignments in the views is very nice. The store is available everywhere which is also nice.

### Cons
- What I found annoying was this copying job that you will have to do every time an action is triggered to create a new state and not mofify the old(current one)
- There is a huge extra boilerplate of states and actions. And i fear that this is going to increase a lot later on 
- Things get complicated very fast the more attributes you have for each state and if middlewares create new actions, perform some side effects etc. you may run very fest into stack over flows. (endless loops)

### Challanges
- The most important and impactful part to me today (03.05.2020) is to find a way to keep states as flat as possible and to maintain things manageable and easy understandable. I fear huge business logic problems there. So the way i probably would handle it, would be to create business logic resolvers/reducers for every state group(imagining that a complex state may have a lot of variables, constraints etc.) But lets wait for the next chapters of this story.

### Good to Know
- What i saw so far is that you must have a good understanding of redux to do things right (there is a learn curve)
- This new way of functional thinking takes a while till you really understand its power and simplicity (but it's worth)

# What are you going to learn and see?
1. You'll learn the redux core concepts step by step without worring about other things
2. You'll see a plain kotlin OO redux implementation without third party libraries
3. You'll step thru all redux components one by one where I point out what happens in each class
4. You'll see how middlewares could be integrated and how they was tought to handle async tasks (side effects)
5. You'll understand how components can subscribe to state changes  
6. At the end while cloning this project, you should get the app as shown bellow:

<p align="center">
  <img width="340" height="271" src="https://github.com/treslines/AndroidReduxTutorial/blob/master/app/src/main/res/drawable/reduxtutorial.jpg">
</p>

___
# Why shall i use the OO-Version?
Well, the question is not if you should use it or not. **It is much more about the question:** 

> *Is there a way to design my redux code in such a way, that my code complains with the Open-Close-Principle (OCP)?* 

If you take a look at this simple but very often used web-implementation bellow, at bullet 3. you'll see that every reducer has to switch-case actions in order to perform the right action. What does it means for us in terms of code enhancement, maintenance and changes? Imagine, every time you implement a new action, you'll need to touch the reducers including a new switch-case to it. This may cause side effects, than you would be touching/changing already running, tested code. Another problem i see by implementing like in bullet 3.1 is that you'll also going to touch/change this action group. Again that's not open-closed. (i know this example was designed for the web and there this may be good, but since i am targeting android apps which does support oo-design, that's why i am trying to archieve the same result using the redux-core concepts but adapting it to conform to OCP)

:dart: It just a private initative aiming to get better enhanceable, maintainable code. My main goal with this project is to find out how easy or complex it is to implement and use redux in android apps and to see if it really has this huge benefit everybody is talking about or just adds more boilerplates and unnecessary complexity to it. **So stay tunned! :octocat: and give it a star :sparkles:!**

The project root is very simplistic and there you are gonna find a structure like this bellow, where in **web** folder you'll find a web sample implementation of redux (that's where i got inpired from), in **oo** folder the object oriented implementation without middleware and in **mid** folder you are gonna find the complete middleware, object oriented implementation, which is used in the main activity. 

<p align="center">
  <img width="152" height="143" src="https://github.com/treslines/AndroidReduxTutorial/blob/master/app/src/main/res/drawable/project.jpg">
</p>

___
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
### Generic store implementation (here without middleware just for show case)
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

// 3. implement the entities for our needs - see code bellow

// 4. use store in our app over dependency injection
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

# Contributions:
  - Fork the project, implement your contribution, make a pull request

## License:
[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)
- **[MIT license](http://opensource.org/licenses/mit-license.php)**
- Copyright 2020 © Ricardo Ferreira

## Author:
<pre>
<b>Ricardo Ferreira</b>
Software Engineer at SopraSteria <a href="https://www.soprasteria.com/">https://www.soprasteria.com/</a>
Instagram: ricardo7307
Twitter: ricardo_7307
Other projects you may like also: 
- Natural UML <a href="https://treslines.github.io/">Create UML, Use Case or State Diagrams Online For Free!</a> 
- Orchid Password App <a href="https://play.google.com/store/apps/details?id=com.softsuit.orchid">Offline password manger resistant to quantum computing attacks!</a> 👊
- Codegramm - Needs Driven Development <a href="http://codegramm.herokuapp.com/">Tired to figure out adequate design patterns for your app? Try codegramm. It is free!</a> 
- Tech-Blog <a href="http://www.cleancodedevelopment-qualityseal.blogspot.com.br">http://www.cleancodedevelopment-qualityseal.blogspot.com.br</a>
</pre>
