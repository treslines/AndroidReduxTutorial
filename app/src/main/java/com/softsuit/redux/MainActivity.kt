package com.softsuit.redux

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.softsuit.redux.mid.*
import com.softsuit.redux.mid.DI.reduxStore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        0. This snippet does the same as code bellow it (1.) and may be more readable
//        or understandable at the beginning, while getting in touch with kotlin's syntax

//        Let's point it out what it does in detail:
//        it defines a variable 'subscriber' which is a typed function whose implementation accepts
//        the defined type 'CounterState' and as soon as this function gets called, it does
//        something with the instance of this type, but returns nothing. In the implementation's
//        body is where our component grabs the instance of 'CounterState' and assigns its value.

//        This snippet does the same as 1. in a little 'verboser' manner
//        val listener : StateChangeListener<CounterState> = {
//            xIdTxtCounter.text = "${it.value}"
//        }
//        DI.counterStore.addStateChangeListener(listener)

//      -------------------------------------------------------------------------------------------

        // 1. web: register any component interested into counter state changes
        // DI.counterStore.addStateChangeListener {
        //    xIdTxtCounter.text = "${it.value}"
        // }

        // 2. web: dispatch initial action to archive a defined state
        // DI.counterStore.dispatch(action = CounterActions.Init)

//      -------------------------------------------------------------------------------------------

        // 1. oo: register any component interested into counter state changes
        // ooDI.counterStore.addStateChangeListener {
        //    xIdTxtCounter.text = "${it.value}"
        // }

        // 2. oo: dispatch initial action to archive a defined state
        // ooDI.counterStore.dispatch(ooCounterInitialAction("main activity, on create, initial state"))

        // -------------------------------------------------------------------------------------------

        // 1. mid: register any component interested into counter state changes
        reduxStore.subscribe {
            when (it.internal) {
                is ResetCounterState -> xIdTxtCounter.text = "0"
                is IncrementCounterState -> xIdTxtCounter.text = (xIdTxtCounter.text.toString().toInt() + 1).toString()
                is DecrementCounterState -> xIdTxtCounter.text = (xIdTxtCounter.text.toString().toInt() - 1).toString()
            }
        }

        // 2. mid: dispatch search event to database
        reduxStore.dispatch(ResetCounterAction("Reset Counter Event"))

    }

    // 3. dispatch actions on user interaction
    fun decrement(view: View) {
        // web
        // DI.counterStore.dispatch(action = CounterActions.Decrement)

        // oo
        // ooDI.counterStore.dispatch(ooCounterDecrementAction("decrement"))

        // mid
        reduxStore.dispatch(DecrementCounterAction("Decrement Counter Event"))
    }
    fun increment(view: View) {
        // web
        // DI.counterStore.dispatch(action = CounterActions.Increment)

        // oo
        // ooDI.counterStore.dispatch(ooCounterIncrementAction("increment"))

        // mid
        reduxStore.dispatch(IncrementCounterAction("Increment Counter Event"))
    }

    fun search(view: View) {
        // mid
        reduxStore.dispatch(DebugAction())
    }
}
