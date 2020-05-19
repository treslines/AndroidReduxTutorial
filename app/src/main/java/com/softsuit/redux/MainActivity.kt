package com.softsuit.redux

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.softsuit.redux.mid.*
import com.softsuit.redux.mid.DI.store
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --------------------------------------------------------------------------
        // 1. register any component interested into counter state changes
        // --------------------------------------------------------------------------
        val aSimpleCounterObserver = object : SimpleStateObserver<AppState> {
            override fun observe() = CounterState()
            override fun onChange(state: AppState) {
                val data: CounterStateModel? = state.getData(CounterStateModel::class.java)
                data?.let { xIdTxtCounter.text = it.name }
            }
        }
        store.subscribeSimpleState(aSimpleCounterObserver)

        // --------------------------------------------------------------------------
        // 2. register for SearchResultState
        // --------------------------------------------------------------------------
        val aSimpleSearchResultState = object : SimpleStateObserver<AppState> {
            override fun observe() = SearchResultState()
            override fun onChange(state: AppState) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "SearchResultState: ${state.data}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        store.subscribeSimpleState(aSimpleSearchResultState)

        // --------------------------------------------------------------------------
        // 3. register a conditional state
        // --------------------------------------------------------------------------
        val aCondition: ConditionReducer<AppState> = {
            //it.jsonData["CounterState"] == 2
            true
        }
        val aConditionalCounterObserver = object : ConditionStateObserver<AppState> {
            override fun match() = aCondition
            override fun observe() = CounterState()
            override fun onChange(state: AppState) {
                Toast.makeText(
                    this@MainActivity,
                    "CounterState: ${state.data}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        store.subscribeConditionalState(observer = aConditionalCounterObserver)

        // --------------------------------------------------------------------------
        // 4. register a multi state
        // --------------------------------------------------------------------------
        val aMultiStateObserver = object : MultiStateObserver<AppState> {
            override fun observe() = listOf(SearchForKeywordState(), SearchResultState())
            override fun onChange(state: AppState) {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Result: ${state.child!!::class.java.simpleName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        store.subscribeMultiState(observer = aMultiStateObserver)

        // --------------------------------------------------------------------------
        // 5. dispatch initial reset counter action
        // --------------------------------------------------------------------------
        store.dispatch(ResetCounterAction("Reset Counter Event"))

    }

    // --------------------------------------------------------------------------
    // 6. dispatch actions on user input
    // --------------------------------------------------------------------------
    fun decrement(view: View) = store.reduce(DecrementCounterAction("Decrement Counter Event"))

    fun increment(view: View) = store.reduce(IncrementCounterAction("Increment Counter Event"))

    // --------------------------------------------------------------------------
    // 7. dispatch middleware action on user input
    // --------------------------------------------------------------------------
    fun search(view: View) = store.dispatch(SearchingAction("Searching Event"))

    fun debug(view: View) = store.dispatch(DebugAction())
}
