package com.aqrlei.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * Created by AqrLei on 2019-09-17
 */
private const val ADD_VIEW_TIPS = "ShadowLayout can host only one direct child"
class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var shadowCornerRadiusLT: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowBackground(width, height)
                }
            }
        }
    var shadowCornerRadiusRT: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowBackground(width, height)
                }
            }
        }
    var shadowCornerRadiusRB: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowBackground(width, height)
                }
            }
        }
    var shadowCornerRadiusLB: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowBackground(width, height)
                }
            }
        }
    var shadowSpreadRadius: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowColor: Int = Color.TRANSPARENT
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowBackground(width, height)
                }
            }
        }
    var shadowOffsetX: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowOffsetY: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowLeftShow: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowTopShow: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowRightShow: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }
    var shadowBottomShow: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                if (!isInitializing) {
                    setShadowPadding()
                }
            }
        }

    private var shadowBackground: Drawable = ColorDrawable(Color.TRANSPARENT)

    private var shadowBitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap? = null

    private val shadowPaint: Paint
    private val shadowPath = Path()

    private val childRectF = RectF()
    private val childPaint: Paint
    private val childBpPaint: Paint
    private val childPath = Path()

    private var shadowLeftPadding: Int = 0
    private var shadowTopPadding: Int = 0
    private var shadowRightPadding: Int = 0
    private var shadowBottomPadding: Int = 0

    private var isInitializing: Boolean = true

    private var radii = FloatArray(8) { 0F }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout)?.apply {


            shadowCornerRadiusLT =
                getDimension(R.styleable.ShadowLayout_shadowLeftTopCornerRadius, 0F)
            shadowCornerRadiusRT =
                getDimension(R.styleable.ShadowLayout_shadowRightTopCornerRadius, 0F)
            shadowCornerRadiusRB =
                getDimension(R.styleable.ShadowLayout_shadowRightBottomCornerRadius, 0F)
            shadowCornerRadiusLB =
                getDimension(R.styleable.ShadowLayout_shadowLeftBottomCornerRadius, 0F)
            shadowCornerRadiusLT =
                getDimension(R.styleable.ShadowLayout_shadowCornerRadius, shadowCornerRadiusLT)
            shadowCornerRadiusRT =
                getDimension(R.styleable.ShadowLayout_shadowCornerRadius, shadowCornerRadiusRT)
            shadowCornerRadiusRB =
                getDimension(R.styleable.ShadowLayout_shadowCornerRadius, shadowCornerRadiusRB)
            shadowCornerRadiusLB =
                getDimension(R.styleable.ShadowLayout_shadowCornerRadius, shadowCornerRadiusLB)

            shadowSpreadRadius =
                getDimension(R.styleable.ShadowLayout_shadowSpreadRadius, 0F)

            shadowBackground =
                getDrawable(R.styleable.ShadowLayout_shadowBackground) ?: shadowBackground

            shadowColor = getColor(R.styleable.ShadowLayout_shadowColor, shadowColor)
            shadowOffsetX = getDimension(R.styleable.ShadowLayout_shadowOffsetX, 0F)
            shadowOffsetY = getDimension(R.styleable.ShadowLayout_shadowOffsetY, 0F)

            shadowLeftShow = getBoolean(R.styleable.ShadowLayout_shadowLeftShow, true)
            shadowTopShow = getBoolean(R.styleable.ShadowLayout_shadowTopShow, true)
            shadowRightShow = getBoolean(R.styleable.ShadowLayout_shadowRightShow, true)
            shadowBottomShow = getBoolean(R.styleable.ShadowLayout_shadowBottomShow, true)


            recycle()
        }
        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isDither = true
            isFilterBitmap = true
            style = Paint.Style.FILL
        }
        childPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isDither = true
            isFilterBitmap = true
            style = Paint.Style.FILL
        }
        childBpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isDither = true
            isFilterBitmap = true
            style = Paint.Style.FILL
        }
        setShadowPadding()
    }

    override fun addView(child: View?) {
        check(childCount <= 0) { ADD_VIEW_TIPS }
        super.addView(child)
    }

    override fun addView(child: View?, index: Int) {
        check(childCount <= 0) { ADD_VIEW_TIPS }
        super.addView(child, index)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        check(childCount <= 0) { ADD_VIEW_TIPS }
        super.addView(child, params)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        check(childCount <= 0) { ADD_VIEW_TIPS }
        super.addView(child, index, params)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            setShadowBackground(w, h)
        }
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        val rect = Rect().also {
            it.left = shadowLeftPadding
            it.top = shadowTopPadding
            it.right = (width - shadowRightPadding)
            it.bottom = (height - shadowBottomPadding)
        }
        childRectF.set(rect)
        assembleRadii()
        backgroundBitmap = createChildBackgroundBitmap(
            childRectF.width().toInt(), childRectF.height().toInt(), shadowBackground
        )
        canvas?.drawFilter =
            PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        backgroundBitmap?.let {
            canvas?.drawBitmap(it, null, childRectF, childPaint)
        }
        childPath.reset()
        childPath.addRoundRect(childRectF, radii, Path.Direction.CW)
        if (child?.background != null) {
            canvas?.clipPath(childPath)
        }
        if (isInitializing) {
            this.post {
                isInitializing = false
            }
        }
        return super.drawChild(canvas, child, drawingTime)
    }

    private fun isSetShadowCornerRadius(): Boolean {
        return shadowCornerRadiusLT > 0F
                || shadowCornerRadiusRT > 0F
                || shadowCornerRadiusRB > 0F
                || shadowCornerRadiusLB > 0F
    }

    private fun setShadowPadding() {
        val xPadding = (shadowSpreadRadius + abs(shadowOffsetX)).toInt()
        val yPadding = (shadowSpreadRadius + abs(shadowOffsetY)).toInt()
        shadowLeftPadding = if (shadowLeftShow) xPadding else 0
        shadowTopPadding = if (shadowTopShow) yPadding else 0
        shadowRightPadding = if (shadowRightShow) xPadding else 0
        shadowBottomPadding = if (shadowBottomShow) yPadding else 0
        setPadding(shadowLeftPadding, shadowTopPadding, shadowRightPadding, shadowBottomPadding)
    }

    private fun setShadowBackground(w: Int, h: Int) {
        createShadowBitmap(w, h)?.run {
            background = BitmapDrawable(null, this)
        }
    }

    private fun createShadowBitmap(shadowW: Int, shadowH: Int): Bitmap? {
        shadowBitmap =
            Bitmap.createBitmap(shadowW, shadowH, Bitmap.Config.ARGB_8888).also { bitmap ->
                shadowPath.reset()
                val canvas = Canvas(bitmap)
                canvas.drawFilter =
                    PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                val tempRectF = RectF(
                    shadowSpreadRadius,
                    shadowSpreadRadius,
                    shadowW - shadowSpreadRadius,
                    shadowH - shadowSpreadRadius
                )
                if (shadowOffsetY > 0) {
                    tempRectF.top += shadowOffsetY
                    tempRectF.bottom -= shadowOffsetY

                } else if (shadowOffsetY < 0) {
                    tempRectF.top += abs(shadowOffsetY)
                    tempRectF.bottom -= abs(shadowOffsetY)
                }

                if (shadowOffsetX > 0) {
                    tempRectF.left += shadowOffsetX
                    tempRectF.right -= shadowOffsetX
                } else if (shadowOffsetX < 0) {
                    tempRectF.left += abs(shadowOffsetX)
                    tempRectF.right -= abs(shadowOffsetX)
                }
                shadowPaint.color = Color.TRANSPARENT
                if (!isInEditMode) {
                    shadowPaint.setShadowLayer(
                        shadowSpreadRadius,
                        shadowOffsetX,
                        shadowOffsetY,
                        shadowColor
                    )
                }
                assembleRadii()
                shadowPath.addRoundRect(tempRectF, radii, Path.Direction.CW)
                canvas.drawPath(shadowPath, shadowPaint)
            }
        return shadowBitmap
    }

    private fun createChildBackgroundBitmap(w: Int, h: Int, dr: Drawable): Bitmap {
        childPath.reset()
        val processBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bitmap ->
            dr.apply {
                setBounds(0, 0, w, h)
                val canvas = Canvas(bitmap)
                canvas.drawFilter =
                    PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                this.draw(canvas)
            }
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            val rect = Rect(0, 0, w, h)
            childPath.addRoundRect(RectF(rect), radii, Path.Direction.CW)
            val canvas = Canvas(this)
            canvas.drawFilter =
                PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val saveCount = canvas.saveLayer(RectF(rect), childBpPaint, Canvas.ALL_SAVE_FLAG)
            canvas.drawPath(childPath, childBpPaint)
            childBpPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(processBitmap, rect, rect, childBpPaint)
            childBpPaint.xfermode = null
            canvas.restoreToCount(saveCount)
        }
    }

    private fun assembleRadii() {
        when {
            isSetShadowCornerRadius() -> {
                if (shadowCornerRadiusLT > 0) {
                    radii[0] = shadowCornerRadiusLT
                    radii[1] = shadowCornerRadiusLT
                } else {
                    radii[0] = 0F
                    radii[1] = 0F
                }

                if (shadowCornerRadiusRT > 0) {
                    radii[2] = shadowCornerRadiusRT
                    radii[3] = shadowCornerRadiusRT
                } else {
                    radii[2] = 0F
                    radii[3] = 0F
                }

                if (shadowCornerRadiusRB > 0) {
                    radii[4] = shadowCornerRadiusRB
                    radii[5] = shadowCornerRadiusRB

                } else {
                    radii[4] = 0F
                    radii[5] = 0F
                }

                if (shadowCornerRadiusLB > 0) {
                    radii[6] = shadowCornerRadiusLB
                    radii[7] = shadowCornerRadiusLB
                } else {
                    radii[6] = 0F
                    radii[7] = 0F
                }
            }
            else -> {
                for (i in radii.indices) {
                    radii[i] = 0F
                }
            }
        }
    }

    private fun releaseBitmap() {
        shadowBitmap?.recycle()
        shadowBitmap = null
        backgroundBitmap?.recycle()
        backgroundBitmap = null
    }

    override fun onDetachedFromWindow() {
        releaseBitmap()
        super.onDetachedFromWindow()
    }
}