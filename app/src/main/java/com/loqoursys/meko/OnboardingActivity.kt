package com.loqoursys.meko

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.loqoursys.meko.ui.onboarding.*
import kotlinx.android.synthetic.main.activity_onboarding.*
import utils.Preferences

class OnboardingActivity : AppCompatActivity() {

    private lateinit var context: Context
    private lateinit var config: WelcomeConfiguration

    private var colors = arrayListOf<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        context = this

        btn_skip.setOnClickListener { finishSetup() }
        btn_next.setOnClickListener { next(getNextPageIndex()) }

        colors.add(ContextCompat.getColor(context, R.color.colorWhite))
        colors.add(ContextCompat.getColor(context, R.color.colorAccent))
        colors.add(ContextCompat.getColor(context, R.color.primaryLight))

        config = WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(BackgroundColor(Color.WHITE))
                // .page(TitlePage(R.drawable.app_icon, getString(R.string.meko)))
                .page(BasicPage(R.drawable.food, "Order",
                        "Place orders on delicious home cooked meals, drinks and more")
                        .headerColor(Color.BLACK)
                        .descriptionColor(Color.BLACK)
                        .background(BackgroundColor(colors[0])))
                .page(BasicPage(R.drawable.delivery, "Delivery",
                        "Your order will be delivery promptly to your preferred location")
                        .headerColor(Color.WHITE)
                        .descriptionColor(Color.WHITE)
                        .background(BackgroundColor(colors[1])))
                .page(BasicPage(R.drawable.pay, "Payment",
                        "Pay when ordering or on delivery")
                        .headerColor(Color.WHITE)
                        .descriptionColor(Color.WHITE)
                        .background(BackgroundColor(colors[2])))
                .swipeToDismiss(false)
                .exitAnimation(android.R.anim.fade_out)
                .build()

        val responsiveItems = WelcomeItemList()
        responsiveItems.addAll(welcome_bg, config.pages)
        responsiveItems.setup(config)

        val adapter = OnboardingAdapter(supportFragmentManager)
        pager.adapter = adapter

        pager.addOnPageChangeListener(responsiveItems)
        pager.currentItem = config.firstPageIndex()

        pager_indicator.setViewPager(pager)
        pager_indicator.setSmoothTransition(true)
        pager_indicator.setIndicatorsClickChangePage(true)

        responsiveItems.onPageSelected(pager.currentItem)
        ButtonAdapter(pager)
        updateUI(pager.currentItem)

    }

    override fun onResume() {
        super.onResume()
        Preferences.loadPreferences(this)
    }

    private fun next(pos: Int) {
        pager.currentItem = pos
        updateUI(pos)
    }

    private fun updateUI(pos: Int) {
        when (pos) {
            2 -> {
                btn_skip.visibility = View.INVISIBLE
                btn_next.text = getString(R.string.done)
                btn_next.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            0, 1 -> {
                btn_next.text = getString(R.string.next)
                btn_skip.visibility = View.VISIBLE
                btn_next.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        R.drawable.ic_chevron_right_black_24dp, 0)
            }
        }

        if (pos > 2) {
            finishSetup()
        }
    }


    private fun getNextPageIndex(): Int {
        return pager.currentItem + if (config.isRtl) -1 else 1
    }

    private fun getPreviousPageIndex(): Int {
        return pager.currentItem + if (config.isRtl) 1 else -1
    }

    private fun canScrollToNextPage(): Boolean {
        return if (config.isRtl) getNextPageIndex() >= config.lastViewablePageIndex() else getNextPageIndex() <= config.lastViewablePageIndex()
    }

    private fun canScrollToPreviousPage(): Boolean {
        return if (config.isRtl) getPreviousPageIndex() <= config.firstPageIndex() else getPreviousPageIndex() >= config.firstPageIndex()
    }

    private fun finishSetup() {
        Preferences.setPreferenceInstall(false)
        startActivity(Intent(context, MainActivity::class.java))
        finishAffinity()
    }

    inner class OnboardingAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = config.createFragment(position)

        override fun getCount(): Int = config.pageCount()

    }

    inner class ButtonAdapter(pager: ViewPager) : ViewPager.OnPageChangeListener {
        private val viewPager: ViewPager = pager

        init {
            viewPager.addOnPageChangeListener(this)
        }

        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            invalidateButtons(position)
        }

        override fun onPageSelected(position: Int) {
            invalidateButtons(position)
        }

        private fun invalidateButtons(pos: Int) {
            updateUI(pos)
        }
    }


}
