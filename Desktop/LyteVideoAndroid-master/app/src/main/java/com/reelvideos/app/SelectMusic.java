package com.reelvideos.app;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.reelvideos.app.Music.FragmentDiscoverMusic;
import com.reelvideos.app.Music.FragmentFavouriteMusic;
import com.reelvideos.app.Music.FragmentLocalMusic;
import com.reelvideos.app.Music.NestedMusicListAdapter;
import com.reelvideos.app.Music.ViewPagerAdapter;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.models.SelectMusicData;
//import com.reelvideos.app.utils.DialogCreator;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_SEARCH_SONGS;
import static com.reelvideos.app.config.Constants.APP_SHOWING_DIR;
import static com.reelvideos.app.utils.Utils.selected_music_is_local;


public class SelectMusic extends AppCompatActivity {

    private ImageButton backButton;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    TabLayout tabLayout;
    RelativeLayout relativeLayout;

    MaterialSearchBar searchBar;
    RecyclerView searchRecycler;


    private List<SelectMusicData> music;
    NestedMusicListAdapter music_Adapter;


    public static String selectedMusic = "";
    public static String selectedMusicName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_music);

        //DialogCreator.showProgressDialog(this);

        backButton = findViewById(R.id.select_music_imagebutton_back);
        backButton.setOnClickListener(v -> onBackPressed () );

        viewPager = findViewById(R.id.viewpager);
        relativeLayout = findViewById ( R.id.viewPagerLayout );
        searchRecycler = findViewById ( R.id.searchResult );
        searchBar = findViewById ( R.id.searchBar );

        searchBar.addTextChangeListener ( new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString ().length ()>0){
                    relativeLayout.setVisibility ( View.GONE );
                    searchRecycler.setVisibility ( View.VISIBLE );
                    doSearching ( s.toString () );
                } else {
                    relativeLayout.setVisibility ( View.VISIBLE );
                    searchRecycler.setVisibility ( View.GONE );
                }
            }
        } );




        tabLayout = findViewById(R.id.tabs);
        getTabs();
    }

    public void getTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        new Handler().post(() -> {
            viewPagerAdapter.addFragment(FragmentDiscoverMusic.getInstance(), getString(R.string.DISCOVER));
            viewPagerAdapter.addFragment(FragmentFavouriteMusic.getInstance(), getString(R.string.FAVOURITE));
            viewPagerAdapter.addFragment(FragmentLocalMusic.getInstance(), getString(R.string.My_Music));
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
        });
    }


    private void doSearching(String song) {
        LinearLayoutManager layoutManager = new LinearLayoutManager (this);
        searchRecycler.setLayoutManager(layoutManager);
        callSearchMusicAPI ( song );
    }
    private void callSearchMusicAPI(String song) {

        callNonAuthAPI(API_SEARCH_SONGS.replace ( "category=%query%&","" )+song, 0, new ApiResponseCallback () {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {

                loadUpList (jsonObject);

            }

            @Override
            public void onApiFailureResult(Exception e) {

            }
        });
    }


    private void loadUpList( JSONObject resultsData) {

        if (music != null)
            music.clear();

        SelectMusicData tempModel;

        music = new ArrayList<SelectMusicData> ();
        try {
            JSONArray jsonArray = resultsData.getJSONArray ( "results" );
            for (int i = 0; i < jsonArray.length(); i++) {
                tempModel = new SelectMusicData();
                JSONObject musicJSON = jsonArray.getJSONObject(i);

                tempModel.setTitle(musicJSON.optString("title"));
                tempModel.setMusicURL(musicJSON.optString("file"));
                tempModel.setId(musicJSON.optLong("id"));
                tempModel.setCreator(musicJSON.optLong("creator"));
                tempModel.setPosterURL ( musicJSON.optString ( "poster" ) );
                tempModel.setSinger ( musicJSON.optString ( "singer" ) );
                tempModel.setIs_localMusic ( false );

                music.add(tempModel);

            }


            //DialogCreator.cancelProgressDialog();
            music_Adapter = new NestedMusicListAdapter (this,music);
            searchRecycler.setAdapter(music_Adapter);
            music_Adapter.notifyDataSetChanged();


        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }


    }



    @Override
    protected void onPause() {
        if (NestedMusicListAdapter.mediaPlayer != null) {
            NestedMusicListAdapter.mediaPlayer.stop();
            NestedMusicListAdapter.mediaPlayer.release();
            NestedMusicListAdapter.mediaPlayer = null;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (NestedMusicListAdapter.mediaPlayer != null) {
            NestedMusicListAdapter.mediaPlayer.stop();
            NestedMusicListAdapter.mediaPlayer.release();
            NestedMusicListAdapter.mediaPlayer = null;
        }
        super.onBackPressed();
    }

    public void returnMusic() {
        if (NestedMusicListAdapter.mediaPlayer != null) {
            NestedMusicListAdapter.mediaPlayer.stop();
            NestedMusicListAdapter.mediaPlayer.release();
            NestedMusicListAdapter.mediaPlayer = null;
        }

        if (!selected_music_is_local) {
            downloadAudio(this, selectedMusic, "temp");
        } else {
            scanFile(this, selectedMusic);
            finishActivity();
        }

    }

    public void scanFile(Context context, String filename) {
        MediaScannerConnection.scanFile(context,
                new String[]{filename},
                null,
                (path, uri) -> Log.i("ExternalStorage", "Scanned " + path));
    }


    public void downloadAudio(Context context, String downloadPath, String audio) {

        //DialogCreator.showProgressDialog(context);
        String finalFileName = APP_SHOWING_DIR + "SHARED_" + audio + ".mp3";

        if (new File(finalFileName).exists()) {
            new File(finalFileName).delete();
            //DialogCreator.cancelProgressDialog();
        }

        ThinDownloadManager downloadManager = new ThinDownloadManager();
        Uri downloadUri = Uri.parse(downloadPath);
        Uri destinationUri = Uri.parse(finalFileName);

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadContext(CoreApp.getInstance().getApplicationContext()); //Optional

        downloadRequest.setStatusListener(new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                //DialogCreator.cancelProgressDialog();
                scanFile(context, finalFileName);
                finishActivity();
            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                //DialogCreator.cancelProgressDialog();
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                //DialogCreator.updateStatus(context.getString(R.string.downloading, progress + "%"));
            }
        });

        downloadManager.add(downloadRequest);

    }

    public void finishActivity() {
        Intent mReturn = new Intent();
        mReturn.putExtra("SELECTED_MUSIC", SelectMusic.selectedMusic);
        mReturn.putExtra("SELECTED_MUSIC_NAME", SelectMusic.selectedMusicName);
        setResult(RESULT_OK, mReturn);
        finish();
    }


}