package com.learninghorizon.mytube.adapter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import com.learninghorizon.mytube.fragment.SwipeTabFragment;
import com.learninghorizon.mytube.fragment.Tab1;
import com.learninghorizon.mytube.fragment.Tab2;


public class TabPagerAdapter extends FragmentPagerAdapter {

    Bundle bundle;
    String[] tabTitles;
    public TabPagerAdapter(FragmentManager fragmentManager,String[] tabTitles, Bundle bundle) {
        super(fragmentManager);
        this.tabTitles = tabTitles;
        this.bundle = bundle;
    }

    @Override
    public Fragment getItem(int index) {
        Fragment tab = null;
        int colorResId = 0;
        switch (index) {
            case 0:
                 tab = new Tab1();
                break;
            case 1:
                tab = new Tab2();
                break;
        }
        tab.setArguments(bundle);
        return tab;
    }

    @Override
    public int getCount(){
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return tabTitles[position];
    }
}