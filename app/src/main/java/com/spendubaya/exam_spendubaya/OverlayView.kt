package com.spendubaya.exam_spendubaya

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

class OverlayView(context: Context, packageName: String) {

    private val windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val view: View
    private val layoutParams: LayoutParams

    init {
        view = View(context)
        view.setBackgroundColor(Color.BLACK) // Warna overlay
        view.alpha = 0.5f // Tingkat transparansi overlay

        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY, // atau TYPE_SYSTEM_ALERT jika diperlukan dan diizinkan
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCHABLE, // agar overlay tidak menerima input
            android.graphics.PixelFormat.TRANSLUCENT
        )
    }

    fun show() {
        windowManager.addView(view, layoutParams)
    }

    fun hide() {
        windowManager.removeView(view)
    }
}