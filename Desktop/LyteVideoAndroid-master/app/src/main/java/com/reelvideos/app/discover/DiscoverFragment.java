package com.reelvideos.app.discover;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.chahinem.pageindicator.PageIndicator;
import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.DifferentProfileActivity;
import com.reelvideos.app.MainActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.adapters.SlidingImage_Adapter;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentDiscoverBinding;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.persons.PersonsModel;
import com.reelvideos.app.profile.ProfileFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_GET_TAGS;
import static com.reelvideos.app.config.Constants.API_GET_TOP_CREATOR;
import static com.reelvideos.app.config.Constants.API_GET_VIDEO_BY_HASH_TAG;
import static com.reelvideos.app.config.Constants.BASE_URL;
import static com.reelvideos.app.config.Constants.followMap;
import static com.reelvideos.app.config.Constants.getSharedVideo;


public class DiscoverFragment extends BaseFragment {

    FragmentDiscoverBinding binding;

    TreandingHashtagAdapter treandingHashtagAdapter;
    ArrayList<String> hashTagName = new ArrayList<>();
    ArrayList<String> hashTagViews = new ArrayList<>();


    ArrayList<HashTagModel> hahTagData = new ArrayList<>();
    DiscoverAdapter discoverAdapter;

    TopCreatorAdaptor topCreatorAdaptor;
    ArrayList<PersonsModel> topCreatorData = new ArrayList<>();

    
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private ArrayList<String> urls = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        initComponents();

        //////////////////////////////////////////

        DiscoverAdapter.Listener listener = new DiscoverAdapter.Listener() {
            @Override
            public void onItemClicked(String app, String ball) {
                Intent i = new Intent(getActivity(),HashtagActivity.class);
                i.putExtra("ha",app);
                i.putExtra("vs", ball);
                getActivity().startActivity(i);
            }
        };
        ///////////////////////////////////

        loadBannerAPI();

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        discoverAdapter = new DiscoverAdapter(activity, hahTagData, listener);
        binding.dataRecycler.setAdapter(discoverAdapter);
        binding.dataRecycler.setLayoutManager(layoutManager);
        binding.dataRecycler.setAdapter(discoverAdapter);

        callHashTagAPI();

        //For Top Creator

        LinearLayoutManager layoutManagerTop = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        topCreatorAdaptor = new TopCreatorAdaptor(activity, topCreatorData, (view, position, item) -> OpenProfile(item.getUserID(), item.getUsername(), item.getUserImage(), item.isFollowed()));
        binding.dataRecyclerTopCreator.setAdapter(topCreatorAdaptor);
        binding.dataRecyclerTopCreator.setLayoutManager(layoutManagerTop);
        binding.dataRecyclerTopCreator.setHasFixedSize(true);

        callTopCreatorAPI();


        binding.toolbar.setOnClickListener (view -> {
            SearchFragment searchFragment = new SearchFragment ();
            AppCompatActivity activity=(AppCompatActivity)view.getContext ();
            activity.getSupportFragmentManager().beginTransaction ()
                    .replace ( R.id.discoverFragment,searchFragment )
                    .addToBackStack ( null )
                    .commit ();
        });

        return binding.getRoot();
    }

    private void loadBannerAPI() {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> callNonAuthAPI(BASE_URL+"banners/", 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {

                try {
                    JSONArray resultsArray =  jsonObject.getJSONArray(getString(R.string.results).toLowerCase());
                    initBanner(resultsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onApiFailureResult(Exception e) {

            }

        }));

    }


    private void initBanner(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject currentVideoObject = jsonArray.getJSONObject(i);
                urls.add(currentVideoObject.optString("banner"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPager = activity.findViewById(R.id.pager);
        mPager.setAdapter(new SlidingImage_Adapter(getContext(), urls));


//        PageIndicator indicator = activity.findViewById(R.id.indicator);
//        indicator.attachTo(mPager);

        final float density = getResources().getDisplayMetrics().density;


        NUM_PAGES = urls.size();

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);


    }


    @Override
    public void initComponents() {


        //shubham_keshri
        //For Trending HashTag RecyclerView
//        GridLayoutManager
//                gridLayoutManager
//                = new GridLayoutManager ( activity,2 );
//        treandingHashtagAdapter = new TreandingHashtagAdapter ( activity, hashTagName,hashTagViews );
//        binding.hashTagRecycler.setLayoutManager ( gridLayoutManager );
//        binding.hashTagRecycler.setAdapter ( treandingHashtagAdapter );
//        callHashTagAPI ();


    }


    //For HashTag names only
    /*void callHashTagAPI(){

        StringRequest postRequest = new StringRequest(Request.Method.GET, API_GET_TAGS,
                response -> {
                    // response
                    try {


                        processHashTagData(new JSONArray(response));
                    } catch (JSONException ignored) {

                    }

                },
                error -> {
                    // error
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

        };
        CoreApp.getInstance().queue.add(postRequest);
    }*/


//    public void processHashTagData(JSONArray jsonArray) {
//
//        if (hashTagName != null || hashTagViews != null) {
//            hashTagName.clear ();
//            hashTagViews.clear ();
//        }
//
//
//        for (int i = 0; i < jsonArray.length (); i++) {
//
//
//            JSONObject tagObject = jsonArray.optJSONObject ( i );
//            hashTagName.add ( tagObject.optString ( "name" ) );
//            hashTagViews.add ( tagObject.optString ( "total_views" ) );
//            //Toast.makeText ( activity, hashTagViews.get ( i ), Toast.LENGTH_SHORT ).show ();
//
//            /*try {
//                  JSONObject tagObject = jsonArray.getJSONObject(i);
//                  tempModel.setTagID(String.valueOf(tagObject.optLong("id")));
//                tempModel.setTagViews(String.valueOf(tagObject.optLong("total_views")));
//                  tempModel.setTagName(tagObject.optString("name"));
//                  hahTagData.add(tempModel);
//
//              } catch (JSONException e) {
//                e.printStackTrace();
//              }*/
//
//
//        }
//        treandingHashtagAdapter.notifyDataSetChanged ();
//    }


//Hashtag Along With Viedo

    void callHashTagAPI() {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            StringRequest postRequest = new StringRequest(Request.Method.GET, API_GET_TAGS,
                    response -> {
                        // response
                        try {
                            processData(new JSONArray(response));
                        } catch (JSONException ignored) {

                        }

                    },
                    error -> {
                        // error
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }
            };
            CoreApp.getInstance().queue.add(postRequest);
        });

    }

    void processData(JSONArray jsonArray) {

        if (hahTagData != null)
            hahTagData.clear();


        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject tagObject = jsonArray.optJSONObject(i);


            Handler handler = new Handler(Looper.getMainLooper());
            // separate thread is used for network calls
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // update the ui from here
                    callNonAuthAPI(API_GET_VIDEO_BY_HASH_TAG.replace("%tag%", tagObject.optString("name")), 0, new ApiResponseCallback() {
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


//            tempModel = new HashTagModel();
//
//            try {
//                JSONObject tagObject = jsonArray.getJSONObject(i);
//                tempModel.setTagID(String.valueOf(tagObject.optLong("id")));
//                tempModel.setTagViews(String.valueOf(tagObject.optLong("total_views")));
//                tempModel.setTagName(tagObject.optString("name"));
//                hahTagData.add(tempModel);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        }

        discoverAdapter.notifyDataSetChanged();


    }


    private void loadUpList(JSONObject tagObject, JSONObject resultsData) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            // update the ui from here
            HomeVideosModel tempModel;
            HashTagModel hashTagModel = new HashTagModel();
            ArrayList<HomeVideosModel> finalList = new ArrayList<>();

            JSONArray resultsArray = resultsData.optJSONArray(getString(R.string.results));
            for (int i = 0; i < resultsArray.length(); i++) {
                tempModel = new HomeVideosModel();
                JSONObject currentVideoObject = resultsArray.optJSONObject(i);
                tempModel.setCreatorID(currentVideoObject.optLong(getString(R.string.creator)));
                tempModel.setCreatorIMG(currentVideoObject.optString(getString(R.string.creator_pic)));
                tempModel.setDownloadsCount(currentVideoObject.optLong(getString(R.string.downloads)));
                tempModel.setSharesCount(currentVideoObject.optLong(getString(R.string.shares)));
                tempModel.setVideoCommentsCount(currentVideoObject.optLong(getString(R.string.num_comments)));
                tempModel.setVideoLikesCount(currentVideoObject.optLong(getString(R.string.num_likes)));
                tempModel.setVideoViewCount(currentVideoObject.optLong(getString(R.string.num_views)));
                tempModel.setVideoCreatorName(currentVideoObject.optString(getString(R.string.creator_username)));
                tempModel.setVideoDescription(currentVideoObject.optString(getString(R.string.description)));
                tempModel.setVideoURL(currentVideoObject.optString(getString(R.string.file)));
                tempModel.setVideoID(currentVideoObject.optLong(getString(R.string.id)));
                tempModel.setPosterURL(currentVideoObject.optString(getString(R.string.poster)));
                tempModel.setHashTags(currentVideoObject.optJSONArray(getString(R.string.hashtags)));
                tempModel.setLiked(currentVideoObject.optBoolean(getString(R.string.is_liked)));
                tempModel.setFollowed(currentVideoObject.optBoolean(getString(R.string.is_following)));
                tempModel.setSoundName("");
                finalList.add(tempModel);

            }

            hashTagModel.setNextAPI(resultsData.optString("next"));
            hashTagModel.setDataList(finalList);
            hashTagModel.setTagName(tagObject.optString("name"));
            hashTagModel.setTagViews(String.valueOf(tagObject.optLong("total_views")));
            hashTagModel.setTagID(String.valueOf(tagObject.optLong("id")));
            hahTagData.add(hashTagModel);

        });

        discoverAdapter.notifyDataSetChanged();
    }


    void callTopCreatorAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> {
            if (GlobalVariables.hasUserLoggedIN()) {
                callAuthAPI(API_GET_TOP_CREATOR, 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processTopCreatorData(jsonObject);

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {


                    }
                });
            } else {
                callNonAuthAPI(API_GET_TOP_CREATOR, 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processTopCreatorData(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            }

        });


    }

    public void processTopCreatorData(JSONObject jsonObject) {

        if (topCreatorData != null)
            topCreatorData.clear();

        JSONArray resultsArray = jsonObject.optJSONArray(getString(R.string.results));
        PersonsModel tempModel;


        for (int i = 0; i < resultsArray.length(); i++){
            tempModel = new PersonsModel();
            JSONObject item = resultsArray.optJSONObject(i);
            tempModel.setFollowed(item.optBoolean(getString(R.string.is_following)));
            tempModel.setUsername(item.optString(getString(R.string.username).toLowerCase()));
            tempModel.setUserFullName(item.optString(getString(R.string.fullname).toLowerCase()));
            tempModel.setUserID(item.optString(getString(R.string.id)));
            tempModel.setUserImage(item.optString(getString(R.string.pic)));
            tempModel.setBio(item.optString(getString(R.string.bio)));
            topCreatorData.add(tempModel);
        }


        topCreatorAdaptor.notifyDataSetChanged ();
    }





    private void OpenProfile(String creatorID, String videoCreatorName, String creatorIMG, boolean isFollowing) {
        if (GlobalVariables.getUserId().equals(String.valueOf(creatorID))) {

            //In case of Own Profile
            TabLayout.Tab profile = ((MainActivity) activity).binding.tabs.getTabAt(4);
            profile.select();
            activity.onBackPressed();

        } else {
            Intent intent = new Intent(getActivity(), DifferentProfileActivity.class);
            intent.putExtra("ID", String.valueOf(creatorID));
            intent.putExtra("NAME", videoCreatorName);
            intent.putExtra("IMG", creatorIMG);
            intent.putExtra("isFollowed", isFollowing);
            startActivity(intent);
//            activity.overridePendingTransition();

        }


    }


}