package com.reelvideos.app.discover;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;

import java.util.ArrayList;
import java.util.List;


//class created By shubham_keshri
public class TreandingHashtagAdapter extends RecyclerView
        .Adapter<TreandingHashtagAdapter.ParentViewHolder> {


    Context context;

    private List<String> itemList;
    private ArrayList<String> itemViews;

    public TreandingHashtagAdapter(Context context, List<String> itemList, ArrayList<String> itemViews) {
        this.itemList = itemList;
        this.itemViews =itemViews;
        this.context=context;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Here we inflate the corresponding
        // layout of the parent item
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.item_layout_chips_trending_hashtags,
                        viewGroup, false);

        return new TreandingHashtagAdapter.ParentViewHolder (view);
    }



    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        String parentItem
                = itemList.get(position);

        holder
                .tagName
                .setText( parentItem);
        Bundle bundle=new Bundle();
        bundle.putString("HashTag",itemList.get ( position));
        bundle.putString ( "ViewsCount",itemViews.get ( position ) );




        holder.HashTagBtn.setOnClickListener (view -> {

//                HashTagVideoFragment hashTagVideoFragment=new HashTagVideoFragment ();
//                hashTagVideoFragment.setArguments(bundle);
//                AppCompatActivity activity=(AppCompatActivity)view.getContext ();
//                activity.getSupportFragmentManager ().beginTransaction ().replace ( R.id.discoverFragment,hashTagVideoFragment ).addToBackStack ( null ).commit ();
//

        });


    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ParentViewHolder extends RecyclerView.ViewHolder {

        private TextView tagName;
        private RelativeLayout HashTagBtn;
        public ParentViewHolder(@NonNull View itemView) {
            super ( itemView );

            tagName
                    = itemView
                    .findViewById(
                            R.id.hashTagChip);
            HashTagBtn
                    =itemView
                    .findViewById (
                            R.id.hastTagBtn );

        }
    }
}
