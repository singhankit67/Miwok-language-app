package com.reelvideos.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.callbacks.InsertHashTagListener;
import com.reelvideos.app.databinding.ItemLayoutChipsBlankBinding;
import com.reelvideos.app.models.HashTagsModel;

import java.util.ArrayList;

public class HashTagsSuggestAdapter extends RecyclerView.Adapter<HashTagsSuggestAdapter.ViewHolder> {
    private Context context;
    private ArrayList<HashTagsModel> hashTagList;
    private LayoutInflater layoutInflater;
    private InsertHashTagListener insertHashTagListener;

    public HashTagsSuggestAdapter(Context context, ArrayList<HashTagsModel> hashTagList, InsertHashTagListener insertHashTagListener) {
        this.context = context;
        this.hashTagList = hashTagList;
        this.insertHashTagListener=  insertHashTagListener;
    }

    @NonNull
    @Override
    public HashTagsSuggestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        return new ViewHolder(ItemLayoutChipsBlankBinding.inflate(layoutInflater, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HashTagsSuggestAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mbinding.chip.setText(hashTagList.get(i).getText());
        viewHolder.mbinding.chip.setOnClickListener(v -> insertHashTagListener.onClick(hashTagList.get(i), i));
    }


    @Override
    public int getItemCount() {
        return hashTagList == null ? 0 : hashTagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemLayoutChipsBlankBinding mbinding;

        public ViewHolder(ItemLayoutChipsBlankBinding mbinding) {
            super(mbinding.getRoot());
            this.mbinding = mbinding;
        }
    }
}