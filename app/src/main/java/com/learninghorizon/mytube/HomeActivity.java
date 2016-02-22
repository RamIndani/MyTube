package com.learninghorizon.mytube;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.learninghorizon.mytube.adapter.TabPagerAdapter;
import com.learninghorizon.mytube.fragment.Tab1;
import com.learninghorizon.mytube.fragment.Tab2;
import com.learninghorizon.mytube.ui.widget.SlidingTabLayout;
import com.learninghorizon.mytube.util.DataHolder;

public class HomeActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener  {

    private static final String TAG = "HomeActivity";
    ViewPager viewPager;
    TabPagerAdapter tabPagerAdapter;
    Toolbar toolbar;
    SlidingTabLayout slidingTabLayout;
    private String[] tabs = {"Search","PlayList"};
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
         toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.pager);
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),tabs,getIntent().getExtras());
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(tabPagerAdapter);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        slidingTabLayout.setViewPager(viewPager);


    }

    @Override
    public void onNewIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "Search query : " + query);
            FragmentManager fragmentManager = getFragmentManager();
            viewPager.setCurrentItem(0);
            Tab1 tab1 = (Tab1) tabPagerAdapter.getItem(0);
            tab1.setSearchQuery(query, getApplicationContext(), this);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        }

        if (viewPager.getCurrentItem()==0){
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else if(viewPager.getCurrentItem()==1) {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(true);
        }
        return true;
    }

    public void signout(MenuItem item){

        mGoogleApiClient = DataHolder.getmGoogleApiClient();
            // Clear the default account so that GoogleApiClient will not automatically
            // connect in the future.
        //Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("signout", true);
       // startActivity(intent);
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //invalidateOptionsMenu();
    }

    @Override
    public void onPageSelected(int position) {
        invalidateOptionsMenu();


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}