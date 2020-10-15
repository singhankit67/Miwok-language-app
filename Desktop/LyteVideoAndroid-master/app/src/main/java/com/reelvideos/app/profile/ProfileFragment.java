package com.reelvideos.app.profile;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.reelvideos.app.LoginActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.SettingsActivity;
import com.reelvideos.app.base.BaseFragment;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.databinding.FragmentProfileBinding;
import com.reelvideos.app.utils.AppExtensions;
//import com.reelvideos.app.utils.DialogCreator;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.config.Constants.API_FOLLOW_USER;
import static com.reelvideos.app.config.Constants.API_GET_USER;
import static com.reelvideos.app.config.Constants.API_UNFOLLOW_USER;
import static com.reelvideos.app.config.Constants.PREF_VIDEOS_COUNT;
import static com.reelvideos.app.config.Constants.followMap;
import static com.reelvideos.app.config.GlobalVariables.updateUserData;
import static com.reelvideos.app.utils.AppExtensions.showToast;


public class ProfileFragment extends BaseFragment {


    FragmentProfileBinding binding;
    boolean isOwnProfile = true;
    String userID = GlobalVariables.getUserId();
    String videoCreatorName = GlobalVariables.getUserName();
    String videoCreatorImg = GlobalVariables.getUserName();
    NewStatsBroadCast mReceiver;
    int followers;
    static String instaid;
    boolean isFollowed = false;

    public ProfileFragment() {

    }
    private class NewStatsBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updatePrefsData();
            loadFromAPI();
        }
    }

    public ProfileFragment(String userID, String videoCreatorName, String creatorImg, boolean isFollowed) {
        this.userID = userID;
        this.videoCreatorName = videoCreatorName;
        this.videoCreatorImg = creatorImg;
        this.isFollowed = isFollowed;
        isOwnProfile = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        removeTransparent();
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        initComponents();
        return binding.getRoot();
    }

    private void updatePrefsData() {
        userID = GlobalVariables.getUserId();
        //instaid=GlobalVariables.getInstaId ();
        videoCreatorName = GlobalVariables.getUserName();
        videoCreatorImg = GlobalVariables.getUserName();
    }

    @Override
    public void initComponents() {
        mReceiver = new NewStatsBroadCast();
        activity.registerReceiver(mReceiver, new IntentFilter("newVideo"));
        binding.pager.setOffscreenPageLimit(2);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        binding.pager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.pager);
        setupTabIcons();
        binding.settingsBtn.setOnClickListener(view -> startActivity(new Intent(activity, SettingsActivity.class)));
        binding.backBtn.setOnClickListener(view -> activity.onBackPressed());

        binding.shareBtn.setOnClickListener(view -> AppExtensions.shareProfileLink(activity, userID));
        binding.shareBtnRight.setOnClickListener(view -> AppExtensions.shareProfileLink(activity, userID));

        loadUpDataProfile();

        binding.followingLayout.setOnClickListener(view -> openFollowersCount(false));
        binding.fansLayout.setOnClickListener(view -> openFollowersCount(true));

        if (videoCreatorImg.length() > 4)
            Picasso.get().
                    load(videoCreatorImg).centerCrop()
                    .placeholder(ContextCompat.getDrawable(activity, R.drawable.default_pic))
                    .resize(100, 100).into(binding.userImage);

        loadFromAPI();

        binding.profileBtn.setOnClickListener(view -> processBtnCLick());

        binding.instaIdBtn.setOnClickListener(view -> openInstagramProfile(instaid));

        if (!isOwnProfile) {
            binding.backBtn.setVisibility(View.VISIBLE);
            binding.shareBtn.setVisibility(View.GONE);
            binding.shareBtnRight.setVisibility(View.VISIBLE);
            binding.profileBtn.setText(isFollowed ? R.string.following : R.string.follow);
//            binding.profileBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Primary, null));
            //binding.profileBtn.setEnabled(!isFollowed);
            binding.settingsBtn.setVisibility(View.GONE);
        }
    }

    private void processBtnCLick() {

        if (isOwnProfile) {
            //Edit Profile Options
            openEditProfile();

        } else {

            //Follow
            if (GlobalVariables.hasUserLoggedIN()) {
                if (binding.profileBtn.getText().equals("Follow")) {  //for follow
                    followMap.put(userID, true);
                    activity.sendBroadcast(new Intent("newFollowing"));
                    binding.fanCountTxt.setText(String.valueOf(followers + 1));
                    binding.profileBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Secondary, null));
                    followers += 1;

                    //DialogCreator.showProgressDialog(activity);
                    //Go for API Call
                    callAuthAPI(API_FOLLOW_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {
                            isFollowed = true;
                            binding.profileBtn.setText(R.string.following);
                            //binding.profileBtn.setEnabled(false);
                            loadFromAPI();
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                        }

                        @Override
                        public void onApiFailureResult(Exception e) {
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                            binding.profileBtn.setText(R.string.following);
                            //binding.profileBtn.setEnabled(false);
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                        }
                    });
                } else if (binding.profileBtn.getText().equals("Following")) {    //for un-follow
                    activity.sendBroadcast(new Intent("newFollowing"));
                    followMap.put(userID, false);
                    binding.fanCountTxt.setText(String.valueOf(followers - 1));
                    followers -= 1;
                    binding.profileBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.Primary, null));

                    binding.loaderanimationforprofilefragment.playAnimation();
                    //Go for API Call
                    callAuthAPI(API_UNFOLLOW_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
                        @Override
                        public void onApiSuccessResult(JSONObject jsonObject) {
                            isFollowed = false;
                            binding.profileBtn.setText(R.string.follow);
                            //binding.profileBtn.setEnabled(false);
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                            loadFromAPI();
                        }
                        @Override
                        public void onApiFailureResult(Exception e) {
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                            binding.profileBtn.setText(R.string.follow);
                            //binding.profileBtn.setEnabled(false);
                            //DialogCreator.cancelProgressDialog();
                            binding.loaderanimationforprofilefragment.setVisibility(View.GONE);
                        }
                    });
                }
            } else {
                showToast(activity, getString(R.string.login_conti));
                openLoginActivity();
            }
        }
    }

    private void openEditProfile() {
        //Other Profile
        FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_from_bottom, R.anim.out_to_top, R.anim.slide_from_top, R.anim.out_from_bottom);
        EditProfileFragment editProfileFragment = new EditProfileFragment();
        transaction.addToBackStack(null);
        transaction.replace(R.id.mainMenu, editProfileFragment).commit();

    }
    private void openLoginActivity() {
        Intent intent = new Intent(activity, LoginActivity.class);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_bottom, R.anim.out_to_top);
    }
    private void setupTabIcons() {
        View view1 = LayoutInflater.from(activity).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bi_grid_fill_grey_new, null));
        binding.tabs.getTabAt(0).setCustomView(view1);

        if (isOwnProfile) {
            View view2 = LayoutInflater.from(activity).inflate(R.layout.item_tabs_profile_menu, null);
            ImageView imageView2 = view2.findViewById(R.id.image);
            imageView2.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_ion_bookmark_new, null));
            binding.tabs.getTabAt(1).setCustomView(view2);
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bi_grid_fill_grey_new, null));
                        break;
                    case 1:
                        if (isOwnProfile)
                            image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_ion_bookmark_new_grey, null));
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
                        image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bi_grid_fill_new, null));
                        break;

                    case 1:
                        if (isOwnProfile)
                            image.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_ion_bookmark_new, null));

                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


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
                    result = new UserVideosFragment(false, isOwnProfile, userID);
                    break;
                case 1:
                    result = new SavedVideosFragment(userID);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            if (isOwnProfile)
                return 2;
            else
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


    private void loadUpDataProfile() {

        if (isOwnProfile) {
            binding.videosCount.setText(GlobalVariables.getPrefsLong(PREF_VIDEOS_COUNT));
        }


        binding.usernameTop.setText(videoCreatorName);
        callAuthAPI(API_GET_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                binding.followingTv.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_following))));
                binding.fanCountTxt.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_followers))));
                isFollowed = jsonObject.optBoolean(getString(R.string.is_following));
            }

            @Override
            public void onApiFailureResult(Exception e) {

            }
        });

    }


    private void loadFromAPI() {

        callNonAuthAPI(API_GET_USER.replace("%id%", userID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                binding.usernameTop.setText(String.format("%s", jsonObject.optString(getString(R.string.username).toLowerCase())));
                binding.videosCount.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_videos))));
                binding.followingTv.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_following))));
                binding.fanCountTxt.setText(String.valueOf(jsonObject.optLong(getString(R.string.num_followers))));
                followers = Integer.parseInt(String.valueOf(jsonObject.optLong(getString(R.string.num_followers))));


                if (jsonObject.optString(getString(R.string.cover)).length() > 4)
                    Picasso.get().load(jsonObject.optString(getString(R.string.cover))).placeholder(R.drawable.cover).into(binding.coverImage);
                Picasso.get().
                        load(jsonObject.optString(getString(R.string.pic))).centerCrop()
                        .placeholder(ContextCompat.getDrawable(activity, R.drawable.default_pic))
                        .resize(250, 250).into(binding.userImage);


                //shubham_keshri
                if (jsonObject.optString(getString(R.string.insta_username)).length() > 0) {
                    binding.instaIdBtn.setVisibility(View.VISIBLE);
                    instaid = jsonObject.optString(getString(R.string.insta_username));
                } else {
                    instaid = "";
                }

                if (jsonObject.optString(getString(R.string.bioo)).length ()>0){
                    binding.bio.setVisibility ( View.VISIBLE );
                    binding.bio.setText(StringEscapeUtils.unescapeJava(jsonObject.optString(getString(R.string.bioo))));
                }
                if (jsonObject.optBoolean(getString(R.string.is_verified))) {
                    binding.verificationBadge.setVisibility(View.VISIBLE);
                }
                if (jsonObject.optString(getString(R.string.fullname).toLowerCase()).trim().length() > 0) {
                    binding.fullName.setVisibility(View.VISIBLE);
                    binding.fullName.setText(StringEscapeUtils.unescapeJava(jsonObject.optString(getString(R.string.fullname).toLowerCase())));
                }
                if (isOwnProfile) updateUserData(jsonObject);
            }
            @Override
            public void onApiFailureResult(Exception e) {

            }
        });
    }

    private void openFollowersCount(boolean isFollowers) {
        FragmentTransaction transaction = ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction();
        PersonsMainFragment followersFragment = new PersonsMainFragment(userID, isFollowers);
        transaction.addToBackStack(null);
        transaction.replace(R.id.mainMenu, followersFragment).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            activity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
    public void removeTransparent() {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    }

}