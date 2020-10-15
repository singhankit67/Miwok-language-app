package com.reelvideos.app.callbacks;

import org.json.JSONObject;

public interface ApiResponseCallback {
      void onApiSuccessResult(JSONObject jsonObject);
      void onApiFailureResult(Exception e);
}
