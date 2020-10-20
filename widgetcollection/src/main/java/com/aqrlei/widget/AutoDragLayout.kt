package com.aqrlei.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.aqrlei.widget.BuildConfig.logger
import kotlin.math.*

/**
 * Created by AqrLei on 2019-07-02
 */
class AutoDragLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    companion object {
        private const val POSITION_OFFSET_RATIO_UNKNOWN = -1F
        private const val TAG = "AutoDragLayout"
        private var ENABLE_DEBUG = false

        fun enableDebug(enable: Boolean) {
            ENABLE_DEBUG = enable
        }
    }

    private var downX: Float = 0F
    private var downY: Float = 0F

    private val mTouchSlop = ViewConfiguration.get(this.context).scaledTouchSlop
    private var intercepted: Boolean = false
    private val velocityTracker: VelocityTracker = VelocityTracker.obtain()

    private var stickToEdge: Boolean = false
    private var scrollAnimator: ValueAnimator? = null
    private var positionXOffsetRatio: Float = POSITION_OFFSET_RATIO_UNKNOWN
    private var positionYOffsetRatio: Float = POSITION_OFFSET_RATIO_UNKNOWN

    init {
        context.obtainStyledAttributes(attrs, R.styleable.AutoDragLayout)?.apply {
            stickToEdge = getBoolean(R.styleable.AutoDragLayout_stickToEdge, false)
            positionXOffsetRatio =
                getFloat(R.styleable.AutoDragLayout_positionXOffsetRatio, POSITION_OFFSET_RATIO_UNKNOWN)
            positionYOffsetRatio =
                getFloat(R.styleable.AutoDragLayout_positionYOffsetRatio, POSITION_OFFSET_RATIO_UNKNOWN)
            recycle()
        }
        this.post {
            initPosition()
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scrollAnimator?.cancel()
                scrollAnimator = null
                parent?.requestDisallowInterceptTouchEvent(true)
                debugLog("dispatchTouchEvent: DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                debugLog("dispatchTouchEvent: MOVE")
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                debugLog("dispatchTouchEvent: CANCEL(3)--UP(1)--${ev.actionMasked}")
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        super.onInterceptTouchEvent(ev)
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                intercepted = false
                downX = ev.x
                downY = ev.y
                debugLog("onInterceptTouchEvent: DOWN")
            }
            // 被拦截后，后续的事件就不会到onInterceptTouchEvent中来了
            MotionEvent.ACTION_MOVE -> {
                val slopX = abs(ev.x - downX)
                val slopY = abs(ev.y - downY)
                intercepted = slopX > mTouchSlop || slopY > mTouchSlop
                debugLog("onInterceptTouchEvent: MOVE")
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                debugLog("onInterceptTouchEvent: CANCEL(3)--UP(1)--${ev.actionMasked}")
            }
        }
        return intercepted
    }

    private fun initPosition() {
        val lp = layoutParams as? MarginLayoutParams
        val vg = parent as? ViewGroup
        if (lp != null && vg != null) {
            val actualScrollX = vg.width - width - lp.leftMargin - lp.rightMargin
            val actualScrollY = vg.height - height - lp.topMargin - lp.rightMargin
            var wantScrollX = lp.leftMargin
            var wantScrollY = lp.topMargin

            if (positionXOffsetRatio != POSITION_OFFSET_RATIO_UNKNOWN) {
                wantScrollX = (positionXOffsetRatio * vg.width - width / 2.0F).toInt()
            } else {
                positionXOffsetRatio = (lp.leftMargin + width / 2.0F) / vg.width
            }

            if (positionYOffsetRatio != POSITION_OFFSET_RATIO_UNKNOWN) {
                wantScrollY = (positionYOffsetRatio * vg.height - height / 2.0F).toInt()
            } else {
                positionYOffsetRatio = (lp.topMargin + height / 2.0F) / vg.height
            }
            val scrollX = min(actualScrollX, wantScrollX)
            val scrollY = min(actualScrollY, wantScrollY)

            lp.topMargin = scrollY
            lp.leftMargin = scrollX
            layoutParams = lp
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val lp = layoutParams as? MarginLayoutParams
        val vg = parent as? ViewGroup
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                debugLog("onTouchEvent: DOWN")
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = event.x - downX
                val offsetY = event.y - downY
                velocityTracker.addMovement(event)
                velocityTracker.computeCurrentVelocity(1)
                if (lp != null && vg != null) {
                    lp.leftMargin += offsetX.toInt()
                    lp.leftMargin = max(0, lp.leftMargin)
                    lp.leftMargin = min(lp.leftMargin, vg.width - width)
                    lp.topMargin += offsetY.toInt()
                    lp.topMargin = max(0, lp.topMargin)
                    lp.topMargin = min(lp.topMargin, vg.height - height)
                }
                requestLayout()
                debugLog("onTouchEvent: MOVE")
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (lp != null && vg != null && stickToEdge) {
                    val xVelocity = abs(velocityTracker.xVelocity).toDouble()
                    val yVelocity = abs(velocityTracker.yVelocity).toDouble()
                    val velocity = sqrt(xVelocity.pow(2.0) + yVelocity.pow(2.0))
                    val centerXToLeft = left + width / 2F
                    val centerYToTop = top + height / 2F
                    val centerXToRight = vg.width - centerXToLeft
                    val centerYToBottom = vg.height - centerYToTop
                    var begin = 0
                    var end = 0
                    val callback: (Int?) -> Unit
                    when (minDistanceDirection(centerXToLeft, centerYToTop, centerXToRight, centerYToBottom)) {
                        Edge.LEFT -> {
                            begin = lp.leftMargin
                            end = 0
                            callback = { value ->
                                value?.let {
                                    lp.leftMargin = it
                                    layoutParams = lp
                                }
                            }
                        }
                        Edge.TOP -> {
                            begin = lp.topMargin
                            end = 0
                            callback = { value ->
                                value?.let {
                                    lp.topMargin = it
                                    layoutParams = lp
                                }
                            }
                        }
                        Edge.RIGHT -> {
                            begin = lp.leftMargin
                            end = vg.width - width
                            callback = { value ->
                                value?.let {
                                    lp.leftMargin = it
                                    layoutParams = lp
                                }
                            }
                        }
                        Edge.BOTTOM -> {
                            begin = lp.topMargin
                            end = vg.height - height
                            callback = { value ->
                                value?.let {
                                    lp.topMargin = it
                                    layoutParams = lp
                                }
                            }
                        }
                    }
                    val duration = if (velocity > 0F) (begin - end / xVelocity).toLong() else null
                    scroll(duration, begin, end, callback)
                }
                debugLog("onTouchEvent: CANCEL(3)--UP(1)--${event.actionMasked}")
            }
        }
        return intercepted
    }

    private fun minDistanceDirection(toLeft: Float, toTop: Float, toRight: Float, toBottom: Float): Edge {
        return when {
            toLeft <= toTop
                    && toLeft <= toRight
                    && toLeft <= toBottom -> Edge.LEFT
            toTop <= toLeft
                    && toTop <= toRight
                    && toTop <= toBottom -> Edge.TOP
            toRight <= toLeft
                    && toRight <= toTop
                    && toRight <= toBottom -> Edge.RIGHT
            else -> Edge.BOTTOM
        }
    }

    private fun debugLog(message: String) {
        if (ENABLE_DEBUG) {
            logger.d(TAG, message)
        }
    }

    private fun scroll(d: Long? = null, begin: Int, end: Int, callback: (Int?) -> Unit) {
        scrollAnimator = ValueAnimator.ofInt(begin, end).apply {
            duration = if (d != null) duration else (abs(end - begin) / 300F * 100).toLong()
            addUpdateListener { callback(it.animatedValue as? Int) }
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    override fun onDetachedFromWindow() {
        velocityTracker.recycle()
        super.onDetachedFromWindow()
    }

    private enum class Edge {
        LEFT, TOP, RIGHT, BOTTOM
    }
}