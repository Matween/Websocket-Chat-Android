package io.matic.websocketchat.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Matic on 13/09/2017.
 */

// adapter for fragments stores and returns fragments
public class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> pages = new ArrayList<>();

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    public void addPage(Fragment fragment) {
        pages.add(fragment);
    }
}
