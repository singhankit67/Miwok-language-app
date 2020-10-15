package com.reelvideos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.reelvideos.app.adapters.HomeVideosAdapter;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.ActivityWatchVideosBinding;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.utils.AppExtensions;
import com.reelvideos.app.utils.PreLoaderUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.base.CoreApp.simpleCache;
import static com.reelvideos.app.config.Constants.API_REPORT_VIDEO;
import static com.reelvideos.app.config.Constants.API_SAVE_VIDEO;
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

public class WatchVideosActivity extends BaseActivity {

    ActivityWatchVideosBinding binding;
    ArrayList<HomeVideosModel> dataList = new ArrayList<>();
    int position = 0;
    int currentPage = -1;
    HomeVideosAdapter watchVideoAdapter;
    SimpleExoPlayer previousPlayer;
    LinearLayoutManager layoutManager;
    boolean isUserStopVideo = false;
    boolean isVisibleToUser = true;
    String nextAPI;
    boolean isFromProfile = true;
    boolean isPlaylistEnded = false;

    //shubham_keshri
    long VideoStartTime = 0, VideoEndTime = 0, videoPausedFor = 0, PauseStartTime = 0, PauseEndTime = 0;
    String video_Id, description;

    //    Intent receiver;
//    PendingIntent pendingIntent;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWatchVideosBinding.inflate(getLayoutInflater());
        initComponents();
        setContentView(binding.getRoot());
    }

    @Override
    public void initComponents() {

        processSharedBroadCast = new ProcessSharedBroadCast();
        registerReceiver(processSharedBroadCast, new IntentFilter("confirmShared"));

//        receiver = new Intent(this, SharedReceiver.class);
//        pendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent bundle = getIntent();

        //proxy = CoreApp.getProxy(this);

        if (bundle != null) {
            position = bundle.getIntExtra("position", 0);
            dataList = (ArrayList<HomeVideosModel>) bundle.getSerializableExtra("dataArray");
            isFromProfile = bundle.getBooleanExtra("isFromProfile", true);

            //if (!isFromProfile)
            nextAPI = bundle.getStringExtra("NEXT_API");


        }

        binding.Goback.setOnClickListener(view -> onBackPressed());
        layoutManager = new LinearLayoutManager(this);
        binding.dataRecycler.setLayoutManager(layoutManager);
        binding.dataRecycler.setHasFixedSize(false);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.dataRecycler);
        watchVideoAdapter = new HomeVideosAdapter(this, dataList, (position, item, view) -> {

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
                        showToast(this, getString(R.string.login_conti));
                        openLoginActivity();
                    }
                    break;

                case R.id.comment_layout:
                    CommentBottomSheet commentBottomSheet = new CommentBottomSheet();
                    commentBottomSheet.OpenComment(item.getVideoDescription(), item.getCreatorIMG(), item.getVideoCreatorName(), item.getVideoID(), String.valueOf(item.getVideoCommentsCount()), item.getHashTags(), item.isVerified());
                    commentBottomSheet.show(getSupportFragmentManager(), "TAG");
                    break;
                case R.id.download_layout:
                    if (checkPermissions(this)) {
                        downloadVideo(this, item.getVideoURL(), String.valueOf(item.getVideoID()));
                        processDownloadVideo(currentPage, item);
                        Handler handler = new Handler(Looper.getMainLooper());
                        // separate thread is used for network calls
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callNonAuthAPI(getDownloadVideoAPI(item.getVideoID()), 0, new ApiResponseCallback() {
                                    @Override
                                    public void onApiSuccessResult(JSONObject jsonObject) {
                                        Toast.makeText(WatchVideosActivity.this, R.string.download_success, Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onApiFailureResult(Exception e) {
                                    }
                                });
                            }
                        });
                    } else showToast(this, getString(R.string.permissions));
                    break;

                case R.id.shared_layout:

                    if (checkPermissions(this)) {
                        shareVideo(this, item.getVideoURL(), String.valueOf(item.getVideoID()));
                        sharedPosition = position;
                        sharedItem = item;
                        Handler handler = new Handler(Looper.getMainLooper());
                        // separate thread is used for network calls
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callNonAuthAPI(getShareVideoAPI(item.getVideoID()), 0, new ApiResponseCallback() {
                                    @Override
                                    public void onApiSuccessResult(JSONObject jsonObject) {

                                    }

                                    @Override
                                    public void onApiFailureResult(Exception e) {

                                    }
                                });
                            }
                        });

                    } else showToast(this, getString(R.string.permissions));
            }

        });


        watchVideoAdapter.setHasStableIds(true);
        binding.dataRecycler.setAdapter(watchVideoAdapter);
        watchVideoAdapter.notifyDataSetChanged();

        binding.dataRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    if (!isPlaylistEnded) {
                        currentPage = page_no;
                        removePreviousPlayer();
                        setPlayer(currentPage);
                        video_Id = dataList.get(currentPage).getVideoID();
                        description = dataList.get(currentPage).getVideoDescription();

                        VideoStartTime = System.currentTimeMillis();
                    }
                }
            }
        });

        binding.dataRecycler.scrollToPosition(position);
    }

    private void removePreviousPlayer() {

        if (previousPlayer != null) {
            VideoEndTime = System.currentTimeMillis();
            previousPlayer.release();
            callApiForUploadVideoWatchedTime(video_Id, VideoStartTime, VideoEndTime, videoPausedFor, PauseStartTime);
            VideoStartTime = 0;
            VideoEndTime = 0;
            videoPausedFor = 0;
            PauseStartTime = 0;
        }
    }

    private void processDownloadVideo(int pos, HomeVideosModel item) {
        dataList.remove(pos);
        item.setDownloadsCount(item.getDownloadsCount() + 1);
        dataList.add(pos, item);
        watchVideoAdapter.notifyDataSetChanged();
    }

    private void processShareVideo(int pos, HomeVideosModel item) {
        if (item != null) {
            dataList.remove(pos);
            item.setSharesCount(item.getSharesCount() + 1);
            dataList.add(pos, item);
            watchVideoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();
    }


    public void setPlayer(final int currentPage) {

        final HomeVideosModel item = dataList.get(currentPage);

        preloadNextVideo();

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(1024, 1024, 500, 1024)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true)
                .createDefaultLoadControl();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        final SimpleExoPlayer player = new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .build();


        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache,
                new DefaultHttpDataSourceFactory(Util.getUserAgent(this, getString(R.string.app_name))),
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
                    binding.progressHorizontal.setVisibility(View.VISIBLE);
                    assert layout != null;
                    layout.findViewById(R.id.thumbnail).setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    binding.progressHorizontal.setVisibility(View.GONE);
                    assert layout != null;
                    layout.findViewById(R.id.thumbnail).setVisibility(View.GONE);
                }
            }
        });

        assert layout != null;
        final PlayerView playerView = layout.findViewById(R.id.playerView);
        playerView.setPlayer(player);

        player.setPlayWhenReady(isVisibleToUser);
        previousPlayer = player;

        final RelativeLayout mainLayout = layout.findViewById(R.id.mainLayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(WatchVideosActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    if ((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if (deltaX > 0) {
                            onBackPressed();
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
                        PauseEndTime = System.currentTimeMillis();
                        videoPausedFor += Math.abs(PauseEndTime - PauseStartTime) / 1000;
                        PauseEndTime = 0;
                        PauseStartTime = 0;
                    } else {
                        isUserStopVideo = true;
                        previousPlayer.setPlayWhenReady(false);
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
                        showToast(WatchVideosActivity.this, getString(R.string.login_conti));
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
        Animation sound_animation = AnimationUtils.loadAnimation(this, R.anim.d_clockwise_rotation);
        soundImage.startAnimation(sound_animation);

        if (currentPage != 0 && currentPage + 5 > (dataList.size() - 1))
            callApiForAddingMoreVideos();
    }

    private void preloadNextVideo() {
        if (currentPage + 1 < dataList.size())
            new PreLoaderUtil().preCacheVideo(dataList.get(currentPage + 1).getVideoURL(), this);
    }

    private void showHeartAnim(HomeVideosModel item, RelativeLayout mainLayout, MotionEvent e) {

        int mWidth = this.getResources().getDisplayMetrics().widthPixels;
        int mHeight = this.getResources().getDisplayMetrics().heightPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(this.getApplicationContext());
        lp.setMargins((mWidth / 2) - 125, (mHeight / 2) - 125, 0, 0);
        iv.setLayoutParams(lp);
        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_heart, null));

        mainLayout.addView(iv);
        Animation fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fragment_open_enter);

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
        if ((previousPlayer != null && (isVisibleToUser && !isUserStopVideo))) {
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
        if (processSharedBroadCast != null) {
            unregisterReceiver(processSharedBroadCast);
            processSharedBroadCast = null;
        }
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
        watchVideoAdapter.notifyDataSetChanged();

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

    private void callApiForAddingMoreVideos() {
        if (isPlaylistEnded)
            return;
        if (!isFromProfile) {
            Handler handler = new Handler(Looper.getMainLooper());
            // separate thread is used for network calls
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callNonAuthAPI(nextAPI, 0, new ApiResponseCallback() {
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

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            // separate thread is used for network calls
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callNonAuthAPI(nextAPI, 0, new ApiResponseCallback() {
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
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
    }

    private void loadUpList(JSONObject jsonObject) {

        HomeVideosModel tempModel;

        if (isPlaylistEnded)
            return;

        String tempAPI = jsonObject.optString("next");
        String[] metaData = tempAPI.split("&");
        nextAPI = metaData[0] + "&" + metaData[1] + "&" + metaData[3];

        if (nextAPI.length() < 5) {
            isPlaylistEnded = true;
        }

        JSONArray resultsArray = jsonObject.optJSONArray(getString(R.string.results));
        assert resultsArray != null;
        for (int i = 0; i < resultsArray.length(); i++) {
            tempModel = new HomeVideosModel();
            JSONObject currentVideoObject = resultsArray.optJSONObject(i);
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
            dataList.add(tempModel);

            if (watchVideoAdapter != null)
                watchVideoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppExtensions.deleteCache(this);
    }

    private void OpenProfile(long creatorID, String videoCreatorName, String creatorIMG, boolean isFollowed) {
        if (GlobalVariables.getUserId().equals(String.valueOf(creatorID))) {
            onBackPressed();
        } else {
            //Other Profile
            Intent intent = new Intent(this, DifferentProfileActivity.class);
            intent.putExtra("ID", String.valueOf(creatorID));
            intent.putExtra("NAME", videoCreatorName);
            intent.putExtra("IMG", creatorIMG);
            intent.putExtra("isFollowed", isFollowed);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
        }
    }

    private void showOptionsDialog(String videoID, String videoURL) {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(WatchVideosActivity.this);
        LayoutInflater inflater = LayoutInflater.from(WatchVideosActivity.this);
        View view = inflater.inflate(R.layout.layout_otions_popup, null);
        AlertDialog dialog = myDialog.create();
        dialog.setView(view);

        ImageView save = view.findViewById(R.id.saveImgBtn);
        ImageView download = view.findViewById(R.id.downloadImgBtn);
        ImageView report = view.findViewById(R.id.reportImgBtn);

        LinearLayout saveLayout = view.findViewById(R.id.save_layout);
        LinearLayout reportLayout = view.findViewById(R.id.report_layout);

        save.setOnClickListener(view1 -> {
            processSaveVideo(videoID);
            onResume();
            sendBroadcast(new Intent(getString(R.string.save_video)));
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

        if (isFromProfile) {
            reportLayout.setVisibility(View.INVISIBLE);
            saveLayout.setVisibility(View.INVISIBLE);
        }
        dialog.show();

    }

    private void processSaveVideo(String videoID) {
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {
                callAuthAPI(API_SAVE_VIDEO.replace("%"+R.string.video_id+"%", videoID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            }
        });

        Toast.makeText(WatchVideosActivity.this, R.string.video_saved, Toast.LENGTH_SHORT).show();

    }

    private void processVideoReport(String videoID) {
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(new Runnable() {
            @Override
            public void run() {
                callAuthAPI(API_REPORT_VIDEO.replace("%"+R.string.video_id+"%", videoID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {

                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            }
        });

        Toast.makeText(WatchVideosActivity.this, R.string.video_reported, Toast.LENGTH_SHORT).show();
    }

    private void startDownload(String video_Id, String video_url) {
        if (checkPermissions(WatchVideosActivity.this)) {
            downloadVideo(WatchVideosActivity.this, video_url, video_Id);
            Handler handler = new Handler(Looper.getMainLooper());
            // separate thread is used for network calls
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callNonAuthAPI(getDownloadVideoAPI(video_Id), 0, new ApiResponseCallback() {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {

                        }

                        @Override
                        public void onApiFailureResult(Exception e) {
                        }
                    });
                }
            });

        } else showToast(WatchVideosActivity.this, getString(R.string.permissions));
    }



}
