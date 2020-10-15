package com.reelvideos.app.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.reelvideos.app.Music.ShowAllMusicFragment;
import com.reelvideos.app.R;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.databinding.FragmentNotificationBinding;

public class NotificationFragment extends BaseFragment {
    FragmentNotificationBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentNotificationBinding.inflate ( inflater,container,false);
        initComponents ();
        return binding.getRoot ();
    }
    @Override
    public void initComponents() {

//        binding.followersLayout.setOnClickListener ( v -> follower() );
//        binding.likesLayout.setOnClickListener ( v -> likes() );
//        binding.commentLayout.setOnClickListener ( v -> comment() );
//        binding.hashTagLayout.setOnClickListener ( v -> hashtag() );

    }

    private void hashtag() {
        FragmentNotiHashtags fragmentNotiHashtags = new FragmentNotiHashtags ();
        AppCompatActivity activity=(AppCompatActivity)getContext ();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.Notification_container, fragmentNotiHashtags, fragmentNotiHashtags.getClass().getSimpleName()).addToBackStack(null).commit();
    }

    private void comment() {
        FragmentNotiComment fragmentNotiComment = new FragmentNotiComment ();
        AppCompatActivity activity=(AppCompatActivity)getContext ();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.Notification_container, fragmentNotiComment, fragmentNotiComment.getClass().getSimpleName()).addToBackStack(null).commit();
    }

    private void likes() {
        FragmentNotiLikes fragmentNotiLikes = new FragmentNotiLikes ();
        AppCompatActivity activity=(AppCompatActivity)getContext ();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.Notification_container, fragmentNotiLikes, fragmentNotiLikes.getClass().getSimpleName()).addToBackStack(null).commit();
    }

    private void follower() {
        FragmentNotiFollowers fragmentNotiFollowers = new FragmentNotiFollowers ();
        AppCompatActivity activity=(AppCompatActivity)getContext ();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.Notification_container, fragmentNotiFollowers, fragmentNotiFollowers.getClass().getSimpleName()).addToBackStack(null).commit();
    }
}
