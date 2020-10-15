package com.reelvideos.app.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.reelvideos.app.R;
import com.reelvideos.app.WatchVideosActivity;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentSavedVideosBinding;
import com.reelvideos.app.models.HomeVideosModel;
//import com.reelvideos.app.utils.DialogCreator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_DELETE_VIDEO;
import static com.reelvideos.app.config.Constants.API_FETCH_SAVED_VIDEOS;
import static com.reelvideos.app.config.Constants.PREF_VIDEOS_COUNT;
import static com.reelvideos.app.utils.AppExtensions.showToast;


public class SavedVideosFragment extends BaseFragment {

    String userID;
    boolean isOwnProfile;
    FragmentSavedVideosBinding binding;
    ArrayList<HomeVideosModel> dataList;
    MyVideosAdapter adapter;
    NewVideoBroadCast mReceiver;
    long totalCount = -1;
    String nextAPICall = "NA";
    boolean isExplore = false;
    boolean isLoadMore = false;
    int visibleItemCount;
    int totalItemCount;
    int pastVisibleItems;
    boolean loadMore = true;

    private class NewVideoBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updatePrefsData();
            loadUpVideos();
        }
    }

    public SavedVideosFragment() { }

    public SavedVideosFragment(String userID) {
        this.userID = userID;
    }

    private void updatePrefsData(){
        userID = GlobalVariables.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSavedVideosBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    @Override
    public void initComponents() {
        mReceiver = new NewVideoBroadCast();
        activity.registerReceiver(mReceiver, new IntentFilter("saveVideo"));
        binding.dataRecycler.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 3);
        binding.dataRecycler.setLayoutManager(gridLayoutManager);

        //for loading the video when user scrolled till bottom of list
        binding.dataRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isLoadMore = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView,dx,dy);
                visibleItemCount = gridLayoutManager.getChildCount();
                totalItemCount = gridLayoutManager.getItemCount();
                pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                if (isLoadMore) {
                    if (loadMore && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        isLoadMore = false;
                        loadMoreVideos();
                    }
                }

            }
        });

        dataList = new ArrayList<>();
        adapter = new MyVideosAdapter(activity, dataList, (position, item, view) -> openWatchVideo(position), new MyVideosAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position, HomeVideosModel item, View view) {
                if (isOwnProfile) showDeleteDialog(item.getVideoID(), position);
            }
        });

        binding.dataRecycler.setAdapter(adapter);
        loadUpVideos();

    }


    private void showDeleteDialog(String videoID, int pos) {

        new MaterialAlertDialogBuilder(activity, R.style.dialogs_custom)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_msg))
                .setPositiveButton(getString(R.string.confirm_txt), (dialogInterface, i) -> {
                    processVideoDelete(videoID, pos);
                    dialogInterface.dismiss();
                }).setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();

    }

    private void processVideoDelete(String videoID, int pos){

        //DialogCreator.showProgressDialog(activity);
        binding.loaderanimationforsavedvideos.playAnimation();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                callAuthAPI(API_DELETE_VIDEO.replace("%video_id%", videoID), Request.Method.DELETE, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        //DialogCreator.cancelProgressDialog();
                        binding.loaderanimationforsavedvideos.setVisibility(View.GONE);
                        showToast(activity, getString(R.string.delete_success));
                        dataList.remove(pos);
                        adapter.notifyDataSetChanged();
                        if (adapter.getItemCount() < 1)
                            binding.noDataLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {
                        dataList.remove(pos);
                        adapter.notifyDataSetChanged();
                        showToast(activity, getString(R.string.delete_success));
                        //DialogCreator.cancelProgressDialog();
                        binding.loaderanimationforsavedvideos.setVisibility(View.GONE);
                    }
                });


            }
        });


    }



    private void loadUpVideos() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(API_FETCH_SAVED_VIDEOS.replace("%id%", userID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                        processData(jsonObject);

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
        handler.post(new Runnable() {
            @Override
            public void run() {

                callNonAuthAPI(nextAPICall, 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                        processData(jsonObject);

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });

    }


    void processData(JSONObject jsonObject){

        totalCount = jsonObject.optLong("count");
        loadMore = dataList.size() < totalCount;
        CoreApp.getInstance().getSharedPreferences().edit().putLong(PREF_VIDEOS_COUNT, totalCount).apply();
        HomeVideosModel tempModel;

        if (jsonObject.optString("next").length() < 8){
            nextAPICall = "NA";
            loadMore = false;
        }
        else nextAPICall = jsonObject.optString("next");

        JSONArray videosArray = jsonObject.optJSONArray("results");


        try {
            for (int i = 0; i < videosArray.length(); i++) {
                boolean duplicate = false;
                tempModel = new HomeVideosModel();
                JSONObject currentVideoObject = videosArray.optJSONObject(i);
                tempModel.setCreatorID(currentVideoObject.optLong("creator"));
                tempModel.setCreatorIMG(currentVideoObject.optString("creator_pic"));
                tempModel.setDownloadsCount(currentVideoObject.optLong("downloads"));
                tempModel.setSharesCount(currentVideoObject.optLong("shares"));
                tempModel.setVideoCommentsCount(currentVideoObject.optLong("num_comments"));
                tempModel.setVideoLikesCount(currentVideoObject.optLong("num_likes"));
                tempModel.setVideoCreatorName(currentVideoObject.optString("creator_username"));
                tempModel.setVideoDescription(currentVideoObject.optString("description"));
                tempModel.setVideoURL(currentVideoObject.optString("file"));
                tempModel.setVideoID(currentVideoObject.optLong("id"));
                tempModel.setPosterURL(currentVideoObject.optString("poster"));
                tempModel.setHashTags(currentVideoObject.optJSONArray("hashtags"));
                tempModel.setLiked(currentVideoObject.optBoolean("is_liked"));
                tempModel.setFollowed(currentVideoObject.optBoolean("is_following"));
                tempModel.setVerified(currentVideoObject.optBoolean("creator_is_verified"));
                tempModel.setSoundName("");

                for (int j=0; j<dataList.size(); j++ ){
                    if (dataList.get(j).getVideoID().equals(tempModel.getVideoID())){
                        duplicate = true;
                    }
                }
                if (!duplicate) {
                    dataList.add(tempModel);
                }


            }

            Collections.sort(dataList, new Comparator<HomeVideosModel>(){
                public int compare(HomeVideosModel obj1, HomeVideosModel obj2) {
                    // ## Ascending order
                    return Integer.valueOf(obj2.getVideoID()).compareTo(Integer.valueOf(obj1.getVideoID())); // To compare integer values
                }
            });

            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 0)
                binding.noDataLayout.setVisibility(View.GONE);
            else binding.noDataLayout.setVisibility(View.VISIBLE);

        }catch (Exception ignored){

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null) {
            activity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    void openWatchVideo(int position){
        Intent intent = new Intent(activity, WatchVideosActivity.class);
        intent.putExtra("dataArray", dataList);
        intent.putExtra("position", position);
        intent.putExtra("NEXT_API", nextAPICall);
        intent.putExtra("isFromProfile", true);
        activity.startActivity(intent);
    }

}