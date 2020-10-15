package com.reelvideos.app.Music;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.databinding.FragmentMusicshowallBinding;
import com.reelvideos.app.models.SelectMusicData;
//import com.reelvideos.app.utils.DialogCreator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_SEARCH_SONGS;

public class ShowAllMusicFragment extends BaseFragment {

    FragmentMusicshowallBinding binding;


    private List<SelectMusicData> music;
    NestedMusicListAdapter music_Adapter;
    String category_id,category;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMusicshowallBinding.inflate(inflater, container, false);
        binding.loaderanimationshowallmusic.setVisibility(View.GONE);
        initComponents();
        return binding.getRoot();
    }
    @Override
    public void initComponents() {
        Bundle arguments = getArguments();

        if (arguments != null) {


            ArrayList<SelectMusicData> data = (ArrayList<SelectMusicData>) arguments.getSerializable ( "valuesArray" );

            binding.allMusicList.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext ());
            binding.allMusicList.setLayoutManager(layoutManager);
            NestedMusicListAdapter nestedMusicListAdapter = new NestedMusicListAdapter (getContext (),data);
            binding.allMusicList.setAdapter(nestedMusicListAdapter);

            category_id= arguments.getString ( "category_id" );
            category=arguments.getString ( "category" );
            binding.searchBar.setPlaceHolder ( category );
            binding.searchBar.addTextChangeListener ( new TextWatcher () {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    doSearching (s.toString ());

                    if (s.toString ().length ()>0){
                        binding.searchResult.setVisibility ( View.VISIBLE );
                        binding.allMusicList.setVisibility ( View.GONE );
                    }else {
                        binding.searchResult.setVisibility ( View.GONE );
                        binding.allMusicList.setVisibility ( View.VISIBLE );
                    }
                }
            } );
        }


        binding.selectMusicImagebuttonBack.setOnClickListener ( view -> goBack() );
    }

    private void doSearching(String song) {
        LinearLayoutManager layoutManager = new LinearLayoutManager (getContext ());
        binding.loaderanimationshowallmusic.setVisibility(View.VISIBLE);
        binding.searchResult.setLayoutManager(layoutManager);
        callSearchMusicAPI ( song );
    }
    private void callSearchMusicAPI(String song) {

        callNonAuthAPI(API_SEARCH_SONGS.replace ( "%query%",category_id )+song, 0, new ApiResponseCallback () {
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
            binding.loaderanimationshowallmusic.setVisibility(View.GONE);
            music_Adapter = new NestedMusicListAdapter (getContext (),music);
            binding.searchResult.setAdapter(music_Adapter);
            music_Adapter.notifyDataSetChanged();


        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }


    }

    private void goBack() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchBar.getWindowToken(), 0);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }

}
