package com.reelvideos.app.Music;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.reelvideos.app.databinding.FragmentMusiclocalBinding;
import com.reelvideos.app.models.MusicCategoryModel;
import com.reelvideos.app.models.SelectMusicData;

import java.util.ArrayList;
import java.util.List;

public class FragmentLocalMusic extends Fragment {
    FragmentMusiclocalBinding binding;

    private static final int MY_PERMISSION_REQUEST=1;


    NestedMusicListAdapter musicAdapter;
    List<SelectMusicData> MusicData = new ArrayList<> ();

    public static FragmentLocalMusic getInstance(){
        FragmentLocalMusic fragmentLocalMusic = new FragmentLocalMusic ();
        return fragmentLocalMusic;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach ( context );
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMusiclocalBinding.inflate(inflater, container, false);

        checkPermission();
        return binding.getRoot();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission ( getActivity (),
                Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale ( getActivity (),
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions ( getActivity (),
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions ( getActivity (),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
        } else {
            doStuff();
        }
    }

    private void doStuff() {
        binding.LocalMusicRecycler.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext ());
        binding.LocalMusicRecycler.setLayoutManager(layoutManager);
        getMusicFromStorage ();
        musicAdapter = new NestedMusicListAdapter (getContext (), MusicData);
        musicAdapter.notifyDataSetChanged ();
        binding.LocalMusicRecycler.setAdapter ( musicAdapter );

    }

    public void getMusicFromStorage() {
        ContentResolver contentResolver =getActivity ().getContentResolver ();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor songCursor = contentResolver.query ( songUri,null,null,null,null,null );

        if (songCursor != null && songCursor.moveToFirst ()){
            int songTitle = songCursor.getColumnIndex ( MediaStore.Audio.Media.TITLE );
            int songArtist = songCursor.getColumnIndex ( MediaStore.Audio.Media.ARTIST );
            int songUrl = songCursor.getColumnIndex ( MediaStore.Audio.Media.DATA );


            SelectMusicData tempModel;

            do {
                String currentTitle = songCursor.getString ( songTitle );
                String currentArtist = songCursor.getString ( songArtist );
                String currentUrl = songCursor.getString ( songUrl );
                tempModel = new SelectMusicData ();
                tempModel.setTitle ( currentTitle );
                tempModel.setSinger ( currentArtist );
                tempModel.setMusicURL ( currentUrl );
                tempModel.setIs_localMusic ( true );

                MusicData.add ( tempModel );

            }while (songCursor.moveToNext ());


        }
        songCursor.close ();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission ( getActivity (),
                            Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){

                        doStuff();
                    }
                } else {
                    Toast.makeText ( getActivity (), "Permission Not granted", Toast.LENGTH_SHORT ).show ();
                }
                return;
            }
        }
    }
}
