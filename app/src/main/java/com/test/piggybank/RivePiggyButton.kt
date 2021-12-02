package com.test.piggybank


import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.MotionEvent
import app.rive.runtime.kotlin.RiveTextureView
import app.rive.runtime.kotlin.core.*
import app.rive.runtime.kotlin.renderers.RendererSkia


class RivePiggyButton(context: Context, attrs: AttributeSet?) : RiveTextureView(context, attrs) {

    override val renderer = object : RendererSkia() {
        // this is happening in a separate thread, so we do need to be careful about modifying
        // things in a threadsafe way.
        override fun draw() {

            backgroundArtboard.drawSkia(cppPointer, Fit.COVER, Alignment.CENTER);
            val copy = flyingCoins.toList()
            for (pair in copy) {
                pair.first.drawSkia(cppPointer, Fit.COVER, Alignment.CENTER);
            }
            piggyArtboard.drawSkia(cppPointer, Fit.COVER, Alignment.CENTER);
        }

        /**
         * Advance the animation by some time.
         *
         * This is the crux of running rive animations, we apply the elapsed time to
         * all active animations/ state machines.
         *
         * We know our flying coin animation lasts about 1.7 seconds.
         * So we remove it once that amount of time has elapsed.
         *
         * And then we call "invalidateSelf" which tells android that this object needs to be drawn.
         *
         */
        override fun advance(elapsed: Float) {
            backgroundArtboard.advance(elapsed)
            piggyStateMachineInstance.apply(piggyArtboard, elapsed)
            piggyArtboard.advance(elapsed)
            val removeMe: MutableList<Pair<Artboard, StateMachineInstance>> = mutableListOf()

            for (pair in flyingCoins) {
                flyingCoinTimes[pair] = flyingCoinTimes[pair] as Float + elapsed;
                if (flyingCoinTimes[pair] as Float > 1.7) {
                    removeMe.add(pair)
                }
                pair.second.apply(pair.first, elapsed);
                pair.first.advance(elapsed);
            }

            for (pair in removeMe) {
                flyingCoins.remove(pair);
                flyingCoinTimes.remove(pair)
            }
        }
    }


    // Keep a reference to the file to keep resources around.
    private val file: File = File(resources.openRawResource(R.raw.piggy).readBytes())

    private val backgroundArtboard: Artboard = file.artboard("Background");
    private val piggyArtboard: Artboard = file.artboard("Piggy");
    private val coinArtboard: Artboard = file.artboard("Coin");


    private val piggyStateMachineInstance: StateMachineInstance;
    private val piggyStateMachine: StateMachine = piggyArtboard.stateMachine("PiggyMachine");
    private val coinStateMachine: StateMachine = coinArtboard.stateMachine("CoinMachine");

    private val pressedInput: SMITrigger;

    private val flyingCoins: MutableList<Pair<Artboard, StateMachineInstance>> = mutableListOf()
    private val flyingCoinTimes: MutableMap<Pair<Artboard, StateMachineInstance>, Float> =
        mutableMapOf()


    init {
        piggyStateMachineInstance = StateMachineInstance(piggyStateMachine)
        pressedInput = piggyStateMachineInstance.input("Pressed") as SMITrigger
    }


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
    fun showMeTheMoney() {
        pressedInput.fire()

        // create a new state machine instance for the coin state machine, so we can track
        // its state separately to the other flying coins.
        val coinStateMachineInstance = StateMachineInstance(coinStateMachine);

        // randomize the state machine input slightly so that the coins do not all follow the same path
        val coinRandomInput = coinStateMachineInstance.input("CoinRandomization") as SMINumber;
        coinRandomInput.value = (Math.random() * 100).toFloat();

        // instantiate a fresh copy of the coinArtboard, this creates a new coin shape that we can
        // move and animate, without impacting other flying coins
        val coinArtboardInstance = coinArtboard.getInstance()

        // its time advance our coin, it forces the beginning key of the animation to be applied.
        // without this, the first frame would be how the artboard looks in design mode, in this case
        // a coin in the center of the artbaord.
        coinStateMachineInstance.apply(coinArtboardInstance, 0.0f);
        coinArtboardInstance.advance(0.0f);

        // track this pair so that we can advance the animation and discard it when the animation
        // is over.
        val newPair = Pair(coinArtboardInstance, coinStateMachineInstance)
        flyingCoins.add(newPair)
        flyingCoinTimes[newPair] = 0.0f
    }


}