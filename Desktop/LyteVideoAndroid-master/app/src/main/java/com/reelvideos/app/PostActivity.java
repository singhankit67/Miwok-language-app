package com.reelvideos.app;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.reelvideos.app.adapters.HashTagsInsertedAdapter;
import com.reelvideos.app.adapters.HashTagsSuggestAdapter;
import com.reelvideos.app.base.BaseActivity;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.callbacks.ServiceCallback;
import com.reelvideos.app.config.Constants;
import com.reelvideos.app.databinding.ActivityPostBinding;
import com.reelvideos.app.models.HashTagsModel;
import com.reelvideos.app.recorder.RecordActivity;
import com.reelvideos.app.services.UploadService;
import com.reelvideos.app.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.config.Constants.OUTPUT_FILE_PROCESSED;
import static com.reelvideos.app.config.Constants.PREF_THUMBNAIL_UPLOAD;
import static com.reelvideos.app.utils.AppExtensions.showToast;
import static com.reelvideos.app.utils.Utils.bitmapToFile;

public class PostActivity extends BaseActivity implements ServiceCallback {

    ActivityPostBinding binding;
    ServiceCallback serviceCallback;
    HashTagsSuggestAdapter hashTagsSuggestAdapter;
    ArrayList<HashTagsModel> suggestedHashTags = new ArrayList<>();
    ArrayList<HashTagsModel> insertedHashTags = new ArrayList<>();
    HashTagsInsertedAdapter hashTagsInsertedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initComponents();
    }

    @Override
    public void initComponents() {
        setupSuggestionChips();
        addTextWatcher();
        binding.backBtn.setOnClickListener(view -> onBackPressed());
        binding.cancelBtn.setOnClickListener(view -> onBackPressed());
        binding.btnPost.setOnClickListener(view -> postVideo());
        loadUpThumbnail();
        loadUpDataFromAPI();
    }

    void loadUpThumbnail() {


        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(OUTPUT_FILE_PROCESSED,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        binding.videoThumbnail.setImageBitmap(bmThumbnail);

        //Create BitMap
        bitmapToFile(this, bmThumbnail);

        //Store Thumbnail for Uploading
        if (bmThumbnail != null)
            CoreApp.getInstance().savePreferenceDataString(PREF_THUMBNAIL_UPLOAD, Utils.bitmapToBase64(this, bmThumbnail));
    }

    private void addTextWatcher() {

        binding.hashTagsET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().contains(" ") || charSequence.toString().contains(",")) {
                    insertHashTag(binding.hashTagsET.getText().toString());
                    binding.hashTagsET.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    void insertHashTag(String text) {
        insertedHashTags.add(new HashTagsModel(false, text));
        hashTagsInsertedAdapter.notifyDataSetChanged();
    }

    private void setupSuggestionChips() {

        hashTagsSuggestAdapter = new HashTagsSuggestAdapter(this, suggestedHashTags, (item, position) -> {
            insertedHashTags.add(new HashTagsModel(true, item.getText()));
            hashTagsInsertedAdapter.notifyDataSetChanged();

            if (item.isSuggested()) {
                suggestedHashTags.remove(position);
                hashTagsSuggestAdapter.notifyDataSetChanged();
                if (hashTagsSuggestAdapter.getItemCount() == 0)
                    binding.tvSuggestion.setVisibility(View.GONE);
            }


        });
        binding.suggestRecycler.setAdapter(hashTagsSuggestAdapter);

        hashTagsInsertedAdapter = new HashTagsInsertedAdapter(this, insertedHashTags, (item, position) -> {

            insertedHashTags.remove(position);
            hashTagsInsertedAdapter.notifyDataSetChanged();

            if (item.isSuggested()) {
                suggestedHashTags.add(new HashTagsModel(true, item.getText()));
                hashTagsSuggestAdapter.notifyDataSetChanged();
                if (hashTagsSuggestAdapter.getItemCount() > 0)
                    binding.tvSuggestion.setVisibility(View.VISIBLE);
            }

        });
        binding.hashRecycler.setAdapter(hashTagsInsertedAdapter);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    private void postVideo() {

        //Upload to server
        if (binding.descriptionET.getText().length() < 0) {
            showToast(this, getString(R.string.des_small));
            return;
        }

        //Process to Upload
        showToast(this, getString(R.string.upload_start));
        startServicePrs();
    }
    public void startServicePrs() {
        serviceCallback = this;

        UploadService mService = new UploadService(serviceCallback);

        if (!Utils.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("START_SERVICE");
            mServiceIntent.putExtra("desc", binding.descriptionET.getText().toString());
            String hashTags = null;
            if (insertedHashTags.size() > 0) {
                hashTags = insertedHashTags.get(0).getText();
                for (int i = 0; i < insertedHashTags.size(); i++) {
                    hashTags += ",";
                    hashTags += insertedHashTags.get(i).getText();
                }
            }
            mServiceIntent.putExtra("hashTags", hashTags);

            startService(mServiceIntent);

            new Handler().postDelayed(() -> {
                sendBroadcast(new Intent("uploadVideo"));
                startActivity(new Intent(PostActivity.this, MainActivity.class));
            }, 1000);

        } else {

            showToast(this, getString(R.string.upload_progress));
        }
    }

    void loadUpDataFromAPI() {

        callAuthAPI(Constants.API_FETCH_TAGS, 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                JSONArray hashtagArray = null;
                try {
                    hashtagArray = jsonObject.getJSONArray(getString(R.string.results));
                    for (int i = 0; i < hashtagArray.length(); i++)
                        suggestedHashTags.add(new HashTagsModel(true, hashtagArray.getJSONObject(i).optString("name")));
                } catch (JSONException ignored) {
                }

                if (suggestedHashTags.size() > 0) {
                    hashTagsSuggestAdapter.notifyDataSetChanged();
                    binding.tvSuggestion.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onApiFailureResult(Exception e) {

            }
        });
    }

    @Override
    public void showResponse(String response) {

        showToast(this, response);

    }
}