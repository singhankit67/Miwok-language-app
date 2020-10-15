package com.reelvideos.app.discover;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.reelvideos.app.R;
import com.reelvideos.app.WatchVideosActivity;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.utils.AppExtensions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NestedVideoAdapter extends RecyclerView
        .Adapter<NestedVideoAdapter.ChildViewHolder> {

    private ArrayList<HomeVideosModel> ChildItemList;
    Context context;
    String nextAPI;

    NestedVideoAdapter(Context context, String nextAPI, ArrayList<HomeVideosModel> childItemList)
    {
        this.ChildItemList = childItemList;
        this.context = context;
        this.nextAPI = nextAPI;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup,
            int i)
    {

        // Here we inflate the corresponding
        // layout of the child item
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.item_video_icon_hashtag,
                        viewGroup, false);

        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChildViewHolder childViewHolder,
            int position)
    {

        // Create an instance of the ChildItem
        // class for the given position
        HomeVideosModel childItem = ChildItemList.get(position);


//        childViewHolder.likesTv
//                .setText(AppExtensions.GetSuffix(childItem.getVideoLikesCount()));

        childViewHolder.viewsTv
                .setText ( AppExtensions.GetSuffix(childItem.getVideoViewCount ()) );

        Picasso.get()
                .load(childItem.getPosterURL())
                .into(childViewHolder.thumbnail);

        childViewHolder.materialCardView.setOnClickListener(view -> openWatchVideo(position));

    }

    public void openWatchVideo(int position){
        Intent intent = new Intent(context, WatchVideosActivity.class);
        intent.putExtra("dataArray", ChildItemList);
        intent.putExtra("position", position);
        intent.putExtra("isFromProfile", false);
        intent.putExtra("NEXT_API", nextAPI);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount()
    {

        return ChildItemList.size();
    }


    class ChildViewHolder
            extends RecyclerView.ViewHolder {

        TextView likesTv,viewsTv;
        ImageView thumbnail;
        MaterialCardView materialCardView;

        ChildViewHolder(View itemView)
        {
            super(itemView);
//            likesTv = itemView.findViewById(R.id.likesCount);
            viewsTv = itemView.findViewById ( R.id.ViewCount );
            thumbnail = itemView.findViewById(R.id.thumb_image);
            materialCardView = itemView.findViewById(R.id.mainCard);
        }
    }
}