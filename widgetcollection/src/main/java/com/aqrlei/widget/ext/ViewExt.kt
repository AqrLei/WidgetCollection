package com.aqrlei.widget.ext

import android.view.View

/**
 * created by AqrLei on 2020/5/23
 */

private var lastClickTime: Long = 0L

fun View.setOnAvoidFastClickListener(
    clickInterVal: Long = 1000L,
    fastClickCallback: (() -> Unit)? = null,
    avoidFastClick: (v: View) -> Unit) {

    this.setOnClickListener {
        if (System.currentTimeMillis() - lastClickTime > clickInterVal) {
            lastClickTime = System.currentTimeMillis()
            avoidFastClick(this)
        } else {
            fastClickCallback?.invoke()
        }
    }
}