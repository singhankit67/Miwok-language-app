package com.reelvideos.app.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.googlecode.mp4parser.authoring.Track;
import com.reelvideos.app.R;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Random;

import static android.content.Intent.EXTRA_CHOSEN_COMPONENT;
import static com.reelvideos.app.api.CommonClassForAPI.callPostVideoDurationAPI;
import static com.reelvideos.app.config.Constants.APP_DIR_FILE;
import static com.reelvideos.app.config.Constants.APP_HIDDEN_FILE;
import static com.reelvideos.app.config.Constants.APP_SHOWING_DIR;
import static com.reelvideos.app.config.Constants.OUTPUT_FILE_POSTER;
import static com.reelvideos.app.utils.AppExtensions.showToast;

public class Utils {
    public static boolean selected_music_is_local = false;
    static Intent receiver;
    static PendingIntent pendingIntent;

    public static class SharedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                String selectedAppPackage = String.valueOf(intent.getExtras().get(EXTRA_CHOSEN_COMPONENT));
                if (selectedAppPackage.length() > 0)
                    context.sendBroadcast(new Intent("confirmShared"));
            }
        }
    }

    public static void shareVideo(Context context, String downloadPath, String videoID) {

        receiver = new Intent(context, SharedReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);

        DialogCreator.showProgressDialog(context);
        String finalFileName = APP_SHOWING_DIR + "SHARED_" + videoID + ".mp4";

        if (new File(finalFileName).exists()) {
            DialogCreator.cancelProgressDialog();
            String msg = "WOW! I found a great video. Tap to view it!\n" +
                    "Click this link to explore more cool videos!\n" +
                    "\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47\n\n";
            shareVideo(context, finalFileName, videoID, msg, pendingIntent);
            scanFile(context, finalFileName);
            return;
        }

        ThinDownloadManager downloadManager = new ThinDownloadManager();
        Uri downloadUri = Uri.parse(downloadPath);
        Uri destinationUri = Uri.parse(finalFileName);

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadContext(CoreApp.getInstance().getApplicationContext()); //Optional


        downloadRequest.setStatusListener(new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                String msg = "WOW! I found a great video. Tap to view it!\n" +
                        "Click this link to explore more cool videos!\n" +
                        "\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47\n\n";
                DialogCreator.cancelProgressDialog();
                shareVideo(context, finalFileName, videoID, msg, pendingIntent);
                scanFile(context, finalFileName);

            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                DialogCreator.cancelProgressDialog();
                showToast(context, context.getString(R.string.down_fail) + errorMessage);
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
               // DialogCreator.updateStatus(context.getString(R.string.downloading, progress + "%"));
            }
        });

        downloadManager.add(downloadRequest);


    }

    public static void downloadVideo(Context context, String downloadPath, String videoID) {

        DialogCreator.showProgressDialog(context);
        String finalFileName = APP_SHOWING_DIR + "SHARED_" + videoID + ".mp4";

        if (new File(finalFileName).exists()) {
            DialogCreator.cancelProgressDialog();
            showToast(context, context.getString(R.string.downloaded, APP_SHOWING_DIR));
            scanFile(context, finalFileName);
            return;
        }

        ThinDownloadManager downloadManager = new ThinDownloadManager();
        Uri downloadUri = Uri.parse(downloadPath);
        Uri destinationUri = Uri.parse(finalFileName);

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadContext(CoreApp.getInstance().getApplicationContext()); //Optional


        downloadRequest.setStatusListener(new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                DialogCreator.cancelProgressDialog();
                Toast.makeText(context, finalFileName, Toast.LENGTH_SHORT).show();
                scanFile(context, finalFileName);
                showToast(context, context.getString(R.string.downloaded, APP_SHOWING_DIR));
            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                DialogCreator.cancelProgressDialog();
                showToast(context, context.getString(R.string.down_fail) + errorMessage);
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
               // DialogCreator.updateStatus(context.getString(R.string.downloading, progress + "%"));
            }
        });

        downloadManager.add(downloadRequest);


    }

    /*private Bitmap addWaterMark(Bitmap src) {


        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);


        Bitmap waterMark = BitmapFactory.decodeResource( Resources.getSystem (), R.layout.watermark);

        //  canvas.drawBitmap(waterMark, 0, 0, null);
        int startX= (canvas.getWidth()-waterMark.getWidth())/2;//for horisontal position
        int startY=(canvas.getHeight()-waterMark.getHeight())/2;//for vertical position
        canvas.drawBitmap(waterMark,startX,startY,null);

        return result;

    }*/


    //shubham_keshri
    // this function is calculating total time of particular video watched, and using data from ML.
    public static void callApiForUploadVideoWatchedTime(String videoId, long VideoStartTime, long VideoEndTime, long videoPausedFor, long PauseStartTime) {

        long durationOfVideoPlayed;

        if (PauseStartTime != 0) {

            durationOfVideoPlayed = Math.abs(PauseStartTime - VideoStartTime) / 1000;

        } else {
            durationOfVideoPlayed = Math.abs(VideoEndTime - VideoStartTime) / 1000;

        }


        long TotalSecondWatched = Math.abs(durationOfVideoPlayed - videoPausedFor);

        callPostVideoDurationAPI(videoId, String.valueOf(TotalSecondWatched), new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {


            }

            @Override
            public void onApiFailureResult(Exception e) {


            }
        });


        durationOfVideoPlayed = 0;
        TotalSecondWatched = 0;

    }


    public static void scanFile(Context context, String filename) {
        MediaScannerConnection.scanFile(context,
                new String[]{filename},
                null,
                (path, uri) -> Log.i("ExternalStorage", "Scanned " + path));
    }

    public static void shareVideo(Context context, String filename, String videoId, String message, PendingIntent pendingIntent) {

        String videoUrl = "https://lytevideo.com/videos/" + videoId + "/";

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        final File file = new File(filename);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message + videoUrl);
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileprovider", file));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.startActivity(Intent.createChooser(shareIntent, "Share Video Using", pendingIntent.getIntentSender()));
        } else {
            context.startActivity(Intent.createChooser(shareIntent, "Share Video Using"));
        }

    }

//    public static void shareVideoLink(Context context, String videoId){
//
//        String videoUrl = "https://lytevideo.com/videos/"+videoId+"/";
//        String msg = "WOW! I found a great video. Tap to view it!\n" +
//                "Click this link to explore more cool videos!\n" +
//                "\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47\n\n";
//        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, msg + videoUrl);
//        context.startActivity(Intent.createChooser(shareIntent, "Share Video Using"));
//
//    }

    public static void createAppDirs() {

        if (!APP_DIR_FILE.exists()) {
            APP_DIR_FILE.mkdir();
        }

        if (!APP_HIDDEN_FILE.exists()) {
            APP_HIDDEN_FILE.mkdir();
        }

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    public static byte[] getFileDataFromUri(Context context, Uri uri) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            InputStream iStream = context.getContentResolver().openInputStream(uri);
            int bufferSize = 2048;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            if (iStream != null) {
                while ((len = iStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return byteArrayOutputStream.toByteArray();
    }

    // EDIT PROFILE IMAGE
    public static byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String bitmapToBase64(Activity activity, Bitmap imagebitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] byteArray = baos.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    public static Bitmap base64ToBitmap(String base_64) {
        Bitmap decodedByte = null;
        try {

            byte[] decodedString = Base64.decode(base_64, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {

        }
        return decodedByte;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    public static File bitmapToFile(Context context, Bitmap bitmap) {
        //create a file to write bitmap data
        File file = null;
        try {
            file = new File(OUTPUT_FILE_POSTER);
            file.createNewFile();


            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // compressing the poster
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();


            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file; // it will return null
        }
    }

    public static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

}