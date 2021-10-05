package com.test.piggybank


import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton


import app.rive.runtime.kotlin.core.File

class RivePiggyButton(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageButton(context, attrs) {
    private var riveDrawable: PiggyDrawable;
    private var riveFile: File;

    init {
        var resourceBytes = resources.openRawResource(R.raw.piggy).readBytes()
        riveFile = File(resourceBytes)
        riveDrawable = PiggyDrawable(riveFile)

        background = riveDrawable
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // we want to trigger a coin animation when we detect a down press
        if (event?.action == MotionEvent.ACTION_DOWN) {
            riveDrawable.showMeTheMoney()
        }
        return super.onTouchEvent(event)
    }
}
