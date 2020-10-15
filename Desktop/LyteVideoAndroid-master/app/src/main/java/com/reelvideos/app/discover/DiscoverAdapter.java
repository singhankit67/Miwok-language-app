package com.reelvideos.app.discover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;

import java.util.List;

public class DiscoverAdapter extends RecyclerView
        .Adapter<DiscoverAdapter.ParentViewHolder> {

    Context context;

    private RecyclerView.RecycledViewPool
            viewPool
            = new RecyclerView
            .RecycledViewPool();
    private List<HashTagModel> itemList;

    private Listener listener;

    public DiscoverAdapter(Context context, List<HashTagModel> itemList, Listener listener) {
        this.itemList = itemList;
        this.context = context;

        this.listener = listener;
    }

    ///////////KKKKKKKKKKKKK
    public interface Listener {
        void onItemClicked(String ha, String vs);
    }
    ////////////KKKKKKKKKKKK

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(
            @NonNull ViewGroup viewGroup,
            int i) {
        // Here we inflate the corresponding
        // layout of the parent item
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.items_explore_recycler,
                        viewGroup, false);

        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ParentViewHolder parentViewHolder,
            int position) {


        HashTagModel parentItem
                = itemList.get(position);

        parentViewHolder
                .tagName
                .setText("#" + parentItem.getTagName());

//        parentViewHolder
//                .tagViews
//                .setText( context.getString(R.string.views_txt, parentItem.getTagViews()));


        Bundle bundle = new Bundle();
        bundle.putString("HashTag", parentItem.getTagName());
        bundle.putString("ViewsCount", context.getString(R.string.views_txt, parentItem.getTagViews()));
//        parentViewHolder
//                .viewAll
//                .setOnClickListener(view -> {
////                        HashTagVideoFragment hashTagVideoFragment=new HashTagVideoFragment ();
////                        hashTagVideoFragment.setArguments(bundle);
////                        AppCompatActivity activity=(AppCompatActivity)view.getContext ();
////                        activity.getSupportFragmentManager ().beginTransaction ().replace ( R.id.discoverFragment,hashTagVideoFragment ).addToBackStack ( null ).commit ();
//                });



        ////////kkkkkkkkkkkkkkkkk
        parentViewHolder.viewAll.setOnClickListener(view -> listener.onItemClicked(parentItem.getTagName(),parentItem.getTagViews()));
        /////////kkkkkkkkkkkkkkkk

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(
                parentViewHolder
                        .ChildRecyclerView
                        .getContext(),
                LinearLayoutManager.HORIZONTAL,
                false);


        layoutManager
                .setInitialPrefetchItemCount(4);


        NestedVideoAdapter childItemAdapter
                = new NestedVideoAdapter(context, parentItem.getNextAPI(), parentItem.getDataList());
        parentViewHolder
                .ChildRecyclerView
                .setLayoutManager(layoutManager);
        parentViewHolder
                .ChildRecyclerView
                .setAdapter(childItemAdapter);
        parentViewHolder
                .ChildRecyclerView
                .setRecycledViewPool(viewPool);
    }


    @Override
    public int getItemCount() {

        return itemList.size();
    }

    // This class is to initialize
    // the Views present in
    // the parent RecyclerView
    class ParentViewHolder
            extends RecyclerView.ViewHolder {

        private TextView tagName, tagViews;
        private RecyclerView ChildRecyclerView;
        private Button viewAll;

        ParentViewHolder(final View itemView) {
            super(itemView);

            viewAll
                    = itemView
                    .findViewById(R.id.viewsAll);

            tagViews
                    = itemView
                    .findViewById(
                            R.id.viewsTv);
            tagName
                    = itemView
                    .findViewById(
                            R.id.hashtagName);

            ChildRecyclerView
                    = itemView
                    .findViewById(
                            R.id.itemsRecycler);
        }
    }


}