package com.aqrlei.widget.recycler

import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class FlowLayoutManager : RecyclerView.LayoutManager() {

    private var verticalOffset: Int = 0
    private var firstVisiblePosition: Int = 0
    private var lastVisiblePosition: Int = 0
    private val itemRectArray = SparseArray<Rect>()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun isAutoMeasureEnabled(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (childCount == 0 && state.isPreLayout) {
            return
        }
        detachAndScrapAttachedViews(recycler)
        lastVisiblePosition = itemCount
        layoutChildren(recycler)
    }

    private fun layoutChildren(recycler: RecyclerView.Recycler) {
        layoutChildren(recycler, 0)
    }

    private fun layoutChildren(recycler: RecyclerView.Recycler, offsetY: Int): Int {
        var dy = offsetY
        preLayoutChild(recycler, offsetY)
        if (dy >= 0) {//上拉（只有上拉了之后，才有可能下拉）
            dy = pullUpLayoutChild(recycler, offsetY)
        } else {//下拉，子view的布局位置和上拉保存的是一致的
            pullDownLayoutChild(recycler, offsetY)
        }
        return dy
    }


    private fun preLayoutChild(recycler: RecyclerView.Recycler, offsetY: Int) {
        (childCount > 0 && offsetY != 0).takeIf { it }?.let { //滑动了
            for (i in 0 until childCount) {
                getChildAt(i)?.let { child ->
                    when {
                        //  向上越界,上方的子view滑出了显示区
                        offsetY > 0 && (getDecoratedBottom(child) - offsetY < paddingTop) -> {
                            removeAndRecycleView(child, recycler)
                            firstVisiblePosition++
                        }
                        //向下越界，下方的子view滑出了显示区
                        offsetY < 0 && (getDecoratedTop(child) - offsetY > height - paddingBottom) -> {
                            removeAndRecycleView(child, recycler)
                            lastVisiblePosition--
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun pullUpLayoutChild(recycler: RecyclerView.Recycler, offsetY: Int): Int {
        var dy = offsetY
        var topOffset = paddingTop
        var leftOffset = paddingLeft
        var lineMaxHeight = 0

        //上拉（只有上拉了之后，才有可能下拉）
        var minPos = firstVisiblePosition
        lastVisiblePosition = itemCount - 1//最后一个item的索引
        if (childCount > 0) {
            //取当前显示区的最后一个view
            getChildAt(childCount - 1)?.let { child ->
                minPos = getPosition(child) + 1 // 在 adapter 中的索引+1
                topOffset = getDecoratedTop(child)
                leftOffset = getDecoratedRight(child)
                lineMaxHeight = lineMaxHeight.coerceAtLeast(getDecoratedMeasuredVertical(child))
            }
        }

        for (i in minPos..lastVisiblePosition) { // 依据position从缓存中取出子view - 下边界外区域的子view
            recycler.getViewForPosition(i).let { child ->
                addView(child)
                measureChildWithMargins(child, 0, 0)
                if (leftOffset + getDecoratedMeasuredHorizontal(child) <= getHorizontalContentSpace()) {//当前行放得下
                    layoutDecoratedWithMargins(
                        child,
                        leftOffset,
                        topOffset,
                        leftOffset + getDecoratedMeasuredHorizontal(child),
                        topOffset + getDecoratedMeasuredVertical(child)
                    )
                    val rect = Rect(
                        leftOffset,
                        topOffset + verticalOffset,
                        leftOffset + getDecoratedMeasuredHorizontal(child),
                        topOffset + verticalOffset + getDecoratedMeasuredVertical(child)
                    )
                    itemRectArray.put(i, rect)
                    leftOffset += getDecoratedMeasuredHorizontal(child)
                    lineMaxHeight = lineMaxHeight.coerceAtLeast(getDecoratedMeasuredVertical(child))
                } else {
                    leftOffset = paddingLeft
                    topOffset += lineMaxHeight
                    lineMaxHeight = 0
                    // 如果是EXACTLY模式且超出高度，移除和回收子view
                    (heightMode == View.MeasureSpec.EXACTLY && topOffset - dy > height - paddingBottom)
                        .takeIf { it }?.let {
                            removeAndRecycleView(child, recycler)
                            lastVisiblePosition = i - 1
                        } ?: let {
                        layoutDecoratedWithMargins(
                            child,
                            leftOffset,
                            topOffset,
                            leftOffset + getDecoratedMeasuredHorizontal(child),
                            topOffset + getDecoratedMeasuredVertical(child)
                        )
                        val rect = Rect(
                            leftOffset,
                            topOffset + verticalOffset,
                            leftOffset + getDecoratedMeasuredHorizontal(child),
                            topOffset + verticalOffset + getDecoratedMeasuredVertical(child)
                        )
                        itemRectArray.put(i, rect)
                        leftOffset += getDecoratedMeasuredHorizontal(child)
                        lineMaxHeight =
                            lineMaxHeight.coerceAtLeast(getDecoratedMeasuredVertical(child))
                    }
                }
            }
        }

        //取布局后的最后一个子view
        getChildAt(childCount - 1)?.let { lastChild ->
            if (getPosition(lastChild) == itemCount - 1) {// 如果是所有的最后一个
                (height - paddingBottom - getDecoratedBottom(lastChild)).takeIf { it > 0 }?.run {
                    dy -= this
                }
            }
        }
        return dy
    }

    //下拉，子view的布局位置和上拉保存的是一致的
    private fun pullDownLayoutChild(recycler: RecyclerView.Recycler, offsetY: Int) {
        var maxPos = itemCount - 1
        firstVisiblePosition = 0
        if (childCount > 0) {
            getChildAt(0)?.let { child ->
                maxPos = getPosition(child) - 1
            }
        }
        for (i in maxPos downTo firstVisiblePosition) {//逆序布局，上边界外区域的子view
            val rect = itemRectArray.get(i)
            if (rect.bottom - verticalOffset - offsetY < paddingTop) {
                firstVisiblePosition = i + 1
                break
            } else {
                recycler.getViewForPosition(i).let { child ->
                    addView(child, 0)
                    measureChildWithMargins(child, 0, 0)
                    layoutDecoratedWithMargins(
                        child,
                        rect.left,
                        rect.top - verticalOffset,
                        rect.right,
                        rect.bottom - verticalOffset
                    )
                }
            }
        }
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State): Int {
        return if (dy == 0 || childCount == 0) {
            0
        } else {
            var actualOffset = dy
            if (verticalOffset + actualOffset < 0) {
                actualOffset = -verticalOffset
            } else if (actualOffset > 0) {
                getChildAt(childCount - 1)?.let { lastChild ->
                    if (getPosition(lastChild) == itemCount - 1) {
                        val gap = height - paddingBottom - getDecoratedBottom(lastChild)
                        actualOffset = when {
                            gap > 0 -> -gap
                            gap < 0 -> Math.min(actualOffset, -gap)
                            else -> 0
                        }
                    }
                }
            }
            actualOffset = layoutChildren(recycler, actualOffset)
            verticalOffset += actualOffset
            offsetChildrenVertical(-actualOffset)
            actualOffset
        }
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    private fun getDecoratedMeasuredVertical(view: View): Int {
        return (view.layoutParams as? RecyclerView.LayoutParams)?.run {
            getDecoratedMeasuredHeight(view) + topMargin + bottomMargin
        } ?: getDecoratedMeasuredHeight(view)
    }

    private fun getDecoratedMeasuredHorizontal(view: View): Int {
        return (view.layoutParams as? RecyclerView.LayoutParams)?.run {
            getDecoratedMeasuredWidth(view) + leftMargin + rightMargin
        } ?: getDecoratedMeasuredWidth(view)
    }

    private fun getHorizontalContentSpace(): Int = width - paddingLeft - paddingRight

}