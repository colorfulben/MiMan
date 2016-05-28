package com.flintsoft.miman;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Xin on 2015/12/21.
 */
public class MainPageFragmentAdapter extends FragmentPagerAdapter {

    public MainPageFragmentAdapter(FragmentManager fm, Context ctx) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 0;

    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }
}
