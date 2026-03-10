package com.example.cashflowin.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class DraggableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr), View.OnTouchListener {

    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    
    private var isDragging = false
    private val clickThreshold = 10

    init {
        setOnTouchListener(this)
        elevation = 8f
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val action = motionEvent.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = motionEvent.rawX
                downRawY = motionEvent.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val viewWidth = view.width
                val viewHeight = view.height

                val viewParent = view.parent as View
                val parentWidth = viewParent.width
                val parentHeight = viewParent.height

                var newX = motionEvent.rawX + dX
                newX = 0f.coerceAtLeast(newX.coerceAtMost((parentWidth - viewWidth).toFloat()))

                var newY = motionEvent.rawY + dY
                newY = 0f.coerceAtLeast(newY.coerceAtMost((parentHeight - viewHeight).toFloat()))

                if (abs(motionEvent.rawX - downRawX) > clickThreshold || abs(motionEvent.rawY - downRawY) > clickThreshold) {
                    isDragging = true
                    view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    return performClick()
                } else {
                    snapToEdge(view)
                    return true
                }
            }
        }
        return super.onTouchEvent(motionEvent)
    }

    private fun snapToEdge(view: View) {
        val viewParent = view.parent as View
        val parentWidth = viewParent.width
        val viewWidth = view.width
        
        val margin = 48f 
        
        val endX = if (view.x + viewWidth / 2 < parentWidth / 2) {
            margin 
        } else {
            parentWidth - viewWidth - margin
        }

        view.animate()
            .x(endX)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }
}
