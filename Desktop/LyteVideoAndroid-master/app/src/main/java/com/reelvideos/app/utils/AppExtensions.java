package com.reelvideos.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.reelvideos.app.R;

import java.io.File;
import java.util.Locale;

import static com.reelvideos.app.utils.Utils.createAppDirs;

public class AppExtensions {

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static String GetSuffix(long count) {
        try {

            if (count < 1000) return "" + count;
            int exp = (int) (Math.log(count) / Math.log(1000));
            return String.format(Locale.ENGLISH, "%.1f %c",
                    count / Math.pow(1000, exp),
                    "kMGTPE".charAt(exp - 1));
        } catch (Exception e) {
            return String.valueOf(count);
        }

    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean checkPermissions(Context context) {

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            };

            if (!hasPermissions(context, PERMISSIONS)) {
                ((AppCompatActivity) context).requestPermissions(PERMISSIONS, 450);
            } else {
                createAppDirs();
                return true;
            }
            return false;
        } else {
            createAppDirs();
            return true;
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void deleteCache(Context context) {

        try {
            File dir = context.getCacheDir();
            deleteDirectoryTree(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static void shareApp(final Activity activity, final String message) {
        new Thread(() -> {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)));

        }).start();
    }

    public static void shareProfileLink(Context context, String profileID) {

        String profileUrl = "https://lytevideo.com/profile/" + profileID + "/";
        String msg = "Check out this profile on Lyte Video!\n" +
                "I have many entertaining video that might grab your attention !\n" +
                "\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47\n\n";
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg + profileUrl);
        context.startActivity(Intent.createChooser(shareIntent, "Share Profile Using"));

    }

}
