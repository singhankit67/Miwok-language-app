package com.reelvideos.app.discover;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.reelvideos.app.R;
import com.reelvideos.app.WatchVideosActivity;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.models.HomeVideosModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_GET_VIDEO_BY_HASH_TAG;

public class HashtagActivity extends AppCompatActivity {

    List<HomeVideosModel> hashTagVideoData = new ArrayList<HomeVideosModel>();
    HashTagVideoAdapter hashTagVideoAdapter;
    String hashTag, ViewCount, nextAPI;
    String nextAPICall = "NA";
    int visibleItemCount;
    int totalItemCount;
    int pastVisibleItems;
    boolean loadMore = true;
    boolean isLoadMore = false;
    long totalCount = -1;

    RecyclerView hashtagRecycler;
    CoordinatorLayout hashTagRoot;
    TextView tv_ViewsCount,tv_videoCount;
    CollapsingToolbarLayout collapsing_toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);
        hashtagRecycler = findViewById(R.id.hashtagRecycler);
        hashTagRoot = findViewById(R.id.hashTagRoot);
        tv_ViewsCount = findViewById(R.id.tv_ViewsCount);
        tv_videoCount = findViewById(R.id.tv_videoCount);
        collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        init();
    }


    public void init() {

        hashtagRecycler.setHasFixedSize(false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),3);
        hashtagRecycler.setLayoutManager(gridLayoutManager);
        hashtagRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMore = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = gridLayoutManager.getChildCount();
                totalItemCount = gridLayoutManager.getItemCount();
                pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMore) {
                    if (loadMore && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        isLoadMore = false;
                        loadUpMoreFromAPI();
                    }
                }
            }
        });

        hashTagVideoAdapter = new HashTagVideoAdapter(getApplicationContext(), (position, item, view) -> openWatchVideo(position), hashTagVideoData);
        hashtagRecycler.setAdapter(hashTagVideoAdapter);

        hashTag = getIntent().getStringExtra("ha");

        ViewCount = getIntent().getStringExtra("vs");
        collapsing_toolbar.setTitle("#" + hashTag);
        tv_ViewsCount.setText(ViewCount);

        processData(hashTag);
    }

    // TODO ADD THREAD HERE TOO
    public void processData(String hashTag) {

//        if (hashTagVideoData != null)
//            hashTagVideoData.clear();
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_GET_VIDEO_BY_HASH_TAG.replace("%tag%", hashTag), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        loadUpList(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }

                });

            }
        });

    }
    // TODO ADD THREAD HERE TOO
    public void loadUpMoreFromAPI() {

        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> callNonAuthAPI(nextAPICall, 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {

                loadUpList(jsonObject);

            }

            @Override
            public void onApiFailureResult(Exception e) {

            }

        }));

    }
    // TODO ADD THREAD HERE TOO
    private void loadUpList(@NotNull JSONObject jsonObject) {

        totalCount = jsonObject.optLong(getString(R.string.count));
        loadMore = hashTagVideoData.size() < totalCount;
        tv_videoCount.setText(String.format("%s videos", String.valueOf(totalCount)));

        nextAPICall = jsonObject.optString("next");
        nextAPI = jsonObject.optString("next");

        JSONArray resultsArray = jsonObject.optJSONArray(getString(R.string.results));
        for (int i = 0; i < resultsArray.length(); i++) {
            boolean duplicate = false;
            HomeVideosModel tempModel = new HomeVideosModel();
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
            tempModel.setVerified(currentVideoObject.optBoolean(getString(R.string.is_verified)));
            tempModel.setSoundName("");

            for (int j = 0; j < hashTagVideoData.size(); j++) {
                if (hashTagVideoData.get(j).getVideoID().equals(tempModel.getVideoID())) {
                    duplicate = true;
                }
            }
            if (!duplicate) {
                hashTagVideoData.add(tempModel);
            }

        }

        for (int i = 0; i < hashTagVideoData.size(); i++) {
            Log.e("HashTagVideoData", hashTagVideoData.get(i).getVideoID());
        }
        Log.e("Count", String.valueOf(totalCount));
        hashTagVideoAdapter.notifyDataSetChanged();


    }

    public void openWatchVideo(int position) {
        Intent intent = new Intent(this, WatchVideosActivity.class);
        intent.putExtra("dataArray", (Serializable) hashTagVideoData);
        intent.putExtra("position", position);
        intent.putExtra("isFromProfile", false);
        intent.putExtra("NEXT_API", nextAPI);
        this.startActivity(intent);
    }
}