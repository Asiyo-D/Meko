package com.loqoursys.meko.ui.awesomebar

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import com.loqoursys.meko.R

/**
 * Created by florentchampigny on 30/01/2017
 */

class Settings {

    var colorDark: Int = 0
    var colorMain: Int = 0
    var actionsColor: Int = 0
    var isAnimateMenu: Boolean = false

    fun init(context: Context, attrs: AttributeSet?) {
        colorDark = fetchPrimaryDarkColor(context)
        colorMain = fetchPrimaryColor(context)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AwesomeBar)

            colorMain = a.getColor(R.styleable.AwesomeBar_bar_primaryColor, colorMain)
            colorDark = a.getColor(R.styleable.AwesomeBar_bar_primaryDarkColor, colorDark)
            isAnimateMenu = a.getBoolean(R.styleable.AwesomeBar_bar_animatedIcons, true)
            actionsColor = a.getColor(R.styleable.AwesomeBar_bar_actionsColor, colorMain)

            a.recycle()
        }
    }

    private fun fetchPrimaryColor(context: Context): Int {
        val typedValue = TypedValue()

        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        val color = a.getColor(0, 0)

        a.recycle()

        return color
    }

    private fun fetchPrimaryDarkColor(context: Context): Int {
        val typedValue = TypedValue()

        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
        val color = a.getColor(0, 0)

        a.recycle()

        return color
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}
