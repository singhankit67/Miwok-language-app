package com.reelvideos.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFmpegExecution;
import com.daasuu.gpuv.composer.FillMode;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.reelvideos.app.MainActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.callbacks.ServiceCallback;
import com.reelvideos.app.config.Constants;
import com.reelvideos.app.utils.DialogCreator;
import com.reelvideos.app.utils.Utils;

import java.io.File;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.reelvideos.app.config.Constants.OUTPUT_FILE_POSTER;
import static com.reelvideos.app.config.Constants.OUTPUT_FILE_PROCESSED;
import static com.reelvideos.app.config.Constants.OUTPUT_FILE_PROCESSED_FINAL;

public class UploadService extends Service {


    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }

    boolean mAllowRebind;
    ServiceCallback Callback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    //OUTPUT_FILE_PROCESSED
    String videoDes;
    String hashTags;
    SharedPreferences sharedPreferences;
    boolean hasUploadedStarted = false;

    public UploadService() {

    }

    public UploadService(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    public void setCallbacks(ServiceCallback serviceCallback) {
        Callback = serviceCallback;
    }

    @Override
    public void onCreate() {
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction().equals("START_SERVICE")) {
                showNotification();
                videoDes = intent.getStringExtra("desc");
                hashTags = intent.getStringExtra("hashTags");

                Log.e("TAGGED", "STARTED");

                if (hasUploadedStarted)
                    return START_NOT_STICKY;
                else hasUploadedStarted = true;

                new Thread(() -> {
                    MultiPartRequest request = new MultiPartRequest(UploadService.this, new Callback() {
                        @Override
                        public void Response(String resp) {

                            Toast.makeText(UploadService.this, R.string.video_done, Toast.LENGTH_SHORT).show();
                            sendBroadcast(new Intent("uploadVideo"));
                            sendBroadcast(new Intent("newVideo"));

                            deleteTemp();
                            stopForeground(true);
                            stopSelf();

                            sendBroadcast(new Intent("uploadVideo"));
//                                sendBroadcast(new Intent("newVideo"));

                        }
                    });

                    request.addString("description", videoDes);


                        //video will compress through FFmpeg first, if it fails then it will upload through GPUMp4Composer
                        String uploadCommand = "-y -i " + OUTPUT_FILE_PROCESSED + " -vcodec libx264 -b:v 1000k -acodec copy " + OUTPUT_FILE_PROCESSED_FINAL;
                        FFmpeg.executeAsync(uploadCommand, new ExecuteCallback() {
                            @Override
                            public void apply(long executionId, int returnCode) {
                                Config.printLastCommandOutput(Log.INFO);
                                int rc = Config.getLastReturnCode();

                                if (rc == RETURN_CODE_SUCCESS) {

                                    //uploading hashTags along with video in comma separated format
                                    Log.e("Completed", "Started Uploading");
                                    if (hashTags != null)
                                        request.addString("hashtags", hashTags);
                                    request.addVideoFile("file", OUTPUT_FILE_PROCESSED_FINAL, Utils.getRandomString() + ".mp4");
                                    request.addPicFile("poster", OUTPUT_FILE_POSTER, Utils.getRandomString() + ".png");
                                    request.execute();


                                } else {

                                    new GPUMp4Composer(OUTPUT_FILE_PROCESSED, Constants.OUTPUT_FILE_PROCESSED_FINAL)
                                            .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                                            .filter(Constants.VIDEO_GL_FILTER)
                                            .videoBitrate((int) (0.25 * 16 * 355 * 631)) //Update as per your requirements
                                            .listener(new GPUMp4Composer.Listener() {
                                                @Override
                                                public void onProgress(double progress) {
                                                    Log.e("ERROR", "Working" + progress);
                                                }

                                                @Override
                                                public void onCompleted() {
                                                    //uploading hashTags along with video in comma separated format
                                                    Log.e("Completed", "Started Uploading");
                                                    if (hashTags != null)
                                                        request.addString("hashtags", hashTags);
                                                    request.addVideoFile("file", OUTPUT_FILE_PROCESSED_FINAL, Utils.getRandomString() + ".mp4");
                                                    request.addPicFile("poster", OUTPUT_FILE_POSTER, Utils.getRandomString() + ".png");
                                                    request.execute();
                                                }

                                                @Override
                                                public void onCanceled() {
                                                    Log.e("ERROR", "Cancled");
                                                }

                                                @Override
                                                public void onFailed(Exception exception) {
                                                    Log.e("ERROR", "Failed");

                                                }
                                            })
                                            .start();
                                }
                            }
                        });

                }).start();


            } else if (intent.getAction().equals("STOP_SERVICE")) {
                stopForeground(true);
                stopSelf();
            }

        }

        return Service.START_STICKY;
    }

    private void deleteTemp() {

        File f = new File(OUTPUT_FILE_POSTER);
        if (f.exists())
            f.delete();

    }


    // this will show the sticky notification during uploading video
    private void showNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        final String CHANNEL_ID = getString(R.string.UPLOAD_CHANNEL);
        final String CHANNEL_NAME = getString(R.string.UPLOAD_CHANNEL);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        androidx.core.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(getString(R.string.uploading_video))
                .setContentText(getString(R.string.uploading_video_2))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        android.R.drawable.stat_sys_upload))
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        startForeground(95, notification);
    }


}