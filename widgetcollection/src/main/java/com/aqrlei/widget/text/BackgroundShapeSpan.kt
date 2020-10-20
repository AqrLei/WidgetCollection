package com.aqrlei.widget.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

/**
 * created by AqrLei on 2019-12-07
 */
class BackgroundShapeSpan @JvmOverloads constructor(
    private val textColor: Int,
    private val solidColor: Int = Color.TRANSPARENT,
    private val strokeColor: Int? = null,
    private val strokeWidth: Int = 1,
    private val cornerRadius: Float = 0F,
    private val paddingStart: Int = 0,
    private val paddingEnd: Int = 0,
    private val style: Style = Style.STROKE
) : ReplacementSpan() {
    private val mBgPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?): Int {
        return (paint.measureText(text, start, end) + paddingStart + paddingEnd).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint) {
        mBgPaint.color = solidColor
        mBgPaint.style = Paint.Style.FILL
        val size = getSize(paint, text, start, end, paint.fontMetricsInt)
        val padding = (size - paint.measureText(text.subSequence(start, end).toString())) / 2.0F
        val oval = RectF(
            x,
            y + paint.ascent() - padding,
            x + size,
            y + paint.descent() + padding)
        canvas.drawRoundRect(oval, cornerRadius, cornerRadius, mBgPaint)
        if (style == Style.STROKE) {
            mBgPaint.color = strokeColor ?: textColor
            mBgPaint.style = Paint.Style.STROKE
            mBgPaint.strokeWidth = strokeWidth.toFloat()
            canvas.drawRoundRect(oval, cornerRadius, cornerRadius, mBgPaint)
        }

        paint.color = textColor
        canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
    }

    enum class Style {
        FILL, STROKE
    }
}