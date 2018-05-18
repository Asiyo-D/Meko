package com.loqoursys.meko.ui.awesomebar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.loqoursys.meko.R
import com.loqoursys.meko.ui.awesomebar.shape.RoundRect


/**
 * Created by florentchampigny on 30/01/2017
 */

class ActionItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var clickListener: View.OnClickListener? = null

    private var roundRect: RoundRect? = null
    private var backgroundColor = Color.RED
    private var animateBeforeClick = true

    private var icon: ImageView
    private val text: TextView

    init {

        View.inflate(context, R.layout.bar_action_item, this)
        setWillNotDraw(false)

        icon = findViewById<ImageView>(R.id.action_icon) as ImageView
        text = findViewById<TextView>(R.id.action_text) as TextView

        setOnClickListener { v ->
            if (animateBeforeClick) {
                val animated = tryToAnimate()
                if (animated) {
                    postDelayed({
                        if (clickListener != null) {
                            clickListener!!.onClick(v)
                        }
                    }, 300)
                } else {
                    if (clickListener != null) {
                        clickListener!!.onClick(v)
                    }
                }
            } else {
                if (clickListener != null) {
                    clickListener!!.onClick(v)
                }
            }
        }
    }

    private fun tryToAnimate(): Boolean {
        val drawable = icon.drawable
        if (drawable is AnimatedVectorDrawable) {
            drawable.start()
            return true
        } else if (drawable is AnimatedVectorDrawableCompat) {
            drawable.start()
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        if (roundRect == null) {
            roundRect = RoundRect(width, height)
//            roundRect!!.setColor(backgroundColor)
            roundRect!!.color = backgroundColor
        }

        roundRect!!.drawOn(canvas)

        super.onDraw(canvas)
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        if (roundRect != null) {
//            roundRect..setColor(backgroundColor)
            roundRect!!.color = backgroundColor
        }
    }

    fun setText(actionName: String) {
        text.text = actionName
        postInvalidate()
    }

    fun getText(): String {
        return text.text.toString()
    }

    fun setDrawable(@DrawableRes drawable: Int?) {
        if (drawable == null) {
            icon.visibility = View.GONE
        } else {
            icon.setImageResource(drawable)
            icon.visibility = View.VISIBLE
        }
    }

    fun setAnimateBeforeClick(animateBeforeClick: Boolean) {
        this.animateBeforeClick = animateBeforeClick
    }

    fun setClickListener(onClickListener: View.OnClickListener) {
        this.clickListener = onClickListener
    }
}
