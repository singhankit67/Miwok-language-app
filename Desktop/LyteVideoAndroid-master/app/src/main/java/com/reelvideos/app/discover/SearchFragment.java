package com.reelvideos.app.discover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.MainActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.WatchVideosActivity;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentSearchBinding;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.persons.PersonsAdapter;
import com.reelvideos.app.persons.PersonsModel;
import com.reelvideos.app.profile.ProfileFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_SEARCH_ACCOUNTS;
import static com.reelvideos.app.config.Constants.API_SEARCH_HASHTAGS;
import static com.reelvideos.app.config.Constants.API_SEARCH_VIDEOS;
import static com.reelvideos.app.config.Constants.followMap;

public class SearchFragment extends BaseFragment {

    FragmentSearchBinding binding;

    TreandingHashtagAdapter treandingHashtagAdapter;
    ArrayList<String> hashTagName = new ArrayList<>();
    ArrayList<String> hashTagViews = new ArrayList<>();

    PersonsAdapter personsAdapter;
    ArrayList<PersonsModel> AccountList = new ArrayList<>();

    List<HomeVideosModel> VideoList = new ArrayList<HomeVideosModel>();
    HashTagVideoAdapter hashTagVideoAdapter;

    String nextVideosAPI = "NA", nextAccountsAPI = "NA", nextHashtagsAPI = "NA";
    boolean isLoadMoreVideos = false, isLoadMoreAccounts = false, isLoadMoreHashtags = false;
    long totalVideosInServer = -1, totalAccountsInServer = -1, totalHashtagsInServer = -1;
    int visibleVideosCount, visibleAccountsCount, visibleHashtagsCount;
    int totalVideosCount, totalAccountsCount, totalHashtagsCount;
    int pastVisibleVideos, pastVisibleAccounts, pastVisibleHashtags;
    boolean loadMoreVideos = true, loadMoreAccounts = true, loadMoreHashtags = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        initComponents();

        return binding.getRoot();
    }


    @Override
    public void initComponents() {

        binding.goBackToExplore.setOnClickListener(view -> goBackToSearch());

        //Account Search
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        binding.AccountResultRecyclerView.setLayoutManager(linearLayoutManager);
        binding.AccountResultRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMoreAccounts = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleAccountsCount = linearLayoutManager.getChildCount();
                totalAccountsCount = linearLayoutManager.getItemCount();
                pastVisibleAccounts = linearLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMoreAccounts) {
                    if (loadMoreAccounts && (visibleAccountsCount + pastVisibleAccounts) >= totalAccountsCount) {
                        isLoadMoreAccounts = false;
                        loadUpMoreFromAccountsAPI();
                    }
                }
            }
        });
        personsAdapter = new PersonsAdapter(activity, AccountList, (view, position, item) -> OpenProfile(item.getUserID(), item.getUsername(), item.getUserImage(), item.isFollowed()));
        binding.AccountResultRecyclerView.setAdapter(personsAdapter);
        binding.AccountResultRecyclerView.setHasFixedSize(true);


        //HashTag search
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(activity);
        binding.HashTagResultRecyclerView.setLayoutManager(linearLayoutManager2);
        binding.HashTagResultRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMoreHashtags = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleHashtagsCount = linearLayoutManager.getChildCount();
                totalHashtagsCount = linearLayoutManager.getItemCount();
                pastVisibleHashtags = linearLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMoreHashtags) {
                    if (loadMoreHashtags && (visibleHashtagsCount + pastVisibleHashtags) + 5 >= totalHashtagsCount) {
                        isLoadMoreHashtags = false;
                        loadUpMoreFromHashTagsAPI();
                    }
                }
            }
        });
        treandingHashtagAdapter = new TreandingHashtagAdapter(activity, hashTagName, hashTagViews);
        binding.HashTagResultRecyclerView.setAdapter(treandingHashtagAdapter);
        binding.HashTagResultRecyclerView.setHasFixedSize(true);


        //Video Result
        binding.VideoResultRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 3);
        binding.VideoResultRecyclerView.setLayoutManager(gridLayoutManager);
        binding.VideoResultRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMoreVideos = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleVideosCount = gridLayoutManager.getChildCount();
                totalVideosCount = gridLayoutManager.getItemCount();
                pastVisibleVideos = gridLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMoreVideos) {
                    if (loadMoreVideos && (visibleVideosCount + pastVisibleVideos) >= totalVideosCount) {
                        isLoadMoreVideos = false;
                        loadMoreVideos();
                    }
                }
            }
        });
        hashTagVideoAdapter = new HashTagVideoAdapter(activity, (position, item, view) -> openWatchVideo(position), VideoList);
        binding.VideoResultRecyclerView.setAdapter(hashTagVideoAdapter);

        binding.searchIT.requestFocus();
        binding.searchIT.setFocusableInTouchMode(true);
        binding.searchIT.setFocusable(true);
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        binding.searchIT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (binding.tabs.getSelectedTabPosition() == 0) {
                    if (AccountList != null)
                        AccountList.clear();
                    String query = editable.toString();
                    binding.AccountResultRecyclerView.setVisibility(View.VISIBLE);
                    binding.HashTagResultRecyclerView.setVisibility(View.GONE);
                    binding.VideoResultRecyclerView.setVisibility(View.GONE);
                    loadUpFromAccountsAPI(query);
                } else if (binding.tabs.getSelectedTabPosition() == 1) {
                    if (hashTagName != null) {
                        hashTagName.clear();
                        hashTagViews.clear();
                    }
                    String query = editable.toString();
                    binding.AccountResultRecyclerView.setVisibility(View.GONE);
                    binding.HashTagResultRecyclerView.setVisibility(View.VISIBLE);
                    binding.VideoResultRecyclerView.setVisibility(View.GONE);
                    loadUpFromHashTagsAPI(query);
                } else {
                    if (VideoList != null)
                        VideoList.clear();
                    String query = editable.toString();
                    binding.AccountResultRecyclerView.setVisibility(View.GONE);
                    binding.HashTagResultRecyclerView.setVisibility(View.GONE);
                    binding.VideoResultRecyclerView.setVisibility(View.VISIBLE);
                    loadUpFromVideoAPI(query);

                }
            }
        });

        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        binding.searchIT.setHint("Search Accounts");
                        String accountsQuery = binding.searchIT.getText().toString();
                        binding.AccountResultRecyclerView.setVisibility(View.VISIBLE);
                        binding.HashTagResultRecyclerView.setVisibility(View.GONE);
                        binding.VideoResultRecyclerView.setVisibility(View.GONE);
                        loadUpFromAccountsAPI(accountsQuery);
                        break;
                    case 1:
                        binding.searchIT.setHint("Search Hashtags");
                        String hashtagsQuery = binding.searchIT.getText().toString();
                        binding.AccountResultRecyclerView.setVisibility(View.GONE);
                        binding.HashTagResultRecyclerView.setVisibility(View.VISIBLE);
                        binding.VideoResultRecyclerView.setVisibility(View.GONE);
                        loadUpFromHashTagsAPI(hashtagsQuery);
                        break;
                    case 2:
                        binding.searchIT.setHint("Search Videos");
                        String videosQuery = binding.searchIT.getText().toString();
                        binding.AccountResultRecyclerView.setVisibility(View.GONE);
                        binding.HashTagResultRecyclerView.setVisibility(View.GONE);
                        binding.VideoResultRecyclerView.setVisibility(View.VISIBLE);
                        loadUpFromVideoAPI(videosQuery);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void goBackToSearch() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchIT.getWindowToken(), 0);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }


    private void loadUpFromAccountsAPI(String query) {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_SEARCH_ACCOUNTS.replace("%query%", query), 0, new ApiResponseCallback() {

                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processAccountData(jsonObject);

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });

    }

    private void loadUpMoreFromAccountsAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(nextAccountsAPI, 0, new ApiResponseCallback() {

                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processAccountData(jsonObject);

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });

    }

    private void processAccountData(JSONObject jsonObject) {

        totalAccountsInServer = jsonObject.optLong("count");
        loadMoreAccounts = AccountList.size() < totalAccountsInServer;

        if (jsonObject.optString("next").length() < 8) {
            nextAccountsAPI = "NA";
            loadMoreAccounts = false;
        } else nextAccountsAPI = jsonObject.optString("next");

        binding.message.setVisibility(View.GONE);


        JSONArray resultsArray = jsonObject.optJSONArray("results");
        PersonsModel tempModel;
        for (int i = 0; i < resultsArray.length(); i++) {
            boolean duplicate = false;
            tempModel = new PersonsModel();
            JSONObject item = resultsArray.optJSONObject(i);
            tempModel.setFollowed(item.optBoolean("is_following"));
            tempModel.setUsername(item.optString("username"));
            tempModel.setUserFullName(item.optString("fullname"));
            tempModel.setUserID(item.optString("id"));
            tempModel.setUserImage(item.optString("pic"));
            tempModel.setBio(item.optString("bio"));
            tempModel.setVerified(item.optBoolean("is_verified"));
            for (int j = 0; j < AccountList.size(); j++) {
                if (AccountList.get(j).getUserID().equals(tempModel.getUserID())) {
                    duplicate = true;
                }
            }
            if (!duplicate) {
                AccountList.add(tempModel);
            }
        }

        if (AccountList.size() == 0)
            binding.message.setVisibility(View.VISIBLE);

        personsAdapter.notifyDataSetChanged();
    }


    private void OpenProfile(String creatorID, String videoCreatorName, String creatorIMG, boolean isFollowed) {


        if (GlobalVariables.getUserId().equals(creatorID)) {

            //In case of Own Profile
            TabLayout.Tab profile = ((MainActivity) activity).binding.tabs.getTabAt(4);
            profile.select();
            activity.onBackPressed();

        } else {

            //other profile
            if (followMap.size() > 0) {
                if (followMap.containsKey(creatorID)) {
                    FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
                    ProfileFragment profileFragment = new ProfileFragment(creatorID, videoCreatorName, creatorIMG, followMap.get(creatorID));
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.mainMenu, profileFragment).commit();
                } else {
                    FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
                    ProfileFragment profileFragment = new ProfileFragment(creatorID, videoCreatorName, creatorIMG, isFollowed);
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.mainMenu, profileFragment).commit();
                }
            } else {
                FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
                ProfileFragment profileFragment = new ProfileFragment(creatorID, videoCreatorName, creatorIMG, isFollowed);
                transaction.addToBackStack(null);
                transaction.replace(R.id.mainMenu, profileFragment).commit();
            }
        }


    }


    private void loadUpFromHashTagsAPI(String query) {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_SEARCH_HASHTAGS.replace("%query%", query), 0, new ApiResponseCallback() {

                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processHashTagData(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });

    }

    private void loadUpMoreFromHashTagsAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(nextHashtagsAPI, 0, new ApiResponseCallback() {

                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processHashTagData(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });


    }

    private void processHashTagData(JSONObject jsonObject) {


        totalHashtagsInServer = jsonObject.optLong("count");
        loadMoreHashtags = hashTagName.size() < totalHashtagsInServer;

        if (jsonObject.optString("next").length() < 8) {
            nextHashtagsAPI = "NA";
            loadMoreHashtags = false;
        } else nextHashtagsAPI = jsonObject.optString("next");

        binding.message.setVisibility(View.GONE);
        JSONArray resultArray = jsonObject.optJSONArray("results");

        for (int i = 0; i < resultArray.length(); i++) {

            JSONObject currentObject = resultArray.optJSONObject(i);

            hashTagName.add(currentObject.optString("name"));
            hashTagViews.add(currentObject.optString("total_views"));

        }

        if (hashTagName == null) {
            binding.message.setVisibility(View.VISIBLE);
        }
        treandingHashtagAdapter.notifyDataSetChanged();

    }


    private void loadUpFromVideoAPI(String query) {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_SEARCH_VIDEOS.replace("%query%", query), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                        processVideoData(jsonObject);

                    }


                    @Override
                    public void onApiFailureResult(Exception e) {

                    }

                });

            }
        });

    }


    private void loadMoreVideos() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(nextVideosAPI, 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        processVideoData(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });

    }

    private void processVideoData(JSONObject jsonObject) {

        totalVideosInServer = jsonObject.optLong("count");
        loadMoreVideos = VideoList.size() < totalVideosInServer;

        if (jsonObject.optString("next").length() < 8) {
            nextVideosAPI = "NA";
            loadMoreVideos = false;
        } else nextVideosAPI = jsonObject.optString("next");


        binding.message.setVisibility(View.GONE);

        JSONArray resultsArray = jsonObject.optJSONArray("results");
        for (int i = 0; i < resultsArray.length(); i++) {
            boolean duplicate = false;
            HomeVideosModel tempModel = new HomeVideosModel();
            JSONObject currentVideoObject = resultsArray.optJSONObject(i);
            tempModel.setCreatorID(currentVideoObject.optLong("creator"));
            tempModel.setCreatorIMG(currentVideoObject.optString("creator_pic"));
            tempModel.setDownloadsCount(currentVideoObject.optLong("downloads"));
            tempModel.setSharesCount(currentVideoObject.optLong("shares"));
            tempModel.setVideoCommentsCount(currentVideoObject.optLong("num_comments"));
            tempModel.setVideoLikesCount(currentVideoObject.optLong("num_likes"));
            tempModel.setVideoViewCount(currentVideoObject.optLong("num_views"));
            tempModel.setVideoCreatorName(currentVideoObject.optString("creator_username"));
            tempModel.setVideoDescription(currentVideoObject.optString("description"));
            tempModel.setVideoURL(currentVideoObject.optString("file"));
            tempModel.setVideoID(currentVideoObject.optLong("id"));
            tempModel.setPosterURL(currentVideoObject.optString("poster"));
            tempModel.setHashTags(currentVideoObject.optJSONArray("hashtags"));
            tempModel.setLiked(currentVideoObject.optBoolean("is_liked"));
            tempModel.setFollowed(currentVideoObject.optBoolean("is_following"));
            tempModel.setVerified(currentVideoObject.optBoolean("is_verified"));
            tempModel.setSoundName("");

            for (int j = 0; j < VideoList.size(); j++) {
                if (VideoList.get(j).getVideoID().equals(tempModel.getVideoID())) {
                    duplicate = true;
                }
            }
            if (!duplicate) {
                VideoList.add(tempModel);
            }
        }


        if (VideoList.size() == 0)
            binding.message.setVisibility(View.VISIBLE);


        hashTagVideoAdapter.notifyDataSetChanged();
    }

    public void openWatchVideo(int position) {
        Intent intent = new Intent(getContext(), WatchVideosActivity.class);
        intent.putExtra("dataArray", (Serializable) VideoList);
        intent.putExtra("position", position);
        intent.putExtra("isFromProfile", false);
        intent.putExtra("NEXT_API", nextVideosAPI);
        activity.startActivity(intent);
    }

}
