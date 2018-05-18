package com.loqoursys.meko.ui.awesomebar.shape

/**
 * Created by florentchampigny on 29/01/2017
 */


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class RoundRect(val width: Int, val height: Int) : Shape() {
    private val x: Int = 0
    private val y: Int = 0

    override fun drawOn(canvas: Canvas) {
        if (isDisplayBorder) {
            drawRoundedRect(canvas, (x - BORDER_PADDING).toFloat(), (y - BORDER_PADDING).toFloat(), (x + width + BORDER_PADDING).toFloat(), (y + height + BORDER_PADDING).toFloat(), borderPaint)
        }
        drawRoundedRect(canvas, x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat(), paint)
    }

    companion object {

        val BORDER_PADDING = 30

        private fun drawRoundedRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
            val radius = (bottom - top) / 2

            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, radius, radius, paint)
        }
    }
}