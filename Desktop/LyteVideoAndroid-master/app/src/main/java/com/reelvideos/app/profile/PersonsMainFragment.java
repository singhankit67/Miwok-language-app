package com.reelvideos.app.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.R;
import com.reelvideos.app.adapters.TabsAdapter;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.databinding.FragmentPersonsBinding;


public class PersonsMainFragment extends BaseFragment {

    FragmentPersonsBinding binding;
    String userID;
    boolean followersTab;

    public PersonsMainFragment(String userID, boolean isFollowersTab) {
        this.userID = userID;
        this.followersTab = isFollowersTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPersonsBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    @Override
    public void initComponents() {
        setUpTabs();
        binding.backBtn.setOnClickListener(view -> activity.onBackPressed());
    }

    void setUpTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.followers).toUpperCase()));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.following).toUpperCase()));
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        TabsAdapter tabsAdapter = new TabsAdapter(getChildFragmentManager(), binding.tabLayout.getTabCount(), userID);
        binding.pager.setAdapter(tabsAdapter);
        binding.pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (followersTab) {
            binding.pager.setCurrentItem(0);
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
        } else {
            binding.pager.setCurrentItem(1);
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
        }
    }
}