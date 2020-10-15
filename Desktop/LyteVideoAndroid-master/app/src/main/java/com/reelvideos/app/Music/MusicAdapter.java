package com.reelvideos.app.Music;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;
import com.reelvideos.app.models.MusicCategoryModel;
import com.reelvideos.app.models.SelectMusicData;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ParentViewHolder> {

    Context context;

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List<MusicCategoryModel> itemList;


    public MusicAdapter(Context context,List<MusicCategoryModel> itemList) {

        this.itemList = itemList;
        this.context = context;
    }


    @NonNull
    @Override
    public MusicAdapter.ParentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_music_category_recycler,
                        viewGroup,
                        false);

        return new ParentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ParentViewHolder parentViewHolder, int position) {

        MusicCategoryModel parentItem
                = itemList.get(position);

        parentViewHolder
                .categoryName
                .setText(parentItem.getCategoryTag());


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(parentViewHolder.ChildRecyclerView.getContext());


        layoutManager
                .setInitialPrefetchItemCount(4);


        List<SelectMusicData> Mini_list;

        if (parentItem.getDataList ().size ()>3)
        {
            Mini_list=parentItem.getDataList ().subList ( 0,3 );
        }
        else{
            Mini_list=parentItem.getDataList ();
            parentViewHolder.AllSongs.setVisibility ( View.GONE );
        }
        if (parentItem.getDataList ().size ()==0){
            parentViewHolder.AllSongs.setVisibility ( View.GONE );
            parentViewHolder.categoryName.setVisibility ( View.GONE );
        }


        NestedMusicListAdapter childItemAdapter = new NestedMusicListAdapter(context,Mini_list);
        parentViewHolder.ChildRecyclerView.setLayoutManager(layoutManager);
        parentViewHolder.ChildRecyclerView.setAdapter(childItemAdapter);
        parentViewHolder.ChildRecyclerView.setRecycledViewPool(viewPool);




        //Send array list to display all song of a particular category
        List<SelectMusicData> result = parentItem.getDataList ();
        ArrayList<SelectMusicData> dataList = new ArrayList<>(result.size());
        dataList.addAll(result);
        Bundle bundle = new Bundle();
        bundle.putSerializable("valuesArray", dataList);
        bundle.putString ( "category",parentItem.getCategoryTag () );
        bundle.putString ( "category_id", String.valueOf ( parentItem.getId () ) );
        parentViewHolder
                .AllSongs
                .setOnClickListener ( new View.OnClickListener () {
                    @Override
                    public void onClick(View view) {


                        ShowAllMusicFragment showAllMusicFragment = new ShowAllMusicFragment ();
                        showAllMusicFragment.setArguments(bundle);
                        AppCompatActivity activity=(AppCompatActivity)view.getContext ();
                        activity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_frame, showAllMusicFragment, showAllMusicFragment.getClass().getSimpleName()).addToBackStack(null).commit();



                        //stop music of activity on fragment
                        childItemAdapter.stopMusic ();
                    }
                } );


    }

    @Override
    public int getItemCount() {
        return itemList.size();

    }

    public class ParentViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryName,AllSongs;
        private RecyclerView ChildRecyclerView;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);

            AllSongs
                    = itemView
                    .findViewById (
                            R.id.allSong );
            categoryName
                    = itemView
                    .findViewById(
                            R.id.musicCategoryName);
            ChildRecyclerView
                    = itemView
                    .findViewById(
                            R.id.musicItemsRecycler);
        }
    }
}
