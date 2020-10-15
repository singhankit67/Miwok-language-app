package com.reelvideos.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.reelvideos.app.adapters.CommentsAdapter;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.models.CommentsModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.reelvideos.app.api.CommonClassForAPI.callNonAuthAPI;
import static com.reelvideos.app.api.CommonClassForAPI.callPostCommentAPI;
import static com.reelvideos.app.config.Constants.getCommentsAPI;
import static com.reelvideos.app.utils.AppExtensions.showToast;

public class CommentBottomSheet extends BottomSheetDialogFragment {
    CommentsAdapter commentsAdapter;
    ArrayList<CommentsModel> dataList = new ArrayList<>();
    String videoID, CommentsCount, VideoDescription, UserImage, UserName, HashTag, message = "";
    boolean isVerified;
    RecyclerView recyclerView;
    ImageView imageView, send, verificationBadge;
    TextView username, commentCount, emptyComments, videoDescription, hashTag, EM1, EM2, EM3, EM4, EM5, EM6, EM7, EM8, EM9;
    EditText textContent;


    public CommentBottomSheet() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.SheetDialog);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_comment, container, false);
        imageView = view.findViewById(R.id.userImageComment);
        //comment = view.findViewById(R.id.tv1_comment);
        username = view.findViewById(R.id.username_comment);
        commentCount = view.findViewById(R.id.tv1_comment);
        recyclerView = view.findViewById(R.id.recylerview_comment);
        emptyComments = view.findViewById(R.id.emptyCommentsMsg);
        videoDescription = view.findViewById(R.id.videoDescription);
        hashTag = view.findViewById(R.id.hashTag);
        verificationBadge = view.findViewById(R.id.verificationBadge);


        textContent = view.findViewById(R.id.textContent);
//        textContent.requestFocus ();
//        textContent.setFocusableInTouchMode(true);
//        textContent.setFocusable ( true );
//        textContent.setOnClickListener ( new View.OnClickListener () {
//            @Override
//            public void onClick(View view) {
//                InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE);
////                imgr.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
//            }
//        } );
        view.findViewById(R.id.em1).setOnClickListener(view1 -> {
            textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_haha)));
            //int position = textContent.length();
            //Editable etext = textContent.getText();
            //Selection.setSelection(etext, position);

        });
        view.findViewById(R.id.em2).setOnClickListener(view2 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_heart))));
        view.findViewById(R.id.em3).setOnClickListener(view3 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_love))));
        view.findViewById(R.id.em4).setOnClickListener(view4 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_ok))));
        view.findViewById(R.id.em5).setOnClickListener(view5 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_fire))));
        view.findViewById(R.id.em7).setOnClickListener(view6 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_nosee))));
        view.findViewById(R.id.em8).setOnClickListener(view7 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_rocking))));
        view.findViewById(R.id.em9).setOnClickListener(view8 -> textContent.setText(String.format("%s%s", textContent.getText(), getString(R.string.em_clap))));

        send = view.findViewById(R.id.send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        commentsAdapter = new CommentsAdapter(getContext(), dataList, (position, item, view1) -> {

        });
        recyclerView.setAdapter(commentsAdapter);

        if (isVerified) verificationBadge.setVisibility(View.VISIBLE);

        username.setText(UserName);
        if (VideoDescription.length() > 0) {
            videoDescription.setVisibility(View.VISIBLE);
            videoDescription.setText(VideoDescription);
        }
/*
        if (HashTag.length ()>0)
        {
            hashTag.setVisibility ( View.VISIBLE );
            hashTag.setText ( HashTag );
        }
*/
        Picasso.get().
                load(UserImage)
                .resize(100, 100)
                .placeholder(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.default_pic, null)))
                .into(imageView);
        commentCount.setText(CommentsCount);

        if (Integer.parseInt(CommentsCount) == 0)
            emptyComments.setVisibility(View.VISIBLE);

        send.setOnClickListener(view12 -> {
            message = textContent.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                if (GlobalVariables.hasUserLoggedIN()) {
                    sendComment(message);
                    addToList(message);
                    textContent.setText(null);
                } else {
                    showToast(getContext(), getString(R.string.login_conti));
                    openLoginActivity();
                }
            }
        });
        //showLoader();
        fetchAllComments();

        return view;
    }

    public void OpenComment(String description, String userImage, String username, String videoId, String commentCount, String hashtag, boolean isVerified) {

        videoID = videoId;
        CommentsCount = commentCount;
        UserName = username;
        VideoDescription = description;
        UserImage = userImage;
        HashTag = hashtag;
    }

    private void addToList(String message) {

        emptyComments.setVisibility(View.GONE);
        CommentsModel tempModel = new CommentsModel();
        tempModel.setCommentTime("Just Now");
        tempModel.setCommentText(message);
        tempModel.setCommentId("-1");
        tempModel.setCommentById(GlobalVariables.getUserId());
        tempModel.setUserImage(GlobalVariables.getUserPic());
        tempModel.setCommentByUsername(GlobalVariables.getUserName());
        tempModel.setVerified(GlobalVariables.getIsVerified());
        dataList.add(0, tempModel);
        commentsAdapter.notifyDataSetChanged();
        commentCount.setText(getString(R.string.comment_count, (Integer.parseInt(CommentsCount) + 1) + ""));

    }

    private void fetchAllComments() {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> callNonAuthAPI(getCommentsAPI(videoID), 0, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                loadUpData(jsonObject);
                //cancelLoader();
            }

            @Override
            public void onApiFailureResult(Exception e) {
                showToast(getContext(), getString(R.string.error_load_cmnt));
                //cancelLoader();
            }
        }));
    }

    private void loadUpData(JSONObject jsonObject) {

        if (dataList == null)
            dataList = new ArrayList<>();

        try {

            JSONArray commentsArray = jsonObject.getJSONArray("results");
            CommentsModel tempModel;
            for (int i = 0; i < commentsArray.length(); i++) {
                tempModel = new CommentsModel();
                JSONObject singleObjectComment = commentsArray.getJSONObject(i);
                tempModel.setCommentId(String.valueOf(singleObjectComment.optLong("id")));
                tempModel.setCommentById(String.valueOf(singleObjectComment.optLong("user")));
                tempModel.setCommentByUsername(singleObjectComment.optString("username"));
                tempModel.setCommentText(singleObjectComment.optString("text"));
                tempModel.setLikesOnComment(String.valueOf(singleObjectComment.optLong("num_likes")));
                tempModel.setIsCommentLiked(singleObjectComment.optBoolean("is_liked"));
                tempModel.setUserImage(singleObjectComment.optString("user_pic"));
                tempModel.setCommentTime((singleObjectComment.optString("time_ago")));
                tempModel.setVerified(singleObjectComment.optBoolean("is_verified"));

                dataList.add(tempModel);
            }

            commentsAdapter.notifyDataSetChanged();
            commentCount.setText(getString(R.string.comment_count, CommentsCount));

            if (dataList.size() == 0)
                emptyComments.setVisibility(View.VISIBLE);
            else emptyComments.setVisibility(View.GONE);

        } catch (JSONException e) {
            showToast(getContext(), getString(R.string.error_load_cmnt));
        }
    }

    private void sendComment(String message) {

        Handler handler = new Handler(Looper.getMainLooper());
        // separate thread is used for network calls
        handler.post(() -> callPostCommentAPI(message, videoID, new ApiResponseCallback() {
            @Override
            public void onApiSuccessResult(JSONObject jsonObject) {
                //showToast(getContext(), getString(R.string.comment_posted));
            }

            @Override
            public void onApiFailureResult(Exception e) {
                showToast(getContext(), getString(R.string.comment_failed));

            }
        }));
    }

    private void openLoginActivity() {

        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);

    }

}