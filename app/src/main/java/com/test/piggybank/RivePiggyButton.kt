package com.test.piggybank


import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import app.rive.runtime.kotlin.RiveTextureView
import app.rive.runtime.kotlin.core.*
import app.rive.runtime.kotlin.renderers.RendererSkia


class RivePiggyButton(context: Context, attrs: AttributeSet?) : RiveTextureView(context, attrs) {
    private lateinit var file: File
    // Objects that the renderer needs for drawing
    private lateinit var backgroundArtboard: Artboard;
    private lateinit var piggyArtboard: Artboard;

    // Object that the renderer needs to advance the state machines
    private lateinit var piggyStateMachine: StateMachineInstance;

    // Object to set a state machine input (trigger)
    private lateinit var pressedInput: SMITrigger;

    // Reference to all coins, and their time alive
    private val flyingCoins: MutableList<Pair<Artboard, StateMachineInstance>> = mutableListOf()
    private val flyingCoinTimes: MutableMap<Pair<Artboard, StateMachineInstance>, Float> =
        mutableMapOf()

    // Setup the Rive file and required objects
    private fun setupFile(skRenderer: RendererSkia) {
        val resource = resources.openRawResource(R.raw.piggy)
        file = File(resource.readBytes())
        resource.close()

        backgroundArtboard = file.artboard("Background");

        piggyArtboard = file.artboard("Piggy");
        piggyStateMachine = piggyArtboard.stateMachine("PiggyMachine")
        pressedInput = piggyStateMachine.input("Pressed") as SMITrigger

        // This will be deleted with its dependents.
        skRenderer.dependencies.add(file)
    }

    override fun createObserver(): LifecycleObserver {
        return object : DefaultLifecycleObserver {
            /* Optionally override lifecycle methods. */
            // override fun onCreate(owner: LifecycleOwner) {
            //     super.onCreate(owner)
            // }
            // override fun onDestroy(owner: LifecycleOwner) {
            //     super.onDestroy(owner)
            // }
        }
    }

    override fun createRenderer(): RendererSkia {
        val skRenderer = object : RendererSkia() {

            override fun draw() {
                save()
                align(Fit.COVER, Alignment.CENTER,   RectF(0.0f, 0.0f, width, height),backgroundArtboard.bounds,)

                backgroundArtboard.drawSkia(cppPointer);

                val copy = flyingCoins.toList()
                for (pair in copy) {
                    pair.first.drawSkia(cppPointer);
                }
                piggyArtboard.drawSkia(cppPointer);
                restore()

            }

            override fun advance(elapsed: Float) {
                backgroundArtboard.advance(elapsed)
                piggyStateMachine.advance(elapsed)
                piggyArtboard.advance(elapsed)
                val removeMe: MutableList<Pair<Artboard, StateMachineInstance>> = mutableListOf()

                for (pair in flyingCoins) {
                    flyingCoinTimes[pair] = flyingCoinTimes[pair] as Float + elapsed;
                    if (flyingCoinTimes[pair] as Float > 1.7) {
                        removeMe.add(pair)
                    }
                    pair.first.advance(elapsed)
                    pair.second.advance(elapsed)
                }

                for (pair in removeMe) {
                    flyingCoins.remove(pair);
                    flyingCoinTimes.remove(pair)
                }
            }
        }
        // Call setup file only once we created the renderer.
        setupFile(skRenderer)
        return skRenderer
    }

    /**
     * Someone presses anywhere on the screen, if its a down press lets make the piggy sing!
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // we want to trigger a coin animation when we detect a down press
        if (event?.action == MotionEvent.ACTION_DOWN) {
            showMeTheMoney()
        }

        return super.onTouchEvent(event)
    }

    /**
     * Add a new coin into the mix.
     */
    private fun showMeTheMoney() {
        pressedInput.fire()

        // create a new artboard and state machine instance, so we can track, draw, and update them
        // separately to the other flying coins.
        val coinArtboard = file.artboard("Coin");
        val  coinStateMachine = coinArtboard.stateMachine("CoinMachine")


        // randomize the state machine input slightly so that the coins do not all follow the same path
        val coinRandomInput = coinStateMachine.input("CoinRandomization") as SMINumber;
        coinRandomInput.value = (Math.random() * 100).toFloat();

        // its time advance our coin, it forces the beginning key of the animation to be applied.
        // without this, the first frame would be how the artboard looks in design mode, in this case
        // a coin in the center of the artboard.
        coinStateMachine.advance(0.0001f)
        coinStateMachine.advance(0.0001f)
        coinArtboard.advance(0.0001f)

        // track this pair so that we can advance the animation and discard it when the animation
        // is over.
        val newPair = Pair(coinArtboard, coinStateMachine)
        flyingCoins.add(newPair)
        flyingCoinTimes[newPair] = 0.0f
    }

}