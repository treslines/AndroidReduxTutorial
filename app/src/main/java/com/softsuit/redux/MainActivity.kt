package com.softsuit.redux

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.softsuit.redux.oo.*
import com.softsuit.redux.oo.DI.store
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
                val data: CounterStateModel? = state.getDataModel(CounterStateModel::class.java, state.data)
                data?.let { xIdTxtCounter.text = it.counter.toString() }
            }
        }
        store.subscribe(aSimpleCounterObserver)

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
        store.subscribe(aSimpleSearchResultState)

        // --------------------------------------------------------------------------
        // 3. register a conditional state (multi condition can be done as well)
        // --------------------------------------------------------------------------
        val aCondition: ConditionReducer<AppState> = {
            //it.jsonData["CounterState"] == 2
            true
        }
        val aConditionalCounterObserver = object : ConditionStateObserver<AppState> {
            override fun observe() = CounterState()
            override fun match() = aCondition
            override fun onChange(state: AppState) {
                Toast.makeText(
                    this@MainActivity,
                    "CounterState: ${state.data}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        store.subscribe(observer = aConditionalCounterObserver)

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
        store.subscribe(observer = aMultiStateObserver)

        // --------------------------------------------------------------------------
        // 5. dispatch initial reset counter action
        // --------------------------------------------------------------------------
        store.dispatch(ResetCounterAction("Reset Counter Event"))

    }

    // --------------------------------------------------------------------------
    // 6. dispatch actions on user input
    // --------------------------------------------------------------------------
    fun decrement(view: View) = store.reduce(DecrementCounterAction("DecrementCounterEvent"))
    fun increment(view: View) = store.reduce(IncrementCounterAction("IncrementCounterEvent"))

    // --------------------------------------------------------------------------
    // 7. dispatch middleware action on user input
    // --------------------------------------------------------------------------
    fun search(view: View) = store.dispatch(SearchingAction("SearchingEvent"))
    fun debug(view: View) = store.dispatch(DebugAction())
}
