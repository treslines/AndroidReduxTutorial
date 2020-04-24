package com.softsuit.redux

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
//        val subscriber : StoreSubscriber<CounterState> = {
//            xIdTxtCounter.text = "${it.value}"
//        }
//        DI.counterStore.subscribe(subscriber);

//      -------------------------------------------------------------------------------------------

        // 1. register any component interested into counter state changes
        DI.counterStore.subscribe {
            xIdTxtCounter.text = "${it.value}"
        }

        // 2. dispatch initial action to archive a defined state
        DI.counterStore.dispatch(action = CounterActions.Init)

    }

    // 3. dispatch actions on user interaction
    fun decrement(view: View) {
        DI.counterStore.dispatch(action = CounterActions.Decrement)
    }
    fun increment(view: View) {
        DI.counterStore.dispatch(action = CounterActions.Increment)
    }
}
