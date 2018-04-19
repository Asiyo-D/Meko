package com.loqoursys.meko.ui.onboarding;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by stephentuso on 11/16/15
 */
/* package */ public class WelcomeItemList extends ArrayList<OnWelcomeScreenPageChangeListener> implements OnWelcomeScreenPageChangeListener {

    /* package */
    public WelcomeItemList(OnWelcomeScreenPageChangeListener... items) {
        super(Arrays.asList(items));
    }

    /* package */
    public void addAll(OnWelcomeScreenPageChangeListener... items) {
        super.addAll(Arrays.asList(items));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        for (OnWelcomeScreenPageChangeListener changeListener : this) {
            changeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        for (OnWelcomeScreenPageChangeListener changeListener : this) {
            changeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        for (OnWelcomeScreenPageChangeListener changeListener : this) {
            changeListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        for (OnWelcomeScreenPageChangeListener changeListener : this) {
            changeListener.setup(config);
        }
    }
}
