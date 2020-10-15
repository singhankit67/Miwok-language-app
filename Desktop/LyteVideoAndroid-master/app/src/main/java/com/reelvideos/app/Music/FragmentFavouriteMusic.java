package com.reelvideos.app.Music;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.reelvideos.app.databinding.FragmentMusicdiscoverBinding;
import com.reelvideos.app.databinding.FragmentMusicfavouriteBinding;


public class FragmentFavouriteMusic extends Fragment {

    FragmentMusicfavouriteBinding binding;

    public static FragmentFavouriteMusic getInstance(){
        FragmentFavouriteMusic fragmentFavouriteMusic = new FragmentFavouriteMusic ();
        return fragmentFavouriteMusic;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach ( context );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMusicfavouriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
