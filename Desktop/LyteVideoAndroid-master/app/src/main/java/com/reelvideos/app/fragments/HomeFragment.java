package com.reelvideos.app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.CommentBottomSheet;
import com.reelvideos.app.DifferentProfileActivity;
import com.reelvideos.app.LoginActivity;
import com.reelvideos.app.MainActivity;
import com.reelvideos.app.PostActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.adapters.HomeVideosAdapter;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.callbacks.ServiceCallback;
import com.reelvideos.app.config.Constants;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentHomeBinding;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.services.UploadService;
import com.reelvideos.app.utils.PreLoaderUtil;
import com.reelvideos.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_HOME_VIDEOS;
import static com.reelvideos.app.config.Constants.API_REPORT_VIDEO;
import static com.reelvideos.app.config.Constants.API_SAVE_VIDEO;
import static com.reelvideos.app.config.Constants.API_VIEW_VIDEO;
import static com.reelvideos.app.config.Constants.followMap;
import static com.reelvideos.app.config.Constants.getDislikeVideoAPI;
import static com.reelvideos.app.config.Constants.getDownloadVideoAPI;
import static com.reelvideos.app.config.Constants.getLikeVideoAPI;
import static com.reelvideos.app.config.Constants.getShareVideoAPI;
import static com.reelvideos.app.utils.AppExtensions.checkPermissions;
import static com.reelvideos.app.utils.AppExtensions.showToast;
import static com.reelvideos.app.utils.Utils.callApiForUploadVideoWatchedTime;
import static com.reelvideos.app.utils.Utils.downloadVideo;
import static com.reelvideos.app.utils.Utils.shareVideo;


public class HomeFragment extends BaseFragment implements Player.EventListener {

    FragmentHomeBinding binding;
    ArrayList<HomeVideosModel> dataList = new ArrayList<>();
    int currentPage = -1;
    HomeVideosAdapter homeVideosAdapter;
    LinearLayoutManager layoutManager;
    boolean isUserStopVideo = false;
    boolean isVisibleToUser = true;
    SimpleExoPlayer previousPlayer;
    String nextAPI_LINK = "NA";
    UploadingVideoBroadCast mReceiver;
    Cache simpleCache = CoreApp.simpleCache;
    //shubham_keshri

    long VideoStartTime = 0, VideoEndTime = 0, videoPausedFor = 0, PauseStartTime = 0, PauseEndTime = 0;
    String video_Id, android_ID;

    int sharedPosition;
    HomeVideosModel sharedItem;
    ProcessSharedBroadCast processSharedBroadCast;

    private class ProcessSharedBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            processShareVideo(sharedPosition, sharedItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //removeTransparent ();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    @Override
    public void initComponents() {

        processSharedBroadCast = new ProcessSharedBroadCast();
        activity.registerReceiver(processSharedBroadCast, new IntentFilter("confirmShared"));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.dataRecycler);
        layoutManager = new LinearLayoutManager(activity);
        binding.dataRecycler.setLayoutManager(layoutManager);
        binding.dataRecycler.setHasFixedSize(false);

        mReceiver = new UploadingVideoBroadCast();
        getActivity().registerReceiver(mReceiver, new IntentFilter("uploadVideo"));

        //Get android Id shubham_keshri
        android_ID = Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        //Check for Upload Process
        if (Utils.isMyServiceRunning(activity, UploadService.class)) {
            binding.uploadVideoLayout.setVisibility(View.VISIBLE);
            Bitmap bitmap = Utils.base64ToBitmap(CoreApp.getInstance().getSharedPreferences().getString(Constants.PREF_THUMBNAIL_UPLOAD, ""));
//            if (bitmap != null)
//                binding.uploadingThumb.setImageBitmap(bitmap);
        }


        // this is the scroll listener of recycler view which will tell the current item number
        binding.dataRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //Toast.makeText ( activity, String.valueOf ( currentPage ), Toast.LENGTH_SHORT ).show ();
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    currentPage = page_no;
                    releasePreviousPlayer();
                    setPlayer(currentPage);

                    //shubham_keshri
                    video_Id = dataList.get(currentPage).getVideoID();
                    VideoStartTime = System.currentTimeMillis();
                }
            }
        });
        callApiForAllVideos();
    }

    private void releasePreviousPlayer() {
        if (previousPlayer != null) {
            VideoEndTime = System.currentTimeMillis();
            previousPlayer.removeListener(this);
            previousPlayer.release();
            //shubham_keshri

//            callApiForUploadVideoWatchedTime(video_Id, VideoStartTime, VideoEndTime, videoPausedFor, PauseStartTime);

            VideoStartTime = 0;
            VideoEndTime = 0;
            videoPausedFor = 0;
            PauseStartTime = 0;
        }
    }


    private void callApiForAllVideos() {
//        binding.swipeRefresh.setRefreshing(true);
        if (dataList != null)
            dataList.clear();

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> {
            // update the ui from here
            callNonAuthAPI(API_HOME_VIDEOS + "&anonymous_user_id=" + android_ID, 0, new ApiResponseCallback() {

                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {
                    currentPage = -1;
                    loadUpList(jsonObject);
                    setAdapter();
                }

                @Override
                public void onApiFailureResult(Exception e) {

                }

            });

        });
    }

    private void callApiForAddingMoreVideos() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> {
            // update the ui from here
            callNonAuthAPI(nextAPI_LINK, 0, new ApiResponseCallback() {
                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {
                    loadUpList(jsonObject);
                }

                @Override
                public void onApiFailureResult(Exception e) {
                }
            });
        });
    }

    private void loadUpList(JSONObject jsonObject) {
        HomeVideosModel tempModel;
        try {
            String nextAPI = jsonObject.optString(getString(R.string.next).toLowerCase());
            String[] metaData = nextAPI.split("&");
            nextAPI_LINK = metaData[0] + "&" + metaData[1] + "&" + metaData[3];

            JSONArray resultsArray = jsonObject.getJSONArray(getString(R.string.results));
            for (int i = 0; i < resultsArray.length(); i++) {
                boolean duplicate = false;

                tempModel = new HomeVideosModel();
                JSONObject currentVideoObject = resultsArray.getJSONObject(i);
                tempModel.setCreatorID(currentVideoObject.optLong(getString(R.string.creator)));
                tempModel.setCreatorIMG(currentVideoObject.optString(getString(R.string.creator_pic)));
                tempModel.setDownloadsCount(currentVideoObject.optLong(getString(R.string.downloads)));
                tempModel.setSharesCount(currentVideoObject.optLong(getString(R.string.shares)));
                tempModel.setVideoCommentsCount(currentVideoObject.optLong(getString(R.string.num_comments)));
                tempModel.setVideoLikesCount(currentVideoObject.optLong(getString(R.string.num_likes)));
                tempModel.setVideoCreatorName(currentVideoObject.optString(getString(R.string.creator_username)));
                tempModel.setVideoDescription(currentVideoObject.optString(getString(R.string.description)));
                tempModel.setVideoURL(currentVideoObject.optString(getString(R.string.file)));
                tempModel.setVideoID(currentVideoObject.optLong(getString(R.string.id)));
                tempModel.setPosterURL(currentVideoObject.optString(getString(R.string.poster)));
                tempModel.setHashTags(currentVideoObject.optJSONArray(getString(R.string.hashtags)));
                tempModel.setLiked(currentVideoObject.optBoolean(getString(R.string.is_liked)));
                tempModel.setFollowed(currentVideoObject.optBoolean(getString(R.string.is_following)));
                tempModel.setVerified(currentVideoObject.optBoolean(getString(R.string.creator_is_verified)));
                tempModel.setSoundName("");

                for (int j = 0; j < dataList.size(); j++) {
                    if (dataList.get(j).getVideoID().equals(tempModel.getVideoID())) {
                        duplicate = true;
                    }
                }
                if (!duplicate)
                    dataList.add(tempModel);

                if (homeVideosAdapter != null)
                    homeVideosAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            showToast(activity, "" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setAdapter() {
        homeVideosAdapter = new HomeVideosAdapter(activity, dataList, (position, item, view) -> {
            switch (view.getId()) {

                case R.id.user_pic:

                case R.id.username:
                    onPause();
                    if (followMap.size() > 0) {
                        if (followMap.containsKey(String.valueOf(item.getCreatorID()))) {
                            OpenProfile(item.getCreatorID(), item.getVideoCreatorName(), item.getCreatorIMG(), followMap.get(String.valueOf(item.getCreatorID())));
                        } else {
                            OpenProfile(item.getCreatorID(), item.getVideoCreatorName(), item.getCreatorIMG(), item.isFollowed());
                        }
                    } else {
                        OpenProfile(item.getCreatorID(), item.getVideoCreatorName(), item.getCreatorIMG(), item.isFollowed());
                    }
                    break;

                case R.id.like_layout:
                    if (GlobalVariables.hasUserLoggedIN()) {
                        processLikeVideo(currentPage, item);
                    } else {
                        showToast(activity, getString(R.string.login_conti));
                        openLoginActivity();
                    }
                    break;

                case R.id.comment_layout:
                    CommentBottomSheet commentBottomSheet = new CommentBottomSheet();
                    commentBottomSheet.OpenComment(item.getVideoDescription(), item.getCreatorIMG(), item.getVideoCreatorName(), item.getVideoID(), String.valueOf(item.getVideoCommentsCount()), item.getHashTags(), item.isVerified());
                    commentBottomSheet.show(getParentFragmentManager(), "TAG");
                    break;
/*
                case R.id.download_layout:
                    if (checkPermissions(activity)) {
                        downloadVideo(activity, item.getVideoURL(), String.valueOf(item.getVideoID()));
                        processDownloadVideo(currentPage, item);
                        callNonAuthAPI(getDownloadVideoAPI(item.getVideoID()), 0, new ApiResponseCallback() {
                            @Override
                            public void onApiSuccessResult(JSONObject jsonObject) {
                            }
                            @Override
                            public void onApiFailureResult(Exception e) {
                            }
                        });
                    } else showToast(activity, getString(R.string.permissions));
                    break;
*/

                case R.id.shared_layout:

                    if (checkPermissions(activity)) {
                        shareVideo(activity, item.getVideoURL(), String.valueOf(item.getVideoID()));
                        //shareVideoLink(activity, String.valueOf(item.getVideoID()));
                        //processShareVideo(currentPage, item);
                        sharedPosition = position;
                        sharedItem = item;
                        Handler handler = new Handler(Looper.getMainLooper());
                        // separate thread is used for network calls
                        handler.post(() -> callNonAuthAPI(getShareVideoAPI(item.getVideoID()), 0, new ApiResponseCallback() {
                            @Override
                            public void onApiSuccessResult(JSONObject jsonObject) {

                            }

                            @Override
                            public void onApiFailureResult(Exception e) {

                            }
                        }));

                    } else showToast(activity, getString(R.string.permissions));
            }

        });

        homeVideosAdapter.setHasStableIds(true);
        homeVideosAdapter.notifyDataSetChanged();
        binding.dataRecycler.setAdapter(homeVideosAdapter);

    }

    private void OpenProfile(long creatorID, String videoCreatorName, String creatorIMG, boolean isFollowing) {
        if (GlobalVariables.getUserId().equals(String.valueOf(creatorID))) {

            //In case of Own Profile
            TabLayout.Tab profile = ((MainActivity) activity).binding.tabs.getTabAt(4);
            profile.select();

        } else {
            Intent intent = new Intent(getActivity(), DifferentProfileActivity.class);
            intent.putExtra(getString(R.string.ID), String.valueOf(creatorID));
            intent.putExtra(getString(R.string.NAME), videoCreatorName);
            intent.putExtra(getString(R.string.IMG), creatorIMG);
            intent.putExtra(getString(R.string.isFollowed), isFollowing);
            startActivity(intent);

            activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

        }
    }

    private void processShareVideo(int pos, HomeVideosModel item) {
        if (item != null) {
            dataList.remove(pos);
            item.setSharesCount(item.getSharesCount() + 1);
            dataList.add(pos, item);
            homeVideosAdapter.notifyDataSetChanged();
        }
    }

    public void setPlayer(final int currentPage) {
        final HomeVideosModel item = dataList.get(currentPage);
        updateViewCount(item.getVideoID());

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(2000 , 5000, 1500, 2000)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true)
                .createDefaultLoadControl();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(activity);
        final SimpleExoPlayer player = new SimpleExoPlayer.Builder(activity).setLoadControl(loadControl).setTrackSelector(trackSelector).build();

        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache,
                new DefaultHttpDataSourceFactory(Util.getUserAgent(activity, activity.getString(R.string.app_name))),
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(item.getVideoURL()));

        player.prepare(progressiveMediaSource);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        View layout = layoutManager.findViewByPosition(currentPage);

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    binding.progressLayout.setVisibility(View.VISIBLE);
                    layout.findViewById(R.id.thumbnail).setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    binding.progressLayout.setVisibility(View.GONE);
                    layout.findViewById(R.id.thumbnail).setVisibility(View.GONE);
                }
            }
        });

        final PlayerView playerView = layout.findViewById(R.id.playerView);
        playerView.setPlayer(player);

        player.setPlayWhenReady(isVisibleToUser);
        previousPlayer = player;

        preloadNextVideo();

        final RelativeLayout mainLayout = layout.findViewById(R.id.mainLayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if ((deltaXAbs > 900) && (deltaXAbs < 1000)) {
                        if (deltaX > 500) {
                            OpenProfile(item.getCreatorID(), item.getVideoCreatorName(), item.getCreatorIMG(), item.isFollowed());
                        }
                    }
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    super.onSingleTapConfirmed(e);
                    if (!player.getPlayWhenReady()) {
                        isUserStopVideo = false;
                        previousPlayer.setPlayWhenReady(true);

                        //shubham_keshri
                        PauseEndTime = System.currentTimeMillis();
                        videoPausedFor += Math.abs(PauseEndTime - PauseStartTime) / 1000;
                        PauseEndTime = 0;
                        PauseStartTime = 0;
                    } else {

                        isUserStopVideo = true;
                        previousPlayer.setPlayWhenReady(false);

                        //shubham_keshri
                        PauseStartTime = System.currentTimeMillis();
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    showOptionsDialog(item.getVideoID(), item.getVideoURL());
                    onPause();
                    super.onLongPress(e);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if (!player.getPlayWhenReady()) {
                        isUserStopVideo = false;
                        previousPlayer.setPlayWhenReady(true);
                    }


                    if (GlobalVariables.hasUserLoggedIN()) {
                        showHeartAnim(item, mainLayout, e);
                        if (!item.isLiked()) processLikeVideo(currentPage, item);
                    } else {
                        showToast(activity, getString(R.string.login_conti));
                        openLoginActivity();
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        LinearLayout soundImage = layout.findViewById(R.id.sound_image_layout);
        Animation sound_animation = AnimationUtils.loadAnimation(activity, R.anim.d_clockwise_rotation);
        soundImage.startAnimation(sound_animation);

        if (currentPage != 0 && currentPage % 15 == 0)
            callApiForAddingMoreVideos();
    }

    private void updateViewCount(String videoID) {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (GlobalVariables.hasUserLoggedIN()) {
                    callAuthAPI(API_VIEW_VIDEO.replace("%video_id%", videoID), 0, new ApiResponseCallback() {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {

                        }

                        @Override
                        public void onApiFailureResult(Exception e) {

                        }
                    });
                } else {
                    Log.i("non auth url", API_VIEW_VIDEO.replace("%video_id%", videoID) + "?anonymous_user_id=" + android_ID);
                    callNonAuthAPI(API_VIEW_VIDEO.replace("%video_id%", videoID) + "?anonymous_user_id=" + android_ID, 0, new ApiResponseCallback() {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {
                            //Toast.makeText ( activity, "done", Toast.LENGTH_SHORT ).show ();
                        }

                        @Override
                        public void onApiFailureResult(Exception e) {
                            //Toast.makeText ( activity, "failed", Toast.LENGTH_SHORT ).show ();

                        }

                    });
                }

            }
        });


    }

    private void openLoginActivity() {

        Intent intent = new Intent(activity, LoginActivity.class);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);

    }

    private void processLikeVideo(int pos, HomeVideosModel item) {

        boolean action = item.isLiked();
        String URL_TO_ACT;

        if (action) {
            action = false;
            item.setVideoLikesCount(item.getVideoLikesCount() - 1);
            URL_TO_ACT = getDislikeVideoAPI(String.valueOf(item.getVideoID()));
        } else {
            action = true;
            item.setVideoLikesCount(item.getVideoLikesCount() + 1);
            URL_TO_ACT = getLikeVideoAPI(String.valueOf(item.getVideoID()));
        }


        dataList.remove(pos);
        item.setLiked(action);
        dataList.add(pos, item);
        homeVideosAdapter.notifyDataSetChanged();

        //Call API for Update
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {

                callAuthAPI(URL_TO_ACT, 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });

            }
        });


    }

    private void showHeartAnim(HomeVideosModel item, RelativeLayout mainLayout, MotionEvent e) {

        int x = (int) e.getX() - 100;
        int y = (int) e.getY() - 100;
        int mWidth = this.getResources().getDisplayMetrics().widthPixels;
        int mHeight = this.getResources().getDisplayMetrics().heightPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(activity.getApplicationContext());
        lp.setMargins((mWidth / 2) - 125, (mHeight / 2) - 175, 0, 0);
        iv.setLayoutParams(lp);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_lyte_heart));


        mainLayout.addView(iv);
        Animation fadeOutAnim = AnimationUtils.loadAnimation(activity, R.anim.fade_out);

        fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainLayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeOutAnim);

    }

    @Override
    public void onResume() {
        super.onResume();
        if ((previousPlayer != null && (isVisibleToUser && !isUserStopVideo)) && !is_fragment_exits()) {
            previousPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (previousPlayer != null) {
            previousPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (previousPlayer != null) {
            previousPlayer.setPlayWhenReady(false);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (previousPlayer != null) {
            previousPlayer.release();
        }
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        if (processSharedBroadCast != null) {
            activity.unregisterReceiver(processSharedBroadCast);
            processSharedBroadCast = null;
        }
        if (Utils.isMyServiceRunning(activity, UploadService.class)) {
            //PostActivity.stopUploadService(activity);
        }
    }

    public boolean is_fragment_exits() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        return fm.getBackStackEntryCount() != 0;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (previousPlayer != null && (isVisibleToUser && !isUserStopVideo)) {
            previousPlayer.setPlayWhenReady(true);
        } else if (previousPlayer != null && !isVisibleToUser) {
            previousPlayer.setPlayWhenReady(false);
        }
    }


    private void preloadNextVideo() {
        if (currentPage + 1 < dataList.size())
            new PreLoaderUtil().preCacheVideo(dataList.get(currentPage + 1).getVideoURL(), activity);
    }


    private class UploadingVideoBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Utils.isMyServiceRunning(context, UploadService.class)) {
                binding.uploadVideoLayout.setVisibility(View.VISIBLE);
                Bitmap bitmap = Utils.base64ToBitmap(CoreApp.getInstance().getSharedPreferences().getString(Constants.PREF_THUMBNAIL_UPLOAD, ""));
//                if (bitmap != null)
//                    binding.uploadingThumb.setImageBitmap(bitmap);

            } else {
                binding.uploadVideoLayout.setVisibility(View.GONE);
            }

        }
    }

    private void showOptionsDialog(String videoID, String videoURL) {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_otions_popup, null);
        AlertDialog dialog = myDialog.create();
        dialog.setView(view);

        ImageView save = view.findViewById(R.id.saveImgBtn);
        ImageView download = view.findViewById(R.id.downloadImgBtn);
        ImageView report = view.findViewById(R.id.reportImgBtn);

        save.setOnClickListener(view1 -> {
            processSaveVideo(videoID);
            onResume();
            activity.sendBroadcast(new Intent("saveVideo"));
            dialog.dismiss();
        });

        download.setOnClickListener(view2 -> {
            startDownload(video_Id, videoURL);
            onResume();
            dialog.dismiss();
        });

        report.setOnClickListener(view3 -> {
            processVideoReport(videoID);
            onResume();
            dialog.dismiss();
        });

        dialog.show();

    }

    private void processSaveVideo(String videoID) {
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> callAuthAPI(API_SAVE_VIDEO.replace("%video_id%", videoID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {

            }

            @Override
            public void onApiFailureResult(Exception e) {

            }
        }));

        Toast.makeText(activity, getString(R.string.video_saved), Toast.LENGTH_SHORT).show();

    }

    private void processVideoReport(String videoID) {
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {
                callAuthAPI(API_REPORT_VIDEO.replace("%video_id%", videoID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            }
        });

        Toast.makeText(activity, getString(R.string.video_reported), Toast.LENGTH_SHORT).show();
    }

    private void startDownload(String video_Id, String video_url) {
        if (checkPermissions(activity)) {
            downloadVideo(activity, video_url, video_Id);

            Handler handler = new Handler(Looper.getMainLooper());
            // separate thread is used for network calls
            handler.post(() -> callNonAuthAPI(getDownloadVideoAPI(video_Id), 0, new ApiResponseCallback() {
                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {

                }

                @Override
                public void onApiFailureResult(Exception e) {
                }
            }));
        } else showToast(activity, getString(R.string.permissions));
    }
    /*public void removeTransparent(){
        activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }*/

}