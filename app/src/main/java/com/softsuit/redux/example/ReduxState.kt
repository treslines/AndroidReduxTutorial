package com.softsuit.redux.example

import com.softsuit.redux.oo.AppState

/**
 * This is the central place, where you define all your app states. Those states could be placed by its corresponding action also,
 * but as soon as you app grows, it gets very annoying and time consuming to switch/open a lot of different files, while searching
 * for a specific state. Here you can just pick up and group it by topic or use case. And since they are all an one liner, it fits
 * perfectly here in one clear, single file.
 */

// +------------------------------------------------------------------------------------------------+
// | State: Represents either a transition, navigation or UI-Actions:                               |
// | For each action, exits its corresponding state                                                 |
// +------------------------------------------------------------------------------------------------+
class CounterState() : AppState(id = "CounterState")
class SearchResultState : AppState(id = "SearchResultState")
class SearchingState : AppState(id = "SearchingState")
class SearchForKeywordState : AppState(id = "SearchForKeywordState")
class WaitingForUserInputState : AppState(id = "WaitingForUserInputState")
class DebugState : AppState(id = "DebugState")

// +------------------------------------------------------------------------------------------------+
// | Models: Every state knows its data model                                                       |
// +------------------------------------------------------------------------------------------------+
class CounterStateModel(var counter: Int = 0)


