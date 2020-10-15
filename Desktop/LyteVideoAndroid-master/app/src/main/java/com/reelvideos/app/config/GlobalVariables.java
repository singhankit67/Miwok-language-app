package com.reelvideos.app.config;

import android.content.SharedPreferences;

import com.reelvideos.app.base.CoreApp;

import org.json.JSONObject;

import static com.reelvideos.app.config.Constants.PREF_FOLLOWERS_COUNT;
import static com.reelvideos.app.config.Constants.PREF_FOLLOWING_COUNT;
import static com.reelvideos.app.config.Constants.PREF_FULL_NAME;
import static com.reelvideos.app.config.Constants.PREF_TOKEN;
import static com.reelvideos.app.config.Constants.PREF_USER_BIO;
import static com.reelvideos.app.config.Constants.PREF_INSTA_ID;
import static com.reelvideos.app.config.Constants.PREF_USER_COVER;
import static com.reelvideos.app.config.Constants.PREF_USER_ID;
import static com.reelvideos.app.config.Constants.PREF_USER_LOGGED;
import static com.reelvideos.app.config.Constants.PREF_USER_NAME;
import static com.reelvideos.app.config.Constants.PREF_USER_PIC;
import static com.reelvideos.app.config.Constants.PREF_USER_REGION;
import static com.reelvideos.app.config.Constants.PREF_USER_SAW_INTRO_PAGE;
import static com.reelvideos.app.config.Constants.PREF_USER_VERIFIED;
import static com.reelvideos.app.config.Constants.PREF_VIDEOS_COUNT;
import static com.reelvideos.app.config.Constants.PREF_USER_GENDER;

public class GlobalVariables {

    public static SharedPreferences sharedPreferences = CoreApp.getInstance().getSharedPreferences();
    public static boolean hasUserLoggedIN(){
        return sharedPreferences.getBoolean(PREF_USER_LOGGED, false);
    }

    public static boolean hasUserSawIntroPage(){
        return sharedPreferences.getBoolean(PREF_USER_SAW_INTRO_PAGE, false);
    }

    /*private boolean hasUserSawIntroPage;

    public boolean isHasUserSawIntroPage() {
        return hasUserSawIntroPage;
    }

    public void setHasUserSawIntroPage(boolean hasUserSawIntroPage) {
        this.hasUserSawIntroPage = hasUserSawIntroPage;
    }*/

    public static String getAuthKey(){
        return sharedPreferences.getString(PREF_TOKEN, "NA");
    }

    public static void saveUserData(JSONObject jsonObject){
        sharedPreferences.edit().putString(PREF_TOKEN, jsonObject.optString("token"))
                .putString(PREF_USER_NAME, jsonObject.optString("username"))
                .putString(PREF_USER_ID, String.valueOf(jsonObject.optLong("id")))
                .putString(PREF_FULL_NAME, jsonObject.optString("fullname"))
                .putLong(PREF_FOLLOWERS_COUNT, jsonObject.optLong("num_followers"))
                .putLong(PREF_FOLLOWING_COUNT, jsonObject.optLong("num_following"))
                .putLong(PREF_VIDEOS_COUNT, jsonObject.optLong("num_videos"))
                .putString(PREF_USER_PIC, jsonObject.optString("pic"))
                .putString(PREF_INSTA_ID, jsonObject.optString("instagram_username"))
                .putString(PREF_USER_BIO, jsonObject.optString("bio"))
                .putString(PREF_USER_GENDER, jsonObject.optString("gender"))
                .putString(PREF_USER_REGION, jsonObject.optString("region"))
                .putLong(PREF_USER_COVER, jsonObject.optLong("cover"))
                .putString (PREF_USER_VERIFIED, jsonObject.optString ( "is_verified" ))
                .putBoolean(PREF_USER_LOGGED, true)
                .apply();
    }

    public static void updateUserData(JSONObject jsonObject){
        sharedPreferences.edit()
                .putString(PREF_FULL_NAME, jsonObject.optString("fullname"))
                .putLong(PREF_FOLLOWERS_COUNT, jsonObject.optLong("num_followers"))
                .putLong(PREF_FOLLOWING_COUNT, jsonObject.optLong("num_following"))
                .putLong(PREF_VIDEOS_COUNT, jsonObject.optLong("num_videos"))
                .putString(PREF_USER_PIC, jsonObject.optString("pic"))
                .putString(PREF_USER_BIO, jsonObject.optString("bio"))
                .putString(PREF_INSTA_ID, jsonObject.optString("instagram_username"))
                .putString(PREF_USER_GENDER, jsonObject.optString("gender"))
                .putString(PREF_INSTA_ID, jsonObject.optString("instagram_username"))
                .putString(PREF_USER_REGION, jsonObject.optString("region"))
                .putString(PREF_USER_COVER, jsonObject.optString("cover"))
                .putBoolean (PREF_USER_VERIFIED, jsonObject.optBoolean ( "is_verified" ))
                .putBoolean(PREF_USER_LOGGED, true)
                .apply();
    }

    public static void removeAllPrefs(){
        sharedPreferences.edit().clear().apply();
    }

    public static  String getUserName(){
        return sharedPreferences.getString(PREF_USER_NAME, "NA");
    }

    public static  String getUserPic(){
        return sharedPreferences.getString(PREF_USER_PIC, "NA");
    }

    public static  String getUserId(){
        return sharedPreferences.getString(PREF_USER_ID, "NA");
    }

    public static  String getInstaId(){
        return sharedPreferences.getString(PREF_INSTA_ID, "NA");
    }

    public static boolean getIsVerified(){ return sharedPreferences.getBoolean (PREF_USER_VERIFIED, false); }


    public static String getPrefsLong(String pref_name){

       return String.valueOf(sharedPreferences.getLong(pref_name, 0));

    }

    public static String getPrefsString(String pref_name){

        return sharedPreferences.getString(pref_name, "");

    }

}
