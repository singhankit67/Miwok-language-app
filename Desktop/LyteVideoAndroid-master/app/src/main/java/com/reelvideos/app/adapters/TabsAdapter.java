package com.reelvideos.app.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.reelvideos.app.persons.FollowersFragment;
import com.reelvideos.app.persons.FollowingFragment;

public class TabsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    String userID;
    public TabsAdapter(FragmentManager fm, int tabs, String userID){
        super(fm);
        this.mNumOfTabs = tabs;
        this.userID = userID;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return new FollowersFragment(userID);
            case 1:
                return new FollowingFragment(userID);
            default:
                return null;
        }
    }
}