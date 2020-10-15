package com.reelvideos.app.config;

import android.os.Environment;

import com.daasuu.gpuv.egl.filter.GlFilter;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static GlFilter VIDEO_GL_FILTER = new GlFilter();

    //URLS
    public static final String BASE_URL = "https://api.lolipop.live/api/v1/";
    public static final String API_HOME_VIDEOS = BASE_URL + "videos/?type=TRENDING&limit=20";
    public static final String API_LOGIN = BASE_URL + "auth/login/";
    public static final String API_OTP_VERIFY = BASE_URL + "auth/verify-otp/";
    public static final String API_POST_COMMENT = BASE_URL + "comments/";
    public static final String API_POST_VIDEO = BASE_URL + "videos/";
    public static final String API_FETCH_TAGS = BASE_URL + "hashtags/?interest=true";
    //for user profile video it loads 20 on every call
    public static final String API_FETCH_USER_VIDEOS = BASE_URL + "videos/?type=USER&user=%id%&limit=20&ordering=-id";
    public static final String API_FOLLOW_USER = BASE_URL + "auth/users/%id%/follow/";
    public static final String API_UNFOLLOW_USER = BASE_URL + "auth/users/%id%/unfollow/";
    public static final String API_GET_USER = BASE_URL + "auth/users/%id%/";
    //for followers it loads 20 on every call
    public static final String API_GET_FOLLOWERS = BASE_URL + "auth/users/?type=FOLLOWERS&user=%id%&limit=20";
    //for following it loads 20 on every call
    public static final String API_GET_FOLLOWING = BASE_URL + "auth/users/?type=FOLLOWING&user=%id%&limit=20";
    public static final String API_GET_TAGS = BASE_URL + "hashtags/popular/";
    public static final String API_GET_TOP_CREATOR = BASE_URL + "auth/users/?type=TOP_CREATORS";
    //for hashTags data it loads 20 on every call
    public static final String API_GET_VIDEO_BY_HASH_TAG = BASE_URL + "videos/?type=HASHTAG&limit=15&hashtag=%tag%&ordering=-id";
    public static final String API_UPDATE_PROFILE = BASE_URL + "auth/user/update/";
    public static final String API_DELETE_VIDEO = BASE_URL + "videos/%video_id%/delete/";
    public static final String API_VIEW_VIDEO = BASE_URL + "videos/%video_id%/view/";
    public static final String API_REPORT_VIDEO = BASE_URL + "videos/%video_id%/report/";
    public static final String API_SAVE_VIDEO = BASE_URL + "videos/%video_id%/save/";
    //for saved video, it loads 20 on every call
    public static final String API_FETCH_SAVED_VIDEOS = BASE_URL + "videos/?type=SAVED&user=%id%&limit=20&ordering=-id";
    //search api
    public static final String API_SEARCH_VIDEOS = BASE_URL + "videos/?search=%query%&limit=20&ordering=-id";
    public static final String API_SEARCH_HASHTAGS = BASE_URL + "hashtags/?search=%query%&limit=20";
    public static final String API_SEARCH_ACCOUNTS = BASE_URL + "auth/users/?search=%query%&limit=20";
    //music api
    public static final String API_SONG_CATEGORIES = BASE_URL + "musics/categories/";
    public static final String API_CATEGORY_WISE_SONGS = BASE_URL + "musics/?category=";
    public static final String API_SEARCH_SONGS = BASE_URL + "musics/?category=%query%&search=";



    public static String getLikeVideoAPI(String videoID) {
        return BASE_URL + "videos/" + videoID + "/like/";
    }

    public static String getDislikeVideoAPI(String videoID) {
        return BASE_URL + "videos/" + videoID + "/unlike/";
    }

    public static String getShareVideoAPI(String videoID) {
        return BASE_URL + "videos/" + videoID + "/share/";
    }

    public static String getSharedVideo(String videoID) {
        return BASE_URL + "videos/" + videoID + "/";
    }

    public static String getDownloadVideoAPI(String videoID) {
        return BASE_URL + "videos/" + videoID + "/download/";
    }

    public static String getCommentsAPI(String videoID) {
        return BASE_URL + "comments/?video=" + videoID + "&ordering=-id";
    }


    //PREFS NAME

    public static final String PREF_USER_SAW_INTRO_PAGE = "SAW_INTRO";
    public static final String PREF_USER_LOGGED = "LOGGED_USER";
    public static final String PREF_TOKEN = "RANDOM_N";
    public static final String PREF_USER_NAME = "USERNAME";
    public static final String PREF_USER_ID = "USER_ID";
    public static final String PREF_FULL_NAME = "FULL_NAME";
    public static final String PREF_FOLLOWERS_COUNT = "FOLLOWERS";
    public static final String PREF_FOLLOWING_COUNT = "FOLLOWING";
    public static final String PREF_THUMBNAIL_UPLOAD = "UPLOAD_THUMB";
    public static final String PREF_VIDEOS_COUNT = "VIDEO_COUNT";
    public static final String PREF_USER_PIC = "USER_PIC";
    public static final String PREF_USER_COVER = "USER_COVER";
    public static final String PREF_INSTA_ID = "INSTA_ID";
    public static final String PREF_USER_BIO = "USER_BIO";
    public static final String PREF_LOCALE_NAME = "USER_LOCALE";
    public static final String PREF_USER_GENDER = "USER_GENDER";
    public static final String PREF_USER_REGION = "USER_REGION";
    public static final String PREF_USER_VERIFIED = "USER_VERIFIED";


    //FILE HANDLING
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().toString();
    public static final String APP_HIDDEN_DIR = ROOT_DIR + "/.LytVideo/";
    public static final String APP_SHOWING_DIR = ROOT_DIR + "/LytVideo/";
    public static final File APP_DIR_FILE = new File(APP_SHOWING_DIR);
    public static final File APP_HIDDEN_FILE = new File(APP_HIDDEN_DIR);


    //VIDEO DEFAULTS
    public static final int MAXIMUM_RECORDING_DURATION = 60000;
    public static final int RECORDING_DURATION = 60000;
    public static final int MINIMUM_RECORDING_DURATION = 5000;


    //TEMP OPERATIONS
    public static final String GALLERY_RESIZED_VIDEO = APP_HIDDEN_DIR + "gallery_resize_video.mp4";
    public static String GALLERY_TRIMMED_VIDEO = APP_HIDDEN_DIR + "gallery_trimmed_video.mp4";
    public static String OUTPUT_FILE_NO_ADDON = APP_HIDDEN_DIR + "output2.mp4";
    public static String OUTPUT_FILE_PROCESSED = APP_HIDDEN_DIR + "output3_upload.mp4";
    public static String OUTPUT_FILE_PROCESSED_FINAL = APP_HIDDEN_DIR + "outputfinal_upload.mp4";
    public static String VIDEOFILENAMEGLOBAL = APP_HIDDEN_DIR + "videofilename.mp4";
    public static String OUTPUT_FILE_FINAL = APP_HIDDEN_DIR + "output_final.mp4";
    public static String OUTPUT_FILE_POSTER = APP_HIDDEN_DIR + "final_send_poster.mp4";

    //EXTRA
    public static final String[] INDIAN_STATES = new String[]{"Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chhattisgarh",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jammu and Kashmir",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttarakhand",
            "Uttar Pradesh",
            "West Bengal",
            "Andaman and Nicobar Islands",
            "Chandigarh",
            "Dadra and Nagar Haveli",
            "Daman and Diu",
            "Delhi",
            "Lakshadweep",
            "Puducherry"};

    public static Map<String, Boolean> followMap = new HashMap<>();

}