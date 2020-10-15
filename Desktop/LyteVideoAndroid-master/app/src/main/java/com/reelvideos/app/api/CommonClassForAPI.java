package com.reelvideos.app.api;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.reelvideos.app.config.Constants.API_LOGIN;
import static com.reelvideos.app.config.Constants.API_OTP_VERIFY;
import static com.reelvideos.app.config.Constants.API_POST_COMMENT;
import static com.reelvideos.app.config.Constants.BASE_URL;

public class CommonClassForAPI {

    static RequestQueue requestQueue = CoreApp.getInstance().queue;

    public static void callNonAuthAPI(String url, int methodType, ApiResponseCallback apiResponseCallback) {

        // 0 -> GET
        // 1 -> POST
        if (GlobalVariables.hasUserLoggedIN()) {
            //Check if Logged user
            callAuthAPI(url, methodType, apiResponseCallback);
            return;
        }

        JsonObjectRequest callRequest = new JsonObjectRequest(methodType, url, null,
                apiResponseCallback::onApiSuccessResult,
                apiResponseCallback::onApiFailureResult
        );

        requestQueue.add(callRequest);

    }

    public static void callAuthAPI(String url, int methodType, ApiResponseCallback apiResponseCallback) {


        JsonObjectRequest callRequest = new JsonObjectRequest(methodType, url, null,

                apiResponseCallback::onApiSuccessResult,

                apiResponseCallback::onApiFailureResult) {


            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer " + GlobalVariables.getAuthKey());
                return params;
            }
        };

        requestQueue.add(callRequest);

    }

    public static void callAuthAPI(String mobileNumber, ApiResponseCallback apiResponseCallback){

        StringRequest callRequest = new StringRequest(1, API_LOGIN, response -> {
            try {
                apiResponseCallback.onApiSuccessResult(new JSONObject(response));
            } catch (JSONException e) {
                apiResponseCallback.onApiFailureResult(e);
            }
        },
                apiResponseCallback::onApiFailureResult) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobileNumber);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        requestQueue.add(callRequest);

    }

    public static void callOtpVerifyAPI(String mobileNumber, String OTP, ApiResponseCallback apiResponseCallback){

        StringRequest callRequest = new StringRequest(1, API_OTP_VERIFY, response -> {
            try {
                apiResponseCallback.onApiSuccessResult(new JSONObject(response));
            } catch (JSONException e) {
                apiResponseCallback.onApiFailureResult(e);
            }
        },
                apiResponseCallback::onApiFailureResult) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", mobileNumber);
                params.put("otp", OTP);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        requestQueue.add(callRequest);

    }


    public static void callPostCommentAPI(String message, String videoId, ApiResponseCallback apiResponseCallback){

        StringRequest callRequest = new StringRequest(1, API_POST_COMMENT, response -> {
            try {
                apiResponseCallback.onApiSuccessResult(new JSONObject(response));
            } catch (JSONException e) {
                apiResponseCallback.onApiFailureResult(e);
            }
        },
                apiResponseCallback::onApiFailureResult) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("video", videoId);
                params.put("text", message);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + GlobalVariables.getAuthKey());
                return params;
            }
        };

        requestQueue.add(callRequest);

    }
    public static void callPostVideoDurationAPI(String videoId, String duration, ApiResponseCallback apiResponseCallback){

        StringRequest callRequest = new StringRequest(0, BASE_URL+"videos/"+videoId+"/watch/?duration="+duration, response -> {

            try {
                apiResponseCallback.onApiSuccessResult(new JSONObject(response));
            } catch (JSONException e) {
                apiResponseCallback.onApiFailureResult(e);
            }
        },
                apiResponseCallback::onApiFailureResult) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("duration", duration);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + GlobalVariables.getAuthKey());
                return params;
            }
        };

        requestQueue.add(callRequest);

    }



}



