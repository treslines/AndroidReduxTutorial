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
        reduxStore.subscribe {
            // Important: subscribers are dumb and should only assign already computed state values
            when (it.internal) {
                is CounterState -> xIdTxtCounter.text = it.data["CounterState"].toString()
            }
        }

        // 2. dispatch reset counter action
        reduxStore.dispatch(ResetCounterAction("Reset Counter Event"))

        // 3. register for SearchResultState
        reduxStore.subscribe {
            when (it.internal) {
                is SearchResultState ->
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "SearchResultState: ${it.data["SearchResultState"]}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

    }

    // 4. dispatch actions on user input
    fun decrement(view: View) = reduxStore.reduce(DecrementCounterAction("Decrement Counter Event"))
    fun increment(view: View) = reduxStore.reduce(IncrementCounterAction("Increment Counter Event"))

    // 5. dispatch middleware action on user input
    fun search(view: View) = reduxStore.dispatch(SearchingAction("Searching Event"))
    fun debug(view: View) = reduxStore.dispatch(DebugAction())
}
