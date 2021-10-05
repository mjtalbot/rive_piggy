package com.test.piggybank

import android.animation.TimeAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import app.rive.runtime.kotlin.core.*


class PiggyDrawable(piggyFile: File) : Drawable() {

    private val renderer = Renderer()
    private val animator = TimeAnimator()
    private val backgroundArtboard: Artboard = piggyFile.artboard("Background");
    private val piggyArtboard: Artboard = piggyFile.artboard("Piggy");
    private val coinArtboard: Artboard = piggyFile.artboard("Coin");


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

        animator.setTimeListener { _, _, delta ->
            advance(delta.toFloat())
        }
        animator.start()
    }

    fun showMeTheMoney() {
        pressedInput.fire()
        val coinStateMachineInstance = StateMachineInstance(coinStateMachine);
        val coinRandomInput = coinStateMachineInstance.input("CoinRandomization") as SMINumber;
        coinRandomInput.value = (Math.random() * 100).toFloat();
        val newPair = Pair(coinArtboard.getInstance(), coinStateMachineInstance)
        newPair.second.advance(newPair.first, 0.0f);
        newPair.first.advance(0.0f);
        flyingCoins.add(newPair)
        flyingCoinTimes[newPair] = 0.0f
    }

    fun advance(delta: Float) {
        val elapsed = delta / 1000
        backgroundArtboard.advance(elapsed)
        piggyStateMachineInstance.advance(piggyArtboard, elapsed)
        piggyArtboard.advance(elapsed)
        val removeMe: MutableList<Pair<Artboard, StateMachineInstance>> = mutableListOf()

        for (pair in flyingCoins) {
            flyingCoinTimes[pair] = flyingCoinTimes[pair] as Float + elapsed;
            if (flyingCoinTimes[pair] as Float > 1.7) {
                removeMe.add(pair)
            }
            pair.second.advance(pair.first, elapsed);
            pair.first.advance(elapsed);
        }

        for (pair in removeMe) {
            flyingCoins.remove(pair);
            flyingCoinTimes.remove(pair)
        }

        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        renderer.canvas = canvas

        val saved = canvas.save()
        renderer.align(
            Fit.COVER,
            Alignment.CENTER,
            AABB(bounds.width().toFloat(), bounds.height().toFloat()),
            piggyArtboard.bounds
        )

        backgroundArtboard.draw(renderer);
        for (pair in flyingCoins) {
            pair.first.draw(renderer);
        }
        piggyArtboard.draw(renderer);
        canvas.restoreToCount(saved)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}