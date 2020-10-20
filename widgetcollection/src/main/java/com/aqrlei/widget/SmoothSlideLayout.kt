package com.aqrlei.widget

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

/**
 * Created by AqrLei on 2019-12-20
 */
class SmoothSlideLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var downX: Float = 0F
    private var downY: Float = 0F
    private var offsetX: Float = 0F
    private var offsetY: Float = 0F

    private val mTouchSlop = ViewConfiguration.get(this.context).scaledTouchSlop
    private var intercepted: Boolean = false
    private var isMoved: Boolean = false
    private var preExpanded: Boolean = true

    private var alphaChangeEnable: Boolean = false
    private var expandAlpha: Float = 1.0F
        get() = when {
            field < 0F -> 0F
            field > 1.0F -> 1.0F
            else -> field
        }
    private var shrinkAlpha: Float = 0.2F
        get() = when {
            field < 0F -> 0F
            field > 1.0F -> 1.0F
            else -> field
        }

    private var scrollAnimator: ValueAnimator = ObjectAnimator.ofPropertyValuesHolder(this)
    private var alphaProperty: PropertyValuesHolder? = null
    private var translateProperty: PropertyValuesHolder? = null

    private var scrollDirection: ScrollDirection = ScrollDirection.LEFT
    private var scrollDuration: Long = 1000
    private var scrollKeepSize: Float = 100F

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SmoothSlideLayout).apply {
            scrollDirection =
                ScrollDirection.values()[getInt(R.styleable.SmoothSlideLayout_scroll_direction, 0)]
            scrollDuration = getInt(R.styleable.SmoothSlideLayout_scroll_duration, 1000).toLong()
            scrollKeepSize = getDimension(R.styleable.SmoothSlideLayout_scroll_keep_size, 100F)
            alphaChangeEnable =
                getBoolean(R.styleable.SmoothSlideLayout_scroll_alpha_change_enable, false)
            shrinkAlpha =
                getFloat(R.styleable.SmoothSlideLayout_scroll_alpha_to_shrink, shrinkAlpha)
            expandAlpha =
                getFloat(R.styleable.SmoothSlideLayout_scroll_alpha_to_expand, expandAlpha)
            recycle()
        }
        scrollAnimator.interpolator = AccelerateDecelerateInterpolator()
        this.post {
            if (alphaChangeEnable && expandAlpha < 1.0F) {
                assembleAlphaAnimProperty(1.0F, expandAlpha)
                scrollAnimator.setValues(alphaProperty)
                scrollAnimator.start()
            }
        }
    }

    fun setExpand(isExpand: Boolean) {
        if (preExpanded != isExpand) {
            scrollAnimator.cancel()
            scroll(isExpand)
        }
    }

    fun setInterpolator(interpolator: Interpolator) {
        scrollAnimator.interpolator = interpolator
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        intercepted = super.onInterceptTouchEvent(ev)
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                intercepted = false
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val slopX = abs(ev.x - downX)
                val slopY = abs(ev.y - downY)
                isMoved =
                    (slopX > mTouchSlop && isHorizontonScroll()) || (slopY > mTouchSlop && isVerticalScroll())
                intercepted = isMoved
            }
        }
        return intercepted
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                offsetX = event.x - downX
                offsetY = event.y - downY
                val isToExpand = when (scrollDirection) {
                    ScrollDirection.LEFT -> offsetX > 0

                    ScrollDirection.TOP -> offsetY > 0

                    ScrollDirection.RIGHT -> offsetX < 0

                    ScrollDirection.BOTTOM -> offsetY < 0
                }
                if (isMoved && !scrollAnimator.isRunning) {
                    scroll(isToExpand)
                }
            }
        }
        return (intercepted || scrollAnimator.isRunning).takeIf { it } ?: super.onTouchEvent(event)
    }

    private fun scroll(isToExpand: Boolean) {
        preScrollSize()?.let {
            scrollAnimator.run {
                preExpanded = isToExpand
                duration = scrollDuration
                assembleTranslateAnimProperty(getScrollSize(it, isToExpand))
                val fromAlpha = scrollFromAlpha(isToExpand)
                val toAlpha = scrollToAlpha(isToExpand)
                assembleAlphaAnimProperty(fromAlpha, toAlpha)
                if (alphaChangeEnable) {
                    setValues(translateProperty, alphaProperty)
                } else {
                    setValues(translateProperty)
                }
                start()
            }
        }
    }

    private fun scrollFromAlpha(isToExpand: Boolean) = if (isToExpand) shrinkAlpha else expandAlpha

    private fun scrollToAlpha(isToExpand: Boolean) = if (isToExpand) expandAlpha else shrinkAlpha

    private fun preScrollSize(): Float? =
        (if (isHorizontonScroll()) this.width - scrollKeepSize else this.height - scrollKeepSize).takeIf { it > 0 }

    private fun assembleTranslateAnimProperty(scrollSize: Float) {
        translateProperty?.setFloatValues(scrollSize) ?: run {
            val propertyName = if (isHorizontonScroll()) "translationX" else "translationY"
            translateProperty = PropertyValuesHolder.ofFloat(propertyName, scrollSize)
        }
    }

    private fun assembleAlphaAnimProperty(fromAlpha: Float, toAlpha: Float) {
        alphaProperty?.run {
            setFloatValues(fromAlpha, toAlpha)
        } ?: run {
            if (alphaChangeEnable) {
                alphaProperty = PropertyValuesHolder.ofFloat("alpha", fromAlpha, toAlpha)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrollAnimator.cancel()
    }

    private fun getScrollSize(size: Float, isToExpand: Boolean): Float {
        return when (scrollDirection) {
            ScrollDirection.LEFT, ScrollDirection.TOP -> if (isToExpand) 0f else -size
            ScrollDirection.RIGHT, ScrollDirection.BOTTOM -> if (isToExpand) 0F else size
        }
    }

    fun isHorizontonScroll(): Boolean {
        return scrollDirection in arrayOf(ScrollDirection.LEFT, ScrollDirection.RIGHT)
    }

    fun isVerticalScroll(): Boolean {
        return scrollDirection in arrayOf(ScrollDirection.TOP, ScrollDirection.BOTTOM)
    }

    private enum class ScrollDirection {
        LEFT, TOP, RIGHT, BOTTOM
    }
}