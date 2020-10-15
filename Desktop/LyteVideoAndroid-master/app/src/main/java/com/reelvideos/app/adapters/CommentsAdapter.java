package com.reelvideos.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.DifferentProfileActivity;
import com.reelvideos.app.R;
import com.reelvideos.app.models.CommentsModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.icu.lang.UProperty.INT_START;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CustomViewHolder > {

    public Context context;
    private CommentsAdapter.OnItemClickListener listener;
    private ArrayList<CommentsModel> dataList;



    public interface OnItemClickListener {
        void onItemClick(int position, CommentsModel item, View view);
    }

    public CommentsAdapter(Context context, ArrayList<CommentsModel> dataList, CommentsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;

    }

    @Override
    public CommentsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        CommentsAdapter.CustomViewHolder viewHolder = new CommentsAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final CommentsAdapter.CustomViewHolder holder, final int i) {

        final CommentsModel item = dataList.get(i);


        String s= item.getCommentByUsername ()+" "+item.getCommentText ();
        SpannableString ss1=  new SpannableString (s);
//        ss1.setSpan(new RelativeSizeSpan (1.2f), 0,item.getCommentByUsername ().length (), 0); // set size
//        ss1.setSpan(new ForegroundColorSpan ( Color.RED), 0, item.getCommentByUsername ().length (), 0);// set color
        ss1.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0,item.getCommentByUsername ().length (), 0);

        holder.username.setText ( ss1 );
        //holder.username.setText(item.getCommentByUsername());

        try{
            Picasso.get().
                    load(item.getUserImage())
                    .resize(50,50)
                    .placeholder(context.getResources().getDrawable(R.drawable.default_pic))
                    .into(holder.user_pic);

        }catch (Exception ignored){

        }



        holder.timeAgoTv.setText ( item.getCommentTime () );

        if (item.isVerified())
            holder.verificationBadge.setVisibility ( View.VISIBLE );




            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DifferentProfileActivity.class);
                    intent.putExtra("ID", String.valueOf(item.getCommentById()));
                    intent.putExtra("NAME", item.getCommentByUsername());
                    intent.putExtra("IMG", item.getUserImage());
                    intent.putExtra("isFollowed", false);
                    context.startActivity(intent);
                }
            });

    }



    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username,message, timeAgoTv;
        ImageView user_pic,verificationBadge;
        LinearLayout linearLayout;


        public CustomViewHolder(View view) {
            super(view);


            username = view.findViewById(R.id.username);
            user_pic = view.findViewById(R.id.user_pic);
            message = view.findViewById(R.id.message);
            timeAgoTv = view.findViewById(R.id.timeAgoTv);
            verificationBadge = view.findViewById ( R.id.verificationBadge );
            linearLayout = view.findViewById(R.id.upperLayout);

        }

        public void bind(final int postion,final CommentsModel item, final CommentsAdapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> listener.onItemClick(postion,item,v));

        }


    }





}