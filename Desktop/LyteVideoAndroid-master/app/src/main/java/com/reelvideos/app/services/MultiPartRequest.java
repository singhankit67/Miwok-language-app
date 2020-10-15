package com.reelvideos.app.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.reelvideos.app.config.Constants;
import com.reelvideos.app.config.GlobalVariables;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MultiPartRequest {
    public Context context;
    public MultipartBuilder builder;
    private OkHttpClient client;
    private Callback callBack;
    SharedPreferences sharedPreferences;

    public MultiPartRequest(Context context, Callback callback) {
        this.context = context;
        this.builder = new MultipartBuilder();
        this.builder.type(MultipartBuilder.FORM);
        this.client = new OkHttpClient();
        client.setConnectTimeout(20, TimeUnit.SECONDS);
        client.setWriteTimeout(5,TimeUnit.MINUTES);
        client.setReadTimeout(5,TimeUnit.MINUTES);
        this.callBack = callback;
    }

    public void addString(String name, String value) {
        this.builder.addFormDataPart(name, value);
    }



    public void addVideoFile(String name, String filePath, String fileName) {
        this.builder.addFormDataPart(name, fileName, RequestBody.create(
                MediaType.parse("video/mp4"), new File(filePath)));
    }

    public void addPicFile(String name, String filePath, String fileName) {
        this.builder.addFormDataPart(name, fileName, RequestBody.create(
                MediaType.parse("image/png"), new File(filePath)));
    }


    public String execute() {
        new send().execute();
        return "";
    }

    public class send extends AsyncTask<MultiPartRequest, Void, String> {

        @Override
        protected String doInBackground(MultiPartRequest... multiPartRequests) {
            RequestBody requestBody = null;
            Request request = null;
            Response response = null;


            String strResponse = null;

            try {



                requestBody = builder.build();
                request = new Request.Builder()
                        .url(Constants.API_POST_VIDEO).post(requestBody)
                        .header("Authorization", "Bearer " + GlobalVariables.getAuthKey())
                        .build();


                response = client.newCall(request).execute();



                if (!response.isSuccessful()) {
                    Log.e("TAGGED", response+"");
                    throw new IOException();
                }

                strResponse = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                builder = null;
                if (client != null)
                    client = null;
                System.gc();
            }
            return strResponse;
        }

        @Override
        protected void onPostExecute(String response) {

            if (response != null)
                callBack.Response(response);
            else
                callBack.Response("");

            super.onPostExecute(response);
        }
    }

}


