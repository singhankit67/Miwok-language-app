package com.reelvideos.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.callbacks.InsertHashTagListener;
import com.reelvideos.app.databinding.ItemLayoutChipsBinding;
import com.reelvideos.app.models.HashTagsModel;

import java.util.ArrayList;

public class HashTagsInsertedAdapter extends RecyclerView.Adapter<HashTagsInsertedAdapter.ViewHolder> {
    private Context context;
    private ArrayList<HashTagsModel> hashTagList;
    private LayoutInflater layoutInflater;
    private InsertHashTagListener insertHashTagListener;

    public HashTagsInsertedAdapter(Context context, ArrayList<HashTagsModel> hashTagList, InsertHashTagListener insertHashTagListener) {
        this.context = context;
        this.hashTagList = hashTagList;
        this.insertHashTagListener = insertHashTagListener;
    }

    @NonNull
    @Override
    public HashTagsInsertedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        return new HashTagsInsertedAdapter.ViewHolder(ItemLayoutChipsBinding.inflate(layoutInflater, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagsInsertedAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mbinding.hashChip.setText(hashTagList.get(i).getText());
        viewHolder.mbinding.hashChip.setOnCloseIconClickListener(v -> insertHashTagListener.onClick(hashTagList.get(i), i));
    }


    @Override
    public int getItemCount() {
        return hashTagList == null ? 0 : hashTagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemLayoutChipsBinding mbinding;

        public ViewHolder(ItemLayoutChipsBinding mbinding) {
            super(mbinding.getRoot());
            this.mbinding = mbinding;
        }
    }
}