package com.reelvideos.app.Music;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.databinding.FragmentMusicdiscoverBinding;
import com.reelvideos.app.databinding.FragmentSavedVideosBinding;
import com.reelvideos.app.models.MusicCategoryModel;
import com.reelvideos.app.models.SelectMusicData;
import com.reelvideos.app.profile.SavedVideosFragment;
import com.reelvideos.app.utils.DialogCreator;
//import com.reelvideos.app.utils.DialogCreator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_CATEGORY_WISE_SONGS;
import static com.reelvideos.app.config.Constants.API_SONG_CATEGORIES;


public class FragmentDiscoverMusic extends Fragment {
    FragmentMusicdiscoverBinding binding;



    private JSONArray jsonArray;

    private ArrayList<SelectMusicData> music;

    MusicAdapter musicAdapter;
    List<MusicCategoryModel> MusicData = new ArrayList<> ();

    public static FragmentDiscoverMusic getInstance(){
        FragmentDiscoverMusic fragmentDiscoverMusic = new FragmentDiscoverMusic ();
        return fragmentDiscoverMusic;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMusicdiscoverBinding.inflate(inflater, container, false);



        binding.DiscoverMusicRecycler.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext ());
        binding.DiscoverMusicRecycler.setLayoutManager(layoutManager);
        musicAdapter = new MusicAdapter(getContext (), MusicData);


        callMusicAPI();


        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach ( context );
    }



    private void callMusicAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_SONG_CATEGORIES, 0, new ApiResponseCallback () {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                        processData (jsonObject);


                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }

                });

            }
        });


    }






    void processData(JSONObject jsonObject){

        if (MusicData != null)
            MusicData.clear();

        JSONArray resultsArray = jsonObject.optJSONArray("results");
        for (int i = 0; i < resultsArray.length(); i++){

            JSONObject tagObject = resultsArray.optJSONObject(i);
            String id = tagObject.optString ( "id" );

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    callNonAuthAPI(API_CATEGORY_WISE_SONGS+id, 0, new ApiResponseCallback () {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {

                            loadUpList(tagObject, jsonObject);

                        }

                        @Override
                        public void onApiFailureResult(Exception e) {

                        }

                    });

                }
            });

        }


        musicAdapter.notifyDataSetChanged();

    }



    private void loadUpList(JSONObject tagObject, JSONObject resultsData) {

        SelectMusicData tempModel;
        MusicCategoryModel musicCategoryModel = new MusicCategoryModel();

        music = new ArrayList<SelectMusicData>();
        try {
            jsonArray = resultsData.getJSONArray("results");
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
            DialogCreator.cancelProgressDialog();
            binding.loaderanimationformusicdiscover.setVisibility(View.GONE);
            binding.DiscoverMusicRecycler.setAdapter(musicAdapter);


        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }

        //musicCategoryModel.setNextAPI(tagObject.optString("next"));
        musicCategoryModel.setDataList(music);
        musicCategoryModel.setCategoryTag (tagObject.optString("name"));
        musicCategoryModel.setId(tagObject.optLong("id"));
        MusicData.add( musicCategoryModel );
        musicAdapter.notifyDataSetChanged();

    }
}
