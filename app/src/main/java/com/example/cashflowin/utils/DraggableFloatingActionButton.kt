package com.example.cashflowin.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlin.math.abs

class DraggableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ExtendedFloatingActionButton(context, attrs, defStyleAttr), View.OnTouchListener {

    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val action = motionEvent.action
        if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            return true // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth = view.width
            val viewHeight = view.height

            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height

            var newX = motionEvent.rawX + dX
            newX = 0f.coerceAtLeast(newX.coerceAtMost((parentWidth - viewWidth).toFloat()))

            var newY = motionEvent.rawY + dY
            newY = 0f.coerceAtLeast(newY.coerceAtMost((parentHeight - viewHeight).toFloat()))

            view.animate()
                .x(newX)
                .y(newY)
                .setDuration(0)
                .start()
            return true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY

            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY

            if (abs(upDX) < 10 && abs(upDY) < 10) { // Check if it's a click
                return performClick()
            }
            return true // Consumed
        }
        return super.onTouchEvent(motionEvent)
    }
}
