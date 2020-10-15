package com.reelvideos.app.discover;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;
import com.reelvideos.app.WatchVideosActivity;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.profile.MyVideosAdapter;
import com.reelvideos.app.utils.AppExtensions;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


//This Class created by Shubham_keshri
public class HashTagVideoAdapter extends RecyclerView
        .Adapter<HashTagVideoAdapter.CustomViewHolder> {

    Context context;
    private List<HomeVideosModel> itemList;
    String nextAPI;
    private HashTagVideoAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, HomeVideosModel item, View view);
    }

    HashTagVideoAdapter(Context context, HashTagVideoAdapter.OnItemClickListener listener, List<HomeVideosModel> itemList)
    {
        this.nextAPI = nextAPI;
        this.itemList = itemList;
        this.context = context;
        this.listener = listener;

    }


    @NonNull
    @Override
    public HashTagVideoAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.item_video_icon,
                        viewGroup, false);
        
        return new HashTagVideoAdapter.CustomViewHolder ( view );
    }


    @Override
    public void onBindViewHolder(@NonNull HashTagVideoAdapter.CustomViewHolder holder, final int position) {

        final HomeVideosModel  homeVideosModel= itemList.get(position);
        holder.setIsRecyclable(true);


//        holder.likesTv
//                .setText( AppExtensions.GetSuffix(homeVideosModel.getVideoLikesCount()));

        holder.viewsTv
                .setText ( AppExtensions.GetSuffix(homeVideosModel.getVideoViewCount ()) );

        Picasso.get()
                .load(homeVideosModel.getPosterURL())
                .resize(225,300)
                .centerCrop()
                .into(holder.thumbnail);

        holder.bind(position,homeVideosModel,listener);
        //Toast.makeText ( context, String.valueOf ( homeVideosModel.getCreatorID () ), Toast.LENGTH_SHORT ).show ();

    }




    @Override
    public int getItemCount()
    {
        return itemList.size();
    }

    public class CustomViewHolder extends
            RecyclerView.ViewHolder {
        TextView likesTv,viewsTv;
        ImageView thumbnail;
        CardView materialCardView;
        public CustomViewHolder(@NonNull View itemView) {
            super ( itemView );
//            likesTv = itemView.findViewById(R.id.likesCount);
            thumbnail = itemView.findViewById(R.id.thumb_image);
            materialCardView = itemView.findViewById(R.id.mainCard);
            viewsTv = itemView.findViewById ( R.id.ViewCount );

        }

        public void bind(final int position,final HomeVideosModel item, final HashTagVideoAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                listener.onItemClick(position,item,v);
            });
        }
    }
}
