package com.softsuit.redux.mid

/**
 * This is the central place, where you define all your app states. Those states could be placed by its corresponding action also,
 * but as soon as you app grows, it gets very annoying and time consuming to switch/open a lot of different files, while searching
 * for a specific state. Here you can just pick up and group it by topic or use case. And since they are all an one liner, it fits
 * perfectly here in one clear, single file.
 */

// UI-Actions: For each action, there is its corresponding state
class CounterState() : AppState(id = "CounterState")

// Search Middleware Actions: For each action, there is its corresponding state
class SearchResultState : AppState(id = "SearchResultState")

class SearchingState : AppState(id = "SearchingState")
class SearchForKeywordState : AppState(id = "SearchForKeywordState")
class WaitingForUserInputState : AppState(id = "WaitingForUserInputState")

// Debug-Development actions: For each action, there is its corresponding state
class DebugState : AppState(id = "DDebugState")

// TODO: define state types here
// TODO: Ex: SearchResultStateType
class CounterStateModel(val name: String)


