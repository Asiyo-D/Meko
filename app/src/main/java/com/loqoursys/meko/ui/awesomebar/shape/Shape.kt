package com.loqoursys.meko.ui.awesomebar.shape

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Created by florentchampigny on 29/01/2017
 */

abstract class Shape {
    var color = Color.argb(0, 0, 0, 0)
        set(color) {
            field = color
            this.paint.color = this.color
        }
    var paint: Paint
        protected set

    var borderColor = Color.parseColor("#AA999999")
        set(borderColor) {
            field = borderColor
            this.paint.color = borderColor
        }
    val borderPaint: Paint

    var isDisplayBorder = false

    init {
        this.paint = Paint()
        this.paint.color = color
        this.paint.isAntiAlias = true
        this.paint.style = Paint.Style.FILL_AND_STROKE
        //this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        this.borderPaint = Paint()
        this.borderPaint.isAntiAlias = true
        this.borderPaint.color = this.borderColor
    }

    abstract fun drawOn(canvas: Canvas)
}