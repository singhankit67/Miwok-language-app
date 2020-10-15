package com.reelvideos.app.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;

import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.databinding.FragmentNotificationBinding;
import com.reelvideos.app.databinding.FragmentNotificationFollowerBinding;
import com.reelvideos.app.databinding.FragmentNotificationHashtagBinding;

public class FragmentNotiFollowers extends BaseFragment {
    FragmentNotificationFollowerBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentNotificationFollowerBinding.inflate ( inflater,container,false);
        initComponents ();
        return binding.getRoot ();
    }
    @Override
    public void initComponents() {

    }
    private void goBackToSearch() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }
}
