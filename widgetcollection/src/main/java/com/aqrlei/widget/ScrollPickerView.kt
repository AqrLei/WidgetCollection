package com.aqrlei.widget

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Created by AqrLei on 2019-05-28
 */
class ScrollPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {
    companion object {
        private const val TASK_DELAY_MILLIS = 50L

        private val mResources = Resources.getSystem().displayMetrics
        private val DEFAULT_ITEM_TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, mResources)
        private val DEFAULT_ITEM_HINT_TEXT_SIZE = DEFAULT_ITEM_TEXT_SIZE
        private val DEFAULT_ITEM_PADDING = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, mResources)
        private val DEFAULT_DIVIDER_HEIGHT = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, mResources)

        private val DEFAULT_ITEM_SELECTED_COLOR = Color.parseColor("#0288ce")
        private val DEFAULT_ITEM_HINT_COLOR = Color.parseColor("#bbbbbb")
        private val DEFAULT_DIVIDER_COLOR = Color.parseColor("#83cde6")

        private const val DEFAULT_DIVIDER_OFFSET_FACTOR = 0F
    }

    private val containerView = LinearLayout(context)
    private val itemList = ArrayList<String>()
    private val scrollerTask: Runnable
    private var initY = 0
    private val delayTime = TASK_DELAY_MILLIS
    private var itemHeight = 0
    private var selectedIndex = 0

    private var viewWidth = 0

    private var displayItemCount = 0


    var onScrollPickerListener: OnScrollPickListener? = null

    var offset = 1


    private var itemTextSize: Float = DEFAULT_ITEM_TEXT_SIZE
    private var itemHintTextSize : Float = DEFAULT_ITEM_HINT_TEXT_SIZE
    private var itemHorizontalPadding: Float = DEFAULT_ITEM_PADDING
    private var itemVerticalPadding: Float = DEFAULT_ITEM_PADDING
    private var dividerHeight: Float = DEFAULT_DIVIDER_HEIGHT

    private var itemSelectedColor: Int = DEFAULT_ITEM_SELECTED_COLOR
    private var itemHintColor: Int = DEFAULT_ITEM_HINT_COLOR
    private var dividerColor: Int = DEFAULT_DIVIDER_COLOR

    private var dividerOffsetFactor: Float = DEFAULT_DIVIDER_OFFSET_FACTOR

    init {

        this.isVerticalScrollBarEnabled = false
        containerView.orientation = LinearLayout.VERTICAL
        this.addView(containerView)
        context.obtainStyledAttributes(attrs, R.styleable.ScrollPickerView).apply {
            offset = getInt(R.styleable.ScrollPickerView_item_offset, offset)
            itemTextSize = getDimension(R.styleable.ScrollPickerView_item_text_size, itemTextSize)
            itemHintTextSize = getDimension(R.styleable.ScrollPickerView_item_hint_text_size,itemTextSize)
            itemHorizontalPadding = getDimension(R.styleable.ScrollPickerView_item_padding, itemHorizontalPadding)
            itemVerticalPadding = getDimension(R.styleable.ScrollPickerView_item_padding, itemVerticalPadding)
            itemHorizontalPadding =
                getDimension(R.styleable.ScrollPickerView_item_horizontal_padding, itemHorizontalPadding)
            itemVerticalPadding = getDimension(R.styleable.ScrollPickerView_item_vertical_padding, itemVerticalPadding)

            itemSelectedColor = getColor(R.styleable.ScrollPickerView_item_selected_color, itemSelectedColor)
            itemHintColor = getColor(R.styleable.ScrollPickerView_item_hint_color, itemHintColor)
            dividerColor = getColor(R.styleable.ScrollPickerView_divider_color, dividerColor)
            dividerOffsetFactor =
                getFraction(R.styleable.ScrollPickerView_divider_offset_factor, 1, 1, dividerOffsetFactor)
            dividerHeight = getDimension(R.styleable.ScrollPickerView_divider_height, dividerHeight)
            recycle()
        }
        scrollerTask = object : Runnable {
            override fun run() {
                val newScrollY = scrollY
                if (initY - newScrollY == 0) {
                    val remainder = initY % itemHeight
                    val divider = initY / itemHeight
                    when {
                        remainder == 0 -> {
                            selectedIndex = divider + offset
                            onSelectedCallback()
                        }
                        remainder > itemHeight / 2 -> {
                            this@ScrollPickerView.post {
                                this@ScrollPickerView.smoothScrollTo(0, initY - remainder + itemHeight)
                                selectedIndex = divider + offset + 1
                                onSelectedCallback()
                            }
                        }
                        else -> {
                            this@ScrollPickerView.post {
                                this@ScrollPickerView.smoothScrollTo(0, initY - remainder)
                                selectedIndex = divider + offset
                                onSelectedCallback()
                            }
                        }
                    }
                } else {
                    initY = scrollY
                    this@ScrollPickerView.postDelayed(this, delayTime)
                }
            }
        }
    }

    private fun onSelectedCallback() {
        onScrollPickerListener?.onSelected(selectedIndex, itemList.get(selectedIndex))
    }

    fun setItems(list: List<String>) {
        itemList.clear()
        itemList.addAll(list)
        for (i in 0 until offset) {
            itemList.add(0, "")
            itemList.add("")
        }
        initData()
    }

    fun setSelection(position: Int) {
        selectedIndex = position + offset
        this@ScrollPickerView.post {
            this@ScrollPickerView.smoothScrollTo(0, position * itemHeight)
        }
    }

    fun getSelectedItem() = itemList[selectedIndex]
    fun getSelectedIndex() = selectedIndex - offset

    private fun initData() {
        displayItemCount = offset * 2 + 1
        for (item in itemList) {
            containerView.addView(createItemView(item))
        }
        refreshItemView(0)
    }

    private fun createItemView(item: String): TextView {
        val tv = TextView(context).apply {
            layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            isSingleLine = true
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize)
            text = item
            gravity = Gravity.CENTER
            val horizontalPadding = itemHorizontalPadding.toInt()
            val verticalPadding = itemVerticalPadding.toInt()
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        }
        val tempItemHeight = getItemViewMeasureHeight(tv)
        if (itemHeight < tempItemHeight) {
            itemHeight = tempItemHeight
            containerView.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * displayItemCount)
            this.layoutParams = this.layoutParams.apply {
                height = itemHeight * displayItemCount
            }
        }
        return tv
    }

    private fun getItemViewMeasureHeight(view: View): Int {
        val width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        view.measure(width, expandSpec)
        return view.measuredHeight
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        refreshItemView(t)
    }

    private fun refreshItemView(topScroll: Int) {
        var position = topScroll / itemHeight + offset
        val remainder = topScroll % itemHeight
        val divided = topScroll / itemHeight

        if (remainder == 0) {
            position = divided + offset
        } else {
            if (remainder > itemHeight / 2) {
                position = divided + offset + 1
            }
        }

        val childSize = containerView.childCount
        for (i in 0 until childSize) {
            val itemView = containerView.getChildAt(i) as? TextView ?: return
            if (position == i) {
                itemView.setTextColor(itemSelectedColor)
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX,itemTextSize)
                val horizontalPadding = itemHorizontalPadding.toInt()
                val verticalPadding = itemVerticalPadding.toInt()
                itemView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            } else {
                itemView.setTextColor(itemHintColor)
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX,itemHintTextSize)
                val horizontalHintPadding = itemHorizontalPadding.toInt()
                val verticalHintPadding = (itemVerticalPadding + (itemTextSize  - itemHintTextSize) / 2).toInt()
                itemView.setPadding(horizontalHintPadding, verticalHintPadding, horizontalHintPadding, verticalHintPadding)
            }
        }
    }

    override fun setBackground(background: Drawable?) {
        if (viewWidth == 0) {//如果 宽度还未得到则默认取屏幕宽度
            val display = (context as? Activity)?.windowManager?.defaultDisplay
            val outSize = Point(1, 1)
            display?.getSize(outSize)
            viewWidth = outSize.x
        }
        val lineBackground = drawBackgroundDividerLine()
        super.setBackground(lineBackground)
    }

    private fun drawBackgroundDividerLine(): Drawable {
        return object : Drawable() {
            override fun draw(canvas: Canvas) {
                val paint = Paint()
                paint.color = dividerColor
                paint.strokeWidth = dividerHeight
                canvas.drawLine(
                    (viewWidth * dividerOffsetFactor),
                    obtainSelectedAreaBorder()[0].toFloat(),
                    (viewWidth * (1f - dividerOffsetFactor)),
                    obtainSelectedAreaBorder()[0].toFloat(),
                    paint
                )
                canvas.drawLine(
                    (viewWidth * dividerOffsetFactor),
                    obtainSelectedAreaBorder()[1].toFloat(),
                    (viewWidth * (1f - dividerOffsetFactor)),
                    obtainSelectedAreaBorder()[1].toFloat(),
                    paint
                )
            }

            override fun setAlpha(alpha: Int) {}

            override fun setColorFilter(colorFilter: ColorFilter?) {}

            override fun getOpacity(): Int = PixelFormat.UNKNOWN
        }
    }

    private val selectedAreaBorder = Array(2) { 0 }
    private fun obtainSelectedAreaBorder(): Array<Int> {
        selectedAreaBorder[0] = itemHeight * offset
        selectedAreaBorder[1] = itemHeight * (offset + 1)
        return selectedAreaBorder
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        this.background = null
    }

    override fun fling(velocityY: Int) {
        super.fling(velocityY / 3)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            startScrollTask()
        }
        return super.onTouchEvent(ev)
    }

    private fun startScrollTask() {
        initY = scrollY
        this.postDelayed(scrollerTask, delayTime)
    }

    interface OnScrollPickListener {
        fun onSelected(selectedIndex: Int, item: String)
    }
}