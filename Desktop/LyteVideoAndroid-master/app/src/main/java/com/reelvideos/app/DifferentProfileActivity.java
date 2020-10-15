package com.reelvideos.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.ActivityDifferentProfileBinding;
import com.reelvideos.app.profile.PersonsMainFragment;
import com.reelvideos.app.profile.SavedVideosFragment;
import com.reelvideos.app.profile.UserVideosFragment;
import com.reelvideos.app.utils.AppExtensions;
//import com.reelvideos.app.utils.DialogCreator;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.util.Objects;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_FOLLOW_USER;
import static com.reelvideos.app.config.Constants.API_GET_USER;
import static com.reelvideos.app.config.Constants.API_UNFOLLOW_USER;
import static com.reelvideos.app.config.Constants.followMap;
import static com.reelvideos.app.utils.AppExtensions.showToast;

public class DifferentProfileActivity extends BaseActivity {

    ActivityDifferentProfileBinding binding;
    String userID;
    String videoCreatorImg;
    boolean isFollowing = false;
    String videoCreatorName;
    int followers;
    static String instaid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDifferentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        initComponents();
    }

    @Override
    public void initComponents() {

        Intent i = getIntent();
        if (i != null) {
            videoCreatorImg = i.getStringExtra(getString(R.string.IMG));
            videoCreatorName = i.getStringExtra(getString(R.string.NAME));
            isFollowing = i.getBooleanExtra(getString(R.string.isFollowed), false);
            userID = i.getStringExtra(getString(R.string.ID));
        }

        binding.pager.setOffscreenPageLimit(2);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        binding.pager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.pager);
        setupTabIcons();
        binding.backBtn.setOnClickListener(view -> onBackPressed());
        binding.shareBtn.setOnClickListener(view -> AppExtensions.shareProfileLink(getApplicationContext(), userID));


        if (isFollowing) {
            binding.followBtn.setText(getString(R.string.following));
            binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Secondary, null));
            //binding.followBtn.setEnabled(false);
        } else {
            //binding.followBtn.setEnabled(true);
            binding.followBtn.setText(getString(R.string.follow));
            binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Primary, null));
        }

        binding.usernameTop.setText(videoCreatorName);


        binding.followingLayout.setOnClickListener(view -> openFollowersCount(false));
        binding.fansLayout.setOnClickListener(view -> openFollowersCount(true));

//        if (videoCreatorImg.length() > 4)
        Picasso.get().
                load(videoCreatorImg).centerCrop()
                .placeholder(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.default_pic)))
                .resize(100, 100).into(binding.userImage);


        loadFromAPI();

        binding.instaIdBtn.setOnClickListener(view -> openInstagramProfile(instaid));

        binding.followBtn.setOnClickListener(view -> processBtnCLick());

    }

    private void processBtnCLick() {

        if (GlobalVariables.hasUserLoggedIN()) {
            if (binding.followBtn.getText().equals(getString(R.string.follow))) {  //for follow
                binding.followBtn.setText(R.string.following);
                binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Secondary, null));
                followMap.put(userID, true);
                sendBroadcast(new Intent(getString(R.string.newFollowing)));
                binding.fanCountTxt.setText(String.valueOf(followers + 1));
                followers += 1;
                //DialogCreator.showProgressDialog(this);
                binding.loaderanimationfordifferentprofile.playAnimation();
                //Go for API Call
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> callAuthAPI(API_FOLLOW_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
                    @Override
                    public void onApiSuccessResult(JSONObject jsonObject) {
                        isFollowing = true;
                        binding.followBtn.setText(R.string.following);
                        binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Secondary, null));
                        loadFromAPI();
                    }
                    @Override
                    public void onApiFailureResult(Exception e) {
                        binding.loaderanimationfordifferentprofile.setVisibility(View.GONE);
                        binding.followBtn.setText(R.string.following);
                    }
                }));

            } else if (binding.followBtn.getText().equals(getString(R.string.following))) {    //for un-follow
                binding.followBtn.setText(R.string.follow);
                binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Primary, null));
                sendBroadcast(new Intent(getString(R.string.newFollowing)));
                followMap.put(userID, false);
                binding.fanCountTxt.setText(String.valueOf(followers - 1));
                followers -= 1;
//                DialogCreator.showProgressDialog(this);
                //Go for API Call
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        callAuthAPI(API_UNFOLLOW_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
                            @Override
                            public void onApiSuccessResult(JSONObject jsonObject) {
                                isFollowing = false;
                                binding.followBtn.setText(R.string.follow);
                                sendBroadcast(new Intent(getString(R.string.newFollowing)));
                                binding.followBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Primary, null));
                                //binding.followBtn.setEnabled(false);
//                                DialogCreator.cancelProgressDialog();
                                loadFromAPI();
                            }

                            @Override
                            public void onApiFailureResult(Exception e) {
                               // DialogCreator.cancelProgressDialog();
                                binding.loaderanimationfordifferentprofile.setVisibility(View.GONE);
                                binding.followBtn.setText(R.string.follow);
                            }
                        });
                    }
                });

            }

        } else {
            showToast(this, getString(R.string.login_conti));
            openLoginActivity();
        }
    }

    private void setupTabIcons() {

        View view1 = LayoutInflater.from(this).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bi_grid_fill_new, null));
        binding.tabs.getTabAt(0).setCustomView(view1);

        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_bi_grid_fill_new, null));
                        break;

//                    case 1:
//                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_ion_bookmark_new));
//                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_bi_grid_fill_grey_new));
                        break;
//                    case 1:
//                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_ion_bookmark_new_grey));
//                        break;
                }

                tab.setCustomView(v);
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
    }


    private void openInstagramProfile(String instaId) {
        String instaLink = "http://instagram.com/" + instaid + "/";

        Uri uri = Uri.parse(instaLink);
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideosFragment(true, false, userID);
                    break;
//                case 1:
//                    result = new SavedVideosFragment(userID);
//                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 1;
        }


        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
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

    private void loadFromAPI() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> callNonAuthAPI(API_GET_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                binding.usernameTop.setText(jsonObject.optString(getString(R.string.uname)));
                binding.videosCount.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_videos))));
                binding.followingTv.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_following))));
                binding.fanCountTxt.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_followers))));
                followers = Integer.parseInt(String.valueOf(jsonObject.optLong(getString(R.string.num_followers))));


//                        if (jsonObject.optString("cover").length() > 4)
//                            Picasso.get().load(jsonObject.optString("cover")).placeholder(R.drawable.cover).into(binding.coverImage);
                Picasso.get().
                        load(jsonObject.optString(getString(R.string.pic))).centerCrop()
                        .placeholder(ContextCompat.getDrawable(DifferentProfileActivity.this, R.drawable.default_pic))
                        .resize(250, 250).into(binding.userImage);

                binding.fullName.setText(StringEscapeUtils.unescapeJava(jsonObject.optString(getString(R.string.fname))));

                if (jsonObject.optString(getString(R.string.bioo)).length() > 0) {
                    binding.bio.setVisibility(View.VISIBLE);
                    binding.bio.setText(StringEscapeUtils.unescapeJava(jsonObject.optString(getString(R.string.bioo))));
                }
                if (jsonObject.optString(getString(R.string.insta_username)).length() > 0) {
                    binding.instaIdBtn.setVisibility(View.VISIBLE);
                    instaid = jsonObject.optString(getString(R.string.insta_username));
                } else {
                    instaid = "";
                }

                if (jsonObject.optBoolean(getString(R.string.is_verified))) {
                    binding.verificationBadge.setVisibility(View.VISIBLE);
                }
                isFollowing = jsonObject.optBoolean(getString(R.string.is_following));
            }

            @Override
            public void onApiFailureResult(Exception e) {

            }
        }));
    }


    public void goBackToPrev() {
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        finish();
    }

    private void openFollowersCount(boolean isFollowers) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PersonsMainFragment followersFragment = new PersonsMainFragment(userID, isFollowers);
        transaction.addToBackStack(null);

        transaction.replace(R.id.mainProfile, followersFragment).commit();

    }
}