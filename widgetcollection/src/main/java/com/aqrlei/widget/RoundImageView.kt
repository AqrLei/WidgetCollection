package com.aqrlei.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * Created by AqrLei on 2019-07-23
 */
open class RoundImageView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    private val path = Path()
    private val rectF = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
    }
    private var radii = FloatArray(8) { 0F }

    var roundAsCircle: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var topLeftCorner: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var topRightCorner: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var bottomLeftCorner: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var bottomRightCorner: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }

    var borderWidth: Float = 0F // 边框
        set(value) {
            if (field != value) {
                field = value
                paint.strokeWidth = field
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var borderColor = Color.TRANSPARENT
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }

    var dashWidth: Float = 0F // 虚线边框
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }
    var dashGap: Float = 0F
        set(value) {
            if (field != value) {
                field = value
                if (isActivated) {
                    invalidate()
                }
            }
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RoundImageView)?.apply {

            roundAsCircle = getBoolean(R.styleable.RoundImageView_riv_roundAsCircle, false)
            topLeftCorner = getDimension(R.styleable.RoundImageView_riv_roundedCornerRadius, 0f)
            topRightCorner = getDimension(R.styleable.RoundImageView_riv_roundedCornerRadius, 0f)
            bottomLeftCorner = getDimension(R.styleable.RoundImageView_riv_roundedCornerRadius, 0f)
            bottomRightCorner = getDimension(R.styleable.RoundImageView_riv_roundedCornerRadius, 0f)
            topLeftCorner =
                getDimension(R.styleable.RoundImageView_riv_topLeftCorner, topLeftCorner)
            topRightCorner =
                getDimension(R.styleable.RoundImageView_riv_topRightCorner, topRightCorner)
            bottomLeftCorner =
                getDimension(R.styleable.RoundImageView_riv_bottomLeftCorner, bottomLeftCorner)
            bottomRightCorner =
                getDimension(R.styleable.RoundImageView_riv_bottomRightCorner, bottomRightCorner)

            borderWidth = getDimension(R.styleable.RoundImageView_riv_borderWidth, 0f)
            borderColor = getColor(R.styleable.RoundImageView_riv_borderColor, borderColor)

            dashWidth = getDimension(R.styleable.RoundImageView_riv_dashWidth, dashWidth)
            dashGap = getDimension(R.styleable.RoundImageView_riv_dashGap, dashGap)

            recycle()
        }
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        if (dashWidth > 0) {
            paint.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
        }
    }


    override fun onDraw(canvas: Canvas) {
        path.reset()
        rectF.set(0F, 0F, width.toFloat(), height.toFloat())
        if (roundAsCircle) {
            val width = width
            val height = height
            val cx = width / 2f
            val cy = height / 2f
            val radius = cx.coerceAtLeast(cy)
            path.addCircle(cx, cy, radius, Path.Direction.CW)
        } else {
            radii[0] = topLeftCorner
            radii[1] = topLeftCorner
            radii[2] = topRightCorner
            radii[3] = topRightCorner
            radii[4] = bottomLeftCorner
            radii[5] = bottomLeftCorner
            radii[6] = bottomRightCorner
            radii[7] = bottomRightCorner

            path.addRoundRect(rectF, radii, Path.Direction.CW)
        }

        val save = canvas.save()
        canvas.clipPath(path)
        super.onDraw(canvas)
        paint.shader
        paint.color = borderColor
        canvas.drawPath(path, paint)

        canvas.restoreToCount(save)
    }
}