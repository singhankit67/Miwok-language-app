package com.reelvideos.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.Notification.NotificationFragment;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.ActivityMainBinding;
import com.reelvideos.app.discover.DiscoverFragment;
import com.reelvideos.app.fragments.DummyFragment;
import com.reelvideos.app.fragments.HomeFragment;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.profile.ProfileFragment;
import com.reelvideos.app.recorder.RecordActivity;
import com.reelvideos.app.services.UploadService;
import com.reelvideos.app.utils.AppExtensions;
import com.reelvideos.app.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_GET_USER;
import static com.reelvideos.app.config.Constants.getSharedVideo;
import static com.reelvideos.app.config.GlobalVariables.hasUserLoggedIN;
import static com.reelvideos.app.utils.AppExtensions.checkPermissions;
import static com.reelvideos.app.utils.AppExtensions.showToast;
import static com.reelvideos.app.utils.Utils.createAppDirs;


public class MainActivity extends BaseActivity {

    public String receivedVideoId;
    public String receivedProfileID;
    public String receivedProfileName = null;
    public String receivedProfileIMG = null;
    public boolean isFollowing;
    public ActivityMainBinding binding;
    TabLayout tabLayout;
    boolean doubleBackToExitPressedOnce = false;
    ArrayList<HomeVideosModel> dataList = new ArrayList<>();
    int position;
    private static final int PERMISSION_REQUEST_CODE = 450;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addTransparent();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initComponents();

        //getting the video id from shared link
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //receiving the link
        super.onNewIntent(intent);
        Uri url = intent.getData();
        String action = intent.getAction();
        deepLinkSwitcher(url, action);
    }

    private void deepLinkSwitcher(Uri url, String action) {
        //opening requested activity
        if (Intent.ACTION_VIEW.equals(action) && url != null) {
            List<String> data = url.getPathSegments();
            if (data.get(data.size() - 2).equals("videos")) {
                receivedVideoId = data.get(data.size() - 1);
                loadSharedVideo(receivedVideoId);
            } else if (data.get(data.size() - 2).equals("profile")) {
                receivedProfileID = data.get(data.size() - 1);
                loadSharedProfile(receivedProfileID);
            }
        }
    }

    private void loadSharedProfile(String receivedProfileID) {
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> {
            if (GlobalVariables.hasUserLoggedIN()) {
                callAuthAPI(API_GET_USER.replace("%id%", receivedProfileID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        receivedProfileName = jsonObject.optString("fullname");
                        receivedProfileIMG = jsonObject.optString("pic");
                        isFollowing = jsonObject.optBoolean("is_following");
                    }


                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            } else {
                callNonAuthAPI(API_GET_USER.replace("%id%", receivedProfileID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        receivedProfileName = jsonObject.optString("fullname");
                        receivedProfileIMG = jsonObject.optString("pic");
                        isFollowing = jsonObject.optBoolean("is_following");
                    }


                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            }
        });

        OpenProfile(receivedProfileID, receivedProfileName, receivedProfileIMG, isFollowing);

    }

    private void OpenProfile(String creatorID, String videoCreatorName, String creatorIMG, boolean isFollowing) {
        //Other Profile
        Intent intent = new Intent(this, DifferentProfileActivity.class);
        intent.putExtra("ID", String.valueOf(creatorID));
        intent.putExtra("NAME", videoCreatorName);
        intent.putExtra("IMG", creatorIMG);
        intent.putExtra("isFollowed", isFollowing);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);

    }


    @Override
    public void initComponents() {
        //Init Context
        CoreApp.getInstance().setActivity(this);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        binding.viewpager.setOffscreenPageLimit(5);
        binding.viewpager.setPagingEnabled(false);
        binding.viewpager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.viewpager);
        tabLayout = findViewById(R.id.tabs);
        view = binding.getRoot();
        setupTabIcons();
        createAppDirs();
    }

    private void loadSharedVideo(String receivedVideoId) {
        //getting video object from backend
        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> {
            if (GlobalVariables.hasUserLoggedIN()) {
                callAuthAPI(getSharedVideo(receivedVideoId), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        loadUpList(jsonObject);
                    }

                    @Override
                    public void onApiFailureResult(Exception e) {

                    }
                });
            } else {
                callNonAuthAPI(getSharedVideo(receivedVideoId), 0, new ApiResponseCallback() {
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

    private void loadUpList(JSONObject currentVideoObject) {
        //loading the info from json object to array list
        HomeVideosModel tempModel;
        tempModel = new HomeVideosModel();

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
        tempModel.setSoundName("");
        dataList.add(tempModel);

        openWatchVideo(position);
    }

    public void openWatchVideo(int position) {
        Intent intent = new Intent(getApplicationContext(), WatchVideosActivity.class);
        intent.putExtra("dataArray", dataList);
        intent.putExtra("position", position);
        intent.putExtra("isFromProfile", false);
        startActivity(intent);
    }

    private void setupTabIcons() {

        View view1 = LayoutInflater.from(this).inflate(R.layout.item_main_tablayout, (ViewGroup) null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.home_active, null));
        imageView1.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(binding.tabs.getTabAt(0)).setCustomView(view1);

        View view2 = LayoutInflater.from(this).inflate(R.layout.item_main_tablayout, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
//        imageView2.setScaleX(1.2f);
//        imageView2.setScaleY(1.2f);
        imageView2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_explore_outline, null));
        imageView2.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tabs.getTabAt(1).setCustomView(view2);


        View view3 = LayoutInflater.from(this).inflate(R.layout.item_main_tablayout, null);
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setScaleX(1.7f);
        imageView3.setScaleY(1.7f);
        imageView3.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_add, null));
//        imageView3.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        binding.tabs.getTabAt(2).setCustomView(view3);

        View view4 = LayoutInflater.from(this).inflate(R.layout.item_main_tablayout, null);
        ImageView imageView4 = view4.findViewById(R.id.image);
        imageView4.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_messages_outline, null));
        imageView4.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tabs.getTabAt(3).setCustomView(view4);

        View view5 = LayoutInflater.from(this).inflate(R.layout.item_main_tablayout, null);
        ImageView imageView5 = view5.findViewById(R.id.image);

        imageView5.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_person_outline, null));
        imageView5.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.tabs.getTabAt(4).setCustomView(view5);


        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        onHomeClick();
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.home_active, null));
                        break;
                    case 1:
                        OnOtherTabClick();
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_explore, null));
                        break;
                    case 3:
                        OnOtherTabClick();
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_messages, null));
                        break;
                    case 4:
                        OnOtherTabClick();
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_person, null));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.home, null));
                        break;
                    case 1:
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_explore_outline, null));
                        break;
                    case 3:
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_messages_outline, null));
                        break;
                    case 4:
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lyte_person_outline, null));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        final LinearLayout tabStrip = ((LinearLayout) binding.tabs.getChildAt(0));
        tabStrip.setEnabled(false);
        tabStrip.getChildAt(0).setClickable(false);
        view1.setOnClickListener(view -> {
            addTransparent();
            TabLayout.Tab tab = binding.tabs.getTabAt(0);
            tab.select();
        });


        tabStrip.getChildAt(1).setClickable(false);
        view2.setOnClickListener(view -> {
            removeTransparent();
            TabLayout.Tab tab = binding.tabs.getTabAt(1);
            tab.select();
        });


        tabStrip.getChildAt(2).setClickable(true);
        view3.setOnClickListener(v -> {
            if (checkPermissions(MainActivity.this)) {

                if (hasUserLoggedIN()) {
                    if (Utils.isMyServiceRunning(MainActivity.this, UploadService.class)) {
                        showToast(MainActivity.this, getString(R.string.progress_video));
                    } else
                        startActivity(new Intent(getApplicationContext(), RecordActivity.class));
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
                }
            }
        });


        tabStrip.getChildAt(3).

                setClickable(false);
        view4.setOnClickListener(v ->

        {

            if (hasUserLoggedIN()) {
                removeTransparent();
                TabLayout.Tab tab = binding.tabs.getTabAt(3);
                tab.select();

            } else {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
            }

        });

        tabStrip.getChildAt(4).

                setClickable(false);
        view5.setOnClickListener(v ->

        {

            if (hasUserLoggedIN()) {
                removeTransparent();
                TabLayout.Tab tab = binding.tabs.getTabAt(4);
                tab.select();

            } else {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
            }

        });

        onHomeClick();
    }

    public void addTransparent() {
        // tabLayout.setBackgroundColor(getResources().getColor(R.color.transparentColor));

    }

    public void removeTransparent() {

    }

    private void OnOtherTabClick() {

        TabLayout.Tab tab1 = binding.tabs.getTabAt(1);
        View view1 = tab1.getCustomView();
        ImageView imageView1 = view1.findViewById(R.id.image);
//        imageView1.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        tab1.setCustomView(view1);


        TabLayout.Tab tab3 = binding.tabs.getTabAt(3);
        View view3 = tab3.getCustomView();
        ImageView imageView3 = view3.findViewById(R.id.image);
//        imageView3.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        tab3.setCustomView(view3);


        TabLayout.Tab tab4 = binding.tabs.getTabAt(4);
        View view4 = tab4.getCustomView();
        ImageView imageView4 = view4.findViewById(R.id.image);
//        imageView4.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
        tab4.setCustomView(view4);

        binding.tabs.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.d_top_white_line, null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        }


    }

    private void onHomeClick() {
        TabLayout.Tab tab1 = binding.tabs.getTabAt(1);
        View view1 = tab1.getCustomView();
        ImageView imageView1 = view1.findViewById(R.id.image);
//        imageView1.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        tab1.setCustomView(view1);


        TabLayout.Tab tab3 = binding.tabs.getTabAt(3);
        View view3 = tab3.getCustomView();
        ImageView imageView3 = view3.findViewById(R.id.image);
//        imageView3.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        tab3.setCustomView(view3);


        TabLayout.Tab tab4 = binding.tabs.getTabAt(4);
        View view4 = tab4.getCustomView();
        ImageView imageView4 = view4.findViewById(R.id.image);
//        imageView4.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite_50), android.graphics.PorterDuff.Mode.SRC_IN);
        tab4.setCustomView(view4);

        binding.tabs.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.d_top_white_line, null));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment result = null;
            switch (position) {
                case 0:
                    result = new HomeFragment();
                    break;
                case 1:
                    result = new DiscoverFragment();
                    break;
                case 2:
                    result = new DummyFragment();
                    break;
                case 3:
                    result = new NotificationFragment();
                    break;
                case 4:
                    result = new ProfileFragment();
                    break;
                default:
                    result = null;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 5;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            registeredFragments.remove(position);

            super.destroyItem(container, position, object);

        }

        public Fragment getRegisteredFragment(int position) {

            return registeredFragments.get(position);

        }

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppExtensions.deleteCache(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //CoreApp.getProxy(this).shutdown();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() == 0) {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            showToast(this, getString(R.string.exit_msg));

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else
            super.onBackPressed();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean readStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (readStorageAccepted && writeStorageAccepted && cameraAccepted) {
                        if (hasUserLoggedIN()) {

                            if (Utils.isMyServiceRunning(MainActivity.this, UploadService.class)) {
                                showToast(MainActivity.this, getString(R.string.progress_video));
                            } else
                                startActivity(new Intent(getApplicationContext(), RecordActivity.class));

                        }
                    } else {

                        Snackbar.make(view, R.string.permission_denied, Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel(getString(R.string.both_permission_msg),
                                        (dialog, which) -> {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                Manifest.permission.CAMERA},
                                                        PERMISSION_REQUEST_CODE);
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}