package com.loqoursys.meko.ui.awesomebar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.ActionMenuView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.loqoursys.meko.R

/**
 * Created by florentchampigny on 29/01/2017
 */

class AwesomeBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val radius: Int = resources.getDimensionPixelOffset(R.dimen.bar_radius)
    private var onMenuClickListener: View.OnClickListener? = null
    private var actionItemClickListener: ActionItemClickListener? = null
    private var overflowActionItemClickListener: OverflowActionItemClickListener? = null
    private val paintDark: Paint
    private val paintMain: Paint
    private val settings: Settings

    private val iconMenu: ImageView
    private val iconBack: ImageView
    private val iconApp: ImageView
    private val iconAppBackground: ImageView
    private val actionsLayout: ViewGroup

    private val actionMenuView: ActionMenuView

    private val menuAnimationHandler = MenuAnimationHandler()

    private var animRadius = 0f

    init {

        minimumHeight = resources.getDimensionPixelOffset(R.dimen.bar_min_height)

        settings = Settings()
        settings.init(context, attrs)


        setWillNotDraw(false)
        paintDark = Paint(Paint.ANTI_ALIAS_FLAG)
        paintDark.color = settings.colorDark
        paintDark.style = Paint.Style.FILL_AND_STROKE

        paintMain = Paint(Paint.ANTI_ALIAS_FLAG)
        paintMain.color = settings.colorMain
        paintMain.style = Paint.Style.FILL_AND_STROKE

        addView(LayoutInflater.from(context).inflate(R.layout.bar_layout, this, false))

        iconMenu = findViewById<View>(R.id.bar_menu_icon) as ImageView
        iconBack = findViewById<View>(R.id.bar_back_icon) as ImageView
        iconApp = findViewById<View>(R.id.bar_app_icon) as ImageView
        iconAppBackground = findViewById<View>(R.id.bar_app_icon_background) as ImageView
        actionsLayout = findViewById<View>(R.id.bar_actions_layout) as ViewGroup
        actionMenuView = findViewById<View>(R.id.bar_actions_menu_view) as ActionMenuView

        iconMenu.setImageDrawable(AnimatedVectorDrawableCompat.create(getContext(), R.drawable.awsb_ic_menu_animated))

        iconApp.setImageDrawable(getAppIcon(context))
        iconAppBackground.setImageDrawable(getAppIcon(context))
        iconAppBackground.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark))

        if (settings.isAnimateMenu) {
            menuAnimationHandler.sendEmptyMessageDelayed(MESSAGE_ANIMATION_START, DELAYT_BEFORE_FIRST_MENU_ANIMATION.toLong())
        }
        iconMenu.setOnClickListener {
            if (settings.isAnimateMenu) {
                animateMenuImage()
                animateDarkView()
                iconMenu.postDelayed({
                    if (onMenuClickListener != null) {
                        onMenuClickListener!!.onClick(iconMenu)
                    }
                }, DELAY_BETWEEN_MENU_ANIMATION_AND_CLICK.toLong())
            } else {
                if (onMenuClickListener != null) {
                    onMenuClickListener!!.onClick(iconMenu)
                }
            }
        }

        DrawableCompat.setTint(actionMenuView.overflowIcon!!, settings.colorMain)
        actionMenuView.visibility = View.GONE

        actionMenuView.setOnMenuItemClickListener { item ->
            if (overflowActionItemClickListener != null) {
                overflowActionItemClickListener!!.onOverflowActionItemClicked(item.order, item.title.toString())
            }
            true
        }

        iconBack.visibility = View.GONE
        iconBack.setOnClickListener {
            if (onMenuClickListener != null) {
                onMenuClickListener!!.onClick(iconBack)
            }
        }
    }

    private fun animateDarkView() {
        val minAlpha = 100
        val duration = 200
        val delay = 350

        val valueAnimator = ValueAnimator.ofFloat(0F, radius * RATIO_RADIUS_MIN_MAX)
        valueAnimator.interpolator = AccelerateInterpolator()
        valueAnimator.addUpdateListener { animation ->
            animRadius = animation.animatedValue as Float
            paintDark.alpha = 255 - (minAlpha * animation.animatedFraction).toInt()
            postInvalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                postDelayed({
                    val valueAnimator2 = ValueAnimator.ofFloat(radius * RATIO_RADIUS_MIN_MAX, 0F)
                    valueAnimator2.interpolator = AccelerateInterpolator()
                    valueAnimator2.addUpdateListener { animation ->
                        animRadius = animation.animatedValue as Float
                        paintDark.alpha = 255 - minAlpha + (minAlpha * animation.animatedFraction).toInt()
                        postInvalidate()
                    }
                    valueAnimator2.duration = duration.toLong()
                    valueAnimator2.start()
                }, delay.toLong())
            }
        })
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    private fun animateMenuImage() {
        val drawable = iconMenu.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            (iconMenu.drawable as AnimatedVectorDrawableCompat).start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        menuAnimationHandler.removeMessages(MESSAGE_ANIMATION_START)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cY = height / 2

        canvas.drawCircle(0f, cY.toFloat(), radius.toFloat(), paintMain)
        canvas.drawCircle(-radius * RATIO_RADIUS_MIN_MAX + animRadius, cY.toFloat(), radius.toFloat(), paintDark)
    }

    private fun getAppIcon(context: Context): Drawable {
        return context.packageManager.getApplicationIcon(context.applicationInfo)
    }

    fun addAction(@DrawableRes drawable: Int?, actionName: String) {
        val actionItem = ActionItem(context)
        actionItem.setText(actionName)
        actionItem.setDrawable(drawable)
        actionItem.setAnimateBeforeClick(settings.isAnimateMenu)
        actionItem.setBackgroundColor(settings.actionsColor)
        this.actionsLayout.addView(actionItem)

        actionItem.setClickListener(OnClickListener {
            if (actionItemClickListener != null) {
                actionItemClickListener!!.onActionItemClicked(actionsLayout.indexOfChild(actionItem), actionItem)
            }
        })

        val layoutParams = actionItem.layoutParams as LinearLayout.LayoutParams
        layoutParams.leftMargin = resources.getDimensionPixelOffset(R.dimen.bar_actions_margin_left)
        layoutParams.rightMargin = resources.getDimensionPixelOffset(R.dimen.bar_actions_margin_right)
        actionItem.layoutParams = layoutParams
    }

    fun clearActions() {
        this.actionsLayout.removeAllViews()
    }

    fun setOnMenuClickedListener(onMenuClickListener: View.OnClickListener) {
        this.onMenuClickListener = onMenuClickListener
    }

    fun setActionItemClickListener(actionItemClickListener: ActionItemClickListener) {
        this.actionItemClickListener = actionItemClickListener
    }

    fun addOverflowItem(item: String) {
        actionMenuView.menu.add(item)

        actionMenuView.visibility = View.VISIBLE
    }

    fun setOverflowActionItemClickListener(overflowActionItemClickListener: OverflowActionItemClickListener) {
        this.overflowActionItemClickListener = overflowActionItemClickListener
    }

    fun displayHomeAsUpEnabled(enabled: Boolean) {
        if (enabled) {
            this.iconBack.visibility = View.VISIBLE
            this.iconMenu.visibility = View.GONE
        } else {
            this.iconBack.visibility = View.GONE
            this.iconMenu.visibility = View.VISIBLE
        }
    }

    interface ActionItemClickListener {
        fun onActionItemClicked(position: Int, actionItem: ActionItem)
    }

    interface OverflowActionItemClickListener {
        fun onOverflowActionItemClicked(position: Int, item: String)
    }

    internal inner class MenuAnimationHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MESSAGE_ANIMATION_START -> {
                    val drawable = iconMenu.drawable
                    if (drawable is AnimatedVectorDrawableCompat) {
                        (iconMenu.drawable as AnimatedVectorDrawableCompat).start()
                        sendEmptyMessageDelayed(MESSAGE_ANIMATION_START, 5000)
                    }
                }
            }
        }
    }

    companion object {

        val DELAYT_BEFORE_FIRST_MENU_ANIMATION = 1000
        val DELAY_BETWEEN_MENU_ANIMATION_AND_CLICK = 800
        val MESSAGE_ANIMATION_START = 1
        val RATIO_RADIUS_MIN_MAX = 3f / 5f
    }
}
