package com.reelvideos.app.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.reelvideos.app.R;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static android.content.ContentValues.TAG;

public class CoreApp extends Application {

    static  {
        System.loadLibrary("trinity");
        System.loadLibrary("c++_shared");
        System.loadLibrary("marsxlog");
    }

    private static CoreApp mInstance;
    private SharedPreferences sharedPreferences;
    private Activity activity;
    public RequestQueue queue;
    public static CoreApp getInstance() {
        return mInstance;
    }
    public Activity getActivity() {
        return activity;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    //private HttpProxyCacheServer proxy;
    public static SimpleCache simpleCache = null;
    public static LeastRecentlyUsedCacheEvictor leastRecentlyUsedCacheEvictor = null;
    public static ExoDatabaseProvider exoDatabaseProvider = null;
    public static Long exoPlayerCacheSize = (long) (90 * 1024 * 1024);

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);

        String filterLocalDir = getExternalCacheDir().getAbsolutePath() + "/filter/";
        File file = new File(filterLocalDir);
        if (!file.exists()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copyAssets("filter", filterLocalDir);
                }
            }).start();
        }

        String effectLocalDir = getExternalCacheDir().getAbsolutePath() + "/effect/";
        File effectDir = new File(effectLocalDir);
        if (!effectDir.exists()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copyAssets("effect", effectLocalDir);
                }
            }).start();
        }

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = new LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize);
        }

        if (exoDatabaseProvider != null) {
            exoDatabaseProvider = new ExoDatabaseProvider(this);
        }

        if (simpleCache == null) {
            simpleCache = new SimpleCache(getCacheDir(), leastRecentlyUsedCacheEvictor, exoDatabaseProvider);
            if (simpleCache.getCacheSpace() >= 400207768) {
                freeMemory();
            }
            Log.i(TAG, "onCreate: " + simpleCache.getCacheSpace());
        }
    }

    private void copyAssets(String assetDir, String targetDir) {
        if (TextUtils.isEmpty(assetDir) || TextUtils.isEmpty(targetDir)) {
            return;
        }
        String separator = File.separator;
        try {
            String[] fileNames = getAssets().list(assetDir);
            if (fileNames == null){
                return;
            }
            if (fileNames.length != 0){
                File targetFile = new File(targetDir);
                if (!targetFile.exists() && !targetFile.mkdirs()) {
                    return;
                }
                for (String fileName : fileNames) {
                    copyAssets(assetDir + separator + fileName, targetDir + separator + fileName);
                }
            }else {
                copy(assetDir, targetDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copy(String source, String targetPath) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(targetPath)) {
            return;
        }
        File dest = new File(targetPath);
        dest.getParentFile().mkdirs();
        try {
            BufferedInputStream inputStream = new BufferedInputStream(getAssets().open(source));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[2048];
            int length;
            while (true){
                length = inputStream.read(buffer);
                if (length < 0) {
                    break;
                }
                out.write(buffer, 0, length);
            }
            out.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static HttpProxyCacheServer getProxy(Context context) {
//        CoreApp app = (CoreApp) context.getApplicationContext();
//        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
//    }
//
//    private HttpProxyCacheServer newProxy() {
//        return new HttpProxyCacheServer.Builder(this)
//                .maxCacheSize(1024 * 1024 * 1024)
//                .maxCacheFilesCount(20)
//                .build();
//    }


    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }


    public void savePreferenceDataString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }




    /**
     * Call when application is close
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mInstance != null) {
            mInstance = null;
        }
    }

    public void freeMemory() {

        try {
            File dir = getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }



}