package com.softsuit.redux

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.softsuit.redux.mid.*
import com.softsuit.redux.mid.DI.reduxStore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. register any component interested into counter state changes
        val aSimpleCounterStateObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = CounterState()
            override fun onChange(state: AppState) {
                xIdTxtCounter.text = state.data["CounterState"].toString()
            }
        }
        reduxStore.addSimpleStateObserver(aSimpleCounterStateObserver)

        // 2. dispatch reset counter action
        reduxStore.dispatch(ResetCounterAction("Reset Counter Event"))

        // 3. register for SearchResultState
        val aSimpleSearchResultStateObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = SearchResultState()
            override fun onChange(state: AppState) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "SearchResultState: ${state.data["SearchResultState"]}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        reduxStore.addSimpleStateObserver(aSimpleSearchResultStateObserver)

        // 4. Register a conditional state
        val aCondition: ConditionReducer<AppState> = {
            it.data["CounterState"] == 2
        }
        val aConditionalCounterStateObserver = object : ConditionalStateObserver<AppState> {
            override fun match() = aCondition
            override fun observe() = CounterState()
            override fun onChange(state: AppState) {
                Toast.makeText(
                    this@MainActivity,
                    "CounterState: ${state.data["CounterState"]}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        reduxStore.addConditionalStateObserver(observer = aConditionalCounterStateObserver)

        // 5. Register a multi state
        val aMultiStateObserver = object : MultiStateObserver<AppState> {
            override fun observe() = listOf(SearchForKeywordState(), SearchResultState())
            override fun onChange(state: AppState) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Result: ${state.internal!!::class.java.simpleName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        reduxStore.addMultiStateObserver(observer = aMultiStateObserver)

    }

    // 6. dispatch actions on user input
    fun decrement(view: View) = reduxStore.reduce(DecrementCounterAction("Decrement Counter Event"))
    fun increment(view: View) = reduxStore.reduce(IncrementCounterAction("Increment Counter Event"))

    // 7. dispatch middleware action on user input
    fun search(view: View) = reduxStore.dispatch(SearchingAction("Searching Event"))
    fun debug(view: View) = reduxStore.dispatch(DebugAction())
}
