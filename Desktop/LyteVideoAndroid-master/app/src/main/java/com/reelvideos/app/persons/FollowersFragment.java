package com.reelvideos.app.persons;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.MainActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.profile.ProfileFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_GET_FOLLOWERS;


public class FollowersFragment extends BaseFragment {

    com.reelvideos.app.databinding.FragmentFollowersBinding binding;
    PersonsAdapter personsAdapter;
    ArrayList<PersonsModel> dataList = new ArrayList<>();
    String userID;
    String nextAPICall = "NA";
    int visibleItemCount;
    int totalItemCount;
    int pastVisibleItems;
    boolean loadMore = true;
    boolean isLoadMore = false;
    long totalCount = -1;

    public FollowersFragment(String userID) {
        this.userID = userID;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = com.reelvideos.app.databinding.FragmentFollowersBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    @Override
    public void initComponents() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        binding.dataRecycler.setLayoutManager(linearLayoutManager);
        binding.dataRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMore = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMore) {
                    if (loadMore && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        isLoadMore = false;
                        loadUpMoreFromAPI();
                    }
                }

            }
        });
        personsAdapter = new PersonsAdapter(activity, dataList, (view, position, item) -> OpenProfile(item.getUserID(), item.getUsername(), item.getUserImage(), item.isFollowed()));

        binding.dataRecycler.setAdapter(personsAdapter);
        binding.dataRecycler.setHasFixedSize(true);
        loadUpFromAPI();

    }

    private void loadUpFromAPI() {


        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> callNonAuthAPI(API_GET_FOLLOWERS.replace("%id%", userID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                processData(jsonObject);

            }

            @Override
            public void onApiFailureResult(Exception e) {
                cancelLoader();
            }
        }));

    }

    private void loadUpMoreFromAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> callNonAuthAPI(nextAPICall, 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                processData(jsonObject);

            }

            @Override
            public void onApiFailureResult(Exception e) {
                cancelLoader();
            }
        }));
    }

    private void processData(JSONObject jsonObject) {
        showLoader();
        totalCount = jsonObject.optLong("count");
        loadMore = dataList.size() < totalCount;

        if (jsonObject.optString("next").length() < 8) {
            nextAPICall = "NA";
            loadMore = false;
        } else nextAPICall = jsonObject.optString("next");

        JSONArray resultsArray = jsonObject.optJSONArray("results");
        PersonsModel tempModel;
        assert resultsArray != null;
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
            for (int j = 0; j < dataList.size(); j++) {
                if (dataList.get(j).getUserID().equals(tempModel.getUserID())) {
                    duplicate = true;
                }
            }
            if (!duplicate) {
                dataList.add(tempModel);
            }
        }

        if (dataList.size() > 0)
            personsAdapter.notifyDataSetChanged();
        else binding.noFollow.setVisibility(View.VISIBLE);

        cancelLoader();
    }

    void showLoader() {

        binding.progressLayout.setVisibility(View.VISIBLE);
        Animation rotateAnim = AnimationUtils.loadAnimation(activity, R.anim.clockwise_rotation_fast);
        binding.logoIV.startAnimation(rotateAnim);
    }

    void cancelLoader() {
        binding.progressLayout.setVisibility(View.GONE);
        binding.logoIV.clearAnimation();
    }

    private void OpenProfile(String creatorID, String videoCreatorName, String creatorIMG, boolean isFollowed) {


        if (GlobalVariables.getUserId().equals(creatorID)) {

            //In case of Own Profile
            TabLayout.Tab profile = ((MainActivity) activity).binding.tabs.getTabAt(4);
            assert profile != null;
            profile.select();
            activity.onBackPressed();

        } else {

            //Other Profile
            FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
            ProfileFragment profileFragment = new ProfileFragment(creatorID, videoCreatorName, creatorIMG, isFollowed);
            transaction.addToBackStack(null);
            transaction.replace(R.id.mainMenu, profileFragment).commit();

        }


    }

}