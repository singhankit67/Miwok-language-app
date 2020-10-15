package com.reelvideos.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.reelvideos.app.R;
import com.reelvideos.app.callbacks.ApiResponseCallback;
import com.reelvideos.app.config.GlobalVariables;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.utils.AppExtensions;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

import static com.reelvideos.app.api.CommonClassForAPI.callAuthAPI;
import static com.reelvideos.app.config.Constants.API_FOLLOW_USER;

public class HomeVideosAdapter extends RecyclerView.Adapter<HomeVideosAdapter.CustomViewHolder> {

    public Context context;
    private HomeVideosAdapter.OnItemClickListener listener;
    private ArrayList<HomeVideosModel> dataList;


    public interface OnItemClickListener {
        void onItemClick(int position, HomeVideosModel item, View view);
    }


    public HomeVideosAdapter(Context context, ArrayList<HomeVideosModel> dataList, HomeVideosAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @Override
    public HomeVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_home_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        HomeVideosAdapter.CustomViewHolder viewHolder = new HomeVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }
    @Override
    public void onBindViewHolder(final HomeVideosAdapter.CustomViewHolder holder, final int i) {
        final HomeVideosModel item = dataList.get(i);
        holder.setIsRecyclable(false);

        try {
            holder.bind(i, item, listener);
            holder.username.setText(item.getVideoCreatorName());

            if ((item.getSoundName() == null || item.getSoundName().equals("") || item.getSoundName().equals("null"))) {
                holder.sound_name.setText(context.getString(R.string.originalSoundTx, item.getVideoCreatorName()));
            } else {
                holder.sound_name.setText(item.getSoundName());
            }
            holder.sound_name.setSelected(true);

            holder.desc_txt.setText(item.getVideoDescription() + " " + item.getHashTags());

            Picasso.get().
                    load(item.getCreatorIMG())
                    .centerCrop()
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.default_profile_white))
                    .resize(100, 100).into(holder.user_pic);


            if (item.isVerified()) {
                holder.verification.setVisibility(View.VISIBLE);
            }

            if ((item.getSoundName() == null || item.getSoundName().equals(""))
                    || item.getSoundName().equals("null")) {

                item.setSoundPic(item.getCreatorIMG());

            } else if (item.getSoundPic().equals(""))
                item.setSoundPic("Null");

            Picasso.get().
                    load(item.getSoundPic()).centerCrop()
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_round_music))
                    .resize(100, 100).into(holder.sound_image);

            Picasso.get().
                    load(item.getPosterURL())
                    .into(holder.thumbnail);


            if (item.isLiked()) {
                holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_lyte_heart_filled));
            } else {
                holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_lyte_heart));
            }


            holder.like_txt.setText(AppExtensions.GetSuffix(item.getVideoLikesCount()));
            holder.comment_txt.setText(AppExtensions.GetSuffix(item.getVideoCommentsCount()));
            holder.shareCount.setText(AppExtensions.GetSuffix(item.getSharesCount()));
            holder.downloadCount.setText(AppExtensions.GetSuffix(item.getDownloadsCount()));


            if (!item.isFollowing()) {
                holder.followBtn.setVisibility(View.VISIBLE);
            }
            holder.followBtn.setOnClickListener(view -> followUser(item.getCreatorID(), holder));

        } catch (Exception ignored) {

        }
    }

    private void followUser(long userID, final HomeVideosAdapter.CustomViewHolder holder) {
        if (GlobalVariables.hasUserLoggedIN()) {
            context.sendBroadcast(new Intent(String.valueOf(R.string.newFollowing)));
            holder.followBtn.setVisibility(View.GONE);
            callAuthAPI(API_FOLLOW_USER.replace("%id%", String.valueOf(userID)), 0, new ApiResponseCallback() {
                @Override
                public void onApiSuccessResult(JSONObject jsonObject) {

                }

                @Override
                public void onApiFailureResult(Exception e) {

                }
            });

        }
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username, desc_txt, sound_name, followBtn;
        ImageView user_pic, sound_image, thumbnail, verification;

        LinearLayout like_layout, comment_layout, shared_layout, sound_image_layout, downloadLayout;
        ImageView like_image, comment_image;
        TextView like_txt, comment_txt, shareCount, downloadCount;
        PlayerView playerView;


        public CustomViewHolder(View view) {
            super(view);


            username = view.findViewById(R.id.username);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.sound_name);
            sound_image = view.findViewById(R.id.sound_image);

            playerView = view.findViewById(R.id.playerView);

            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.like_image);
            like_txt = view.findViewById(R.id.like_txt);

            thumbnail = view.findViewById(R.id.thumbnail);
            desc_txt = view.findViewById(R.id.desc_txt);

            downloadLayout = view.findViewById(R.id.download_layout);

            comment_layout = view.findViewById(R.id.comment_layout);
            comment_image = view.findViewById(R.id.comment_image);
            comment_txt = view.findViewById(R.id.comment_txt);

            shareCount = view.findViewById(R.id.shareCountTv);
            downloadCount = view.findViewById(R.id.downloadCountTv);

            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            shared_layout = view.findViewById(R.id.shared_layout);
            verification = view.findViewById(R.id.verificationBadge);

            followBtn = view.findViewById(R.id.followBtn);
        }

        public void bind(final int position, final HomeVideosModel item, final HomeVideosAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> listener.onItemClick(position, item, v));


            user_pic.setOnClickListener(v -> listener.onItemClick(position, item, v));

            username.setOnClickListener(v -> listener.onItemClick(position, item, v));


            like_layout.setOnClickListener(v -> listener.onItemClick(position, item, v));

            comment_layout.setOnClickListener(v -> listener.onItemClick(position, item, v));

            shared_layout.setOnClickListener(v -> listener.onItemClick(position, item, v));

            sound_image_layout.setOnClickListener(v -> listener.onItemClick(position, item, v));

            downloadLayout.setOnClickListener(v -> listener.onItemClick(position, item, v));
        }
    }
}