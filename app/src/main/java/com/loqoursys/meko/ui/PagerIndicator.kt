package com.gausio.symbol.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.loqoursys.meko.R

/**
 * Created on 9/5/2017.
 */

class PagerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), ViewPager.OnPageChangeListener {

    private var mSelectedDrawable: Drawable? = null
    private var mUnSelectedDrawable: Drawable? = null

    private var mUnSelectedBitmap: Bitmap? = null
    private var mSelectedBitmap: Bitmap? = null

    private var mRect: Rect? = null
    private var mTempRect: Rect? = null

    // Default size
    private var mIndicatorSize = 13
    private var mIndicatorSelectedSize = 13

    // Default padding between indicators
    private var mPaddingBetweenIndicators = 7

    private var mNumOfIndicators = 3
    private var mSelectedIndicator = 0

    private var mLeftBound: Int = 0
    private var mTopBound: Int = 0
    private var mTotalWidthWeNeed: Int = 0
    private var mIndicatorsClickChangePage: Boolean = false
    private var mViewPager: ViewPager? = null

    private var mOnIndicatorClickListener: OnIndicatorClickListener? = null
    private var mSmoothTransitionEnabled: Boolean = false
    private var mCurrentPositionOffset: Float = 0.toFloat()
    private var mCurrentPosition: Float = 0.toFloat()
    private var mSelectedRect: Rect? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        setDefaults(context)
        getDataFromAttributes(context, attrs)
        convertDrawablesToBitmaps()

        mTempRect = Rect()
    }

    private fun setDefaults(context: Context) {
        val density = resources.displayMetrics.density

        mIndicatorSize = (mIndicatorSize * density).toInt()
        mIndicatorSelectedSize = (mIndicatorSelectedSize * density).toInt()

        mPaddingBetweenIndicators = (mPaddingBetweenIndicators * density).toInt()

        mSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_page_selected)
        mUnSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_page_unselected)
    }

    private fun getDataFromAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PagerIndicator, 0, 0)

            // Get indicators if exist
            val selectedDrawable = a.getDrawable(R.styleable.PagerIndicator_selectedDrawable)
            if (selectedDrawable != null) {
                mSelectedDrawable = selectedDrawable
            }
            val unSelectedDrawable = a.getDrawable(R.styleable.PagerIndicator_unSelectedDrawable)
            if (unSelectedDrawable != null) {
                mUnSelectedDrawable = unSelectedDrawable
            }

            // Get indicator size
            mIndicatorSize = a.getDimension(R.styleable.PagerIndicator_indicatorSize, mIndicatorSize.toFloat()).toInt()
            mIndicatorSelectedSize = a.getDimension(R.styleable.PagerIndicator_indicatorSelectedSize, mIndicatorSize.toFloat()).toInt()

            // Get padding between indicators
            mPaddingBetweenIndicators = a.getDimension(R.styleable.PagerIndicator_paddingBetweenIndicators, mPaddingBetweenIndicators.toFloat()).toInt()

            // Get number of indicators
            mNumOfIndicators = a.getInteger(R.styleable.PagerIndicator_numberOfIndicators, mNumOfIndicators)

            // Get selected indicator
            mSelectedIndicator = a.getInteger(R.styleable.PagerIndicator_selectedIndicator, mSelectedIndicator)

            a.recycle()
        }
    }

    private fun convertDrawablesToBitmaps() {
        mRect = Rect(0, 0, mIndicatorSize, mIndicatorSize)
        mUnSelectedBitmap = drawableToBitmap(mUnSelectedDrawable, mIndicatorSize)

        mSelectedRect = Rect(0, 0, mIndicatorSelectedSize, mIndicatorSelectedSize)
        mSelectedBitmap = drawableToBitmap(mSelectedDrawable, mIndicatorSelectedSize)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)


        var desiredWidth = (mIndicatorSize * mNumOfIndicators
                + mPaddingBetweenIndicators * (mNumOfIndicators - 1)
                + mIndicatorSelectedSize) - mIndicatorSize
        var desiredHeight = Math.max(mIndicatorSize, mIndicatorSelectedSize)

        desiredWidth += paddingLeft + paddingRight
        desiredHeight += paddingTop + paddingBottom

        var width = desiredWidth
        var height = desiredHeight


        //Measure Width
        if (widthMode == View.MeasureSpec.EXACTLY) {
            width = widthSize  // Must be this size (match_parent or exactly value)
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize) // (wrap_content)
        }

        //Measure Height
        if (heightMode == View.MeasureSpec.EXACTLY) {
            height = heightSize // Must be this size (match_parent or exactly value)
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize)   // (wrap_content)
        }

        changeIndicatorSizeIfNecessary(width - paddingLeft - paddingRight, height - paddingTop - paddingBottom)

        setMeasuredDimension(width, height)
    }

    private fun changeIndicatorSizeIfNecessary(width: Int, height: Int) {
        var widthInd = width
        var isIndicatorSizeChanged = false

        val diffWithSelectedSize = mIndicatorSelectedSize - mIndicatorSize
        val allIndicatorsWidth = mIndicatorSize * mNumOfIndicators + diffWithSelectedSize
        val paddingBetweenIndicators = (mNumOfIndicators - 1) * mPaddingBetweenIndicators

        // if width is not wide enough
        if (allIndicatorsWidth + paddingBetweenIndicators > widthInd) {
            widthInd -= paddingBetweenIndicators - diffWithSelectedSize
            mIndicatorSize = widthInd / mNumOfIndicators
            isIndicatorSizeChanged = true
        }

        // if height is not high enough
        if (mIndicatorSize > height) {
            mIndicatorSize = height
            isIndicatorSizeChanged = true
        }

        if (isIndicatorSizeChanged) {
            convertDrawablesToBitmaps()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val hCenter = (width - paddingLeft - paddingRight) / 2
        val vCenter = (height - paddingTop - paddingBottom) / 2

        val diffWithSelectedSize = mIndicatorSelectedSize - mIndicatorSize
        val allIndicatorsWidth = mIndicatorSize * mNumOfIndicators + diffWithSelectedSize
        val paddingBetweenIndicators = (mNumOfIndicators - 1) * mPaddingBetweenIndicators

        mTotalWidthWeNeed = allIndicatorsWidth + paddingBetweenIndicators
        mLeftBound = hCenter - mTotalWidthWeNeed / 2 + paddingLeft
        //mLeftBound = hCenter - mTotalWidthWeNeed / 2 + Math.round(getPaddingLeft() / mDensity);
        mTopBound = vCenter - mIndicatorSize / 2 + paddingTop
        val topSelectedBound = vCenter - mIndicatorSelectedSize / 2 + paddingTop
        //mTopBound = vCenter - mIndicatorSize / 2 + Math.round(getPaddingTop() / mDensity);

        mRect!!.offsetTo(mLeftBound, mTopBound)
        mSelectedRect!!.offsetTo(mLeftBound, topSelectedBound)
        for (i in 0 until mNumOfIndicators) {
            var offset = 0

            //Log.e("ZAQ", "mSelectedIndicator: " + mSelectedIndicator + " mCurrentPosition: " + mCurrentPosition);
            if (i == mSelectedIndicator) {
                offset = mIndicatorSelectedSize - mIndicatorSize
            }

            if (i != mSelectedIndicator || mSmoothTransitionEnabled) {
                canvas.drawBitmap(mUnSelectedBitmap!!, null, mRect!!, null)
            } else {
                canvas.drawBitmap(mSelectedBitmap!!, null, mSelectedRect!!, null)
            }

            if (i.toFloat() == mCurrentPosition && mSmoothTransitionEnabled) {
                mTempRect!!.set(mSelectedRect)
            }

            mRect!!.offset(mIndicatorSize + offset + mPaddingBetweenIndicators, 0)
            mSelectedRect!!.offset(mIndicatorSize + offset + mPaddingBetweenIndicators, 0)

        }

        if (mSmoothTransitionEnabled) {
            val offset = Math.round((mIndicatorSize + mPaddingBetweenIndicators) * mCurrentPositionOffset)
            mTempRect!!.offset(offset, 0)
            canvas.drawBitmap(mSelectedBitmap!!, null, mTempRect!!, null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (mIndicatorsClickChangePage && mOnIndicatorClickListener != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    val indicatorNumberClicked = getNumberOfCLickedIndicator(x, y)
                    if (indicatorNumberClicked > -1) {
                        onIndicatorClick(indicatorNumberClicked)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                }
            }
            return true
        }
        return false
    }

    private fun getNumberOfCLickedIndicator(x: Int, y: Int): Int {
        if (y < mTopBound - mIndicatorSize
                || y > mTopBound + mIndicatorSize
                || x < mLeftBound - mPaddingBetweenIndicators / 2
                || x > mLeftBound + mTotalWidthWeNeed + mPaddingBetweenIndicators / 2) {
            return -1
        }

        var x2 = x - mLeftBound
        val firstIndicatorWidth = mIndicatorSize + mPaddingBetweenIndicators / 2

        if (x2 < firstIndicatorWidth) {
            return 0
        }

        var count = 1
        x2 -= firstIndicatorWidth
        val indicatorWidth = mIndicatorSize + mPaddingBetweenIndicators

        while (x2 > indicatorWidth) {
            count++
            x2 -= indicatorWidth
        }

        return count
    }

    private fun onIndicatorClick(indicatorNumberClicked: Int) {
        if (mIndicatorsClickChangePage && mViewPager != null) {
            mViewPager!!.currentItem = indicatorNumberClicked
        }

        if (mOnIndicatorClickListener != null) {
            mOnIndicatorClickListener!!.onClick(indicatorNumberClicked)
        }
    }


    // Control
    private fun setSelectedIndicator(selectedIndicator: Int) {
        mSelectedIndicator = selectedIndicator
        invalidate()
    }

    fun setViewPager(viewPager: ViewPager) {
        mViewPager = viewPager

        mNumOfIndicators = mViewPager!!.adapter!!.count
        mSelectedIndicator = mViewPager!!.currentItem
        mViewPager!!.addOnPageChangeListener(this)

        invalidate()
    }

    fun setSmoothTransition(newValue: Boolean) {
        mSmoothTransitionEnabled = newValue
    }


    fun setIndicatorsClickChangePage(newValue: Boolean) {
        mIndicatorsClickChangePage = newValue
    }

    fun setIndicatorsClickListener(listener: OnIndicatorClickListener) {
        mOnIndicatorClickListener = listener
    }

    // ViewPager
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (mSmoothTransitionEnabled) {
            mCurrentPositionOffset = positionOffset
            mCurrentPosition = position.toFloat()
            invalidate()
        }
    }

    override fun onPageSelected(position: Int) {
        setSelectedIndicator(position)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    fun setSelectedDrawable(drawable: Drawable) {
        mSelectedDrawable = drawable
        convertDrawablesToBitmaps()
        invalidate()
    }

    fun setUnSelectedDrawable(drawable: Drawable) {
        mUnSelectedDrawable = drawable
        convertDrawablesToBitmaps()
        invalidate()
    }

    // Click listener interface
    interface OnIndicatorClickListener {
        fun onClick(indicatorNumber: Int)
    }

    companion object {

        // Helper
        fun drawableToBitmap(drawable: Drawable?, size: Int): Bitmap {
            var sizeInd = size

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            if (sizeInd < 1) {
                sizeInd = 1
            }

            val bitmap = Bitmap.createBitmap(sizeInd, sizeInd, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable!!.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }

}
