package com.loqoursys.meko.ui

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import com.loqoursys.meko.listener.ObservableScrollable
import com.loqoursys.meko.listener.OnScrollChangedCallback

/**
 * Created by root on 5/7/18 for LoqourSys
 */
class ItemScrollView : NestedScrollView, ObservableScrollable {

    var mScrollChangedCallback: OnScrollChangedCallback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, intDefStyle: Int) : super(context, attrs, intDefStyle)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (mScrollChangedCallback != null) {
            mScrollChangedCallback?.onScroll(l, t)
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        // http://stackoverflow.com/a/6894270/244576
        return super.getTopFadingEdgeStrength()
    }

    override fun getBottomFadingEdgeStrength(): Float {
        // http://stackoverflow.com/a/6894270/244576
        return super.getBottomFadingEdgeStrength()
    }


    override fun setOnScrollChangedCallback(callback: OnScrollChangedCallback) {
        mScrollChangedCallback = callback
    }
}
