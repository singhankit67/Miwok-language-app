package com.reelvideos.app.profile;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.reelvideos.app.R;
import com.reelvideos.app.models.HomeVideosModel;
import com.reelvideos.app.utils.AppExtensions;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

public class MyVideosAdapter extends RecyclerView.Adapter<MyVideosAdapter.CustomViewHolder > {

    public Context context;
    private MyVideosAdapter.OnItemClickListener listener;
    private MyVideosAdapter.OnItemLongClickListener longClickListener;
    private ArrayList<HomeVideosModel> dataList;


    public interface OnItemClickListener {
        void onItemClick(int position, HomeVideosModel item, View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, HomeVideosModel item, View view);
    }
    public MyVideosAdapter(Context context, ArrayList<HomeVideosModel> dataList, MyVideosAdapter.OnItemClickListener listener, MyVideosAdapter.OnItemLongClickListener longListener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.longClickListener = longListener;
    }

    @Override
    public MyVideosAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myvideo_layout,null);
        MyVideosAdapter.CustomViewHolder viewHolder = new MyVideosAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }



    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView thumb_image;

        TextView likesTv,viewTv;

        public CustomViewHolder(View view) {
            super(view);

            thumb_image = view.findViewById(R.id.thumb_image);
//            likesTv = view.findViewById(R.id.likesCount);
            viewTv = view.findViewById ( R.id.ViewCount );
        }

        public void bind(final int position,final HomeVideosModel item, final MyVideosAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                listener.onItemClick(position,item,v);
            });
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(position,item,v);
                return true;
            });
        }
    }

    @Override
    public void onBindViewHolder(final MyVideosAdapter.CustomViewHolder holder, final int i) {
        final HomeVideosModel item= dataList.get(i);
        holder.setIsRecyclable(true);

        Picasso.get()
                .load(item.getPosterURL())
                .resize(225,300)
                .centerCrop()
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.drawable.image_placeholder)
                .into(holder.thumb_image);

//        holder.likesTv.setText(AppExtensions.GetSuffix(item.getVideoLikesCountt()));
        holder.viewTv.setText ( AppExtensions.GetSuffix(item.getVideoViewCount ()) );
        holder.bind(i,item,listener);
    }
}