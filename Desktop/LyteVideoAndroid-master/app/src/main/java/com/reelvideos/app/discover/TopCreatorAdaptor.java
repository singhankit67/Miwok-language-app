package com.reelvideos.app.discover;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;
import com.reelvideos.app.persons.PersonsModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TopCreatorAdaptor extends
        RecyclerView.Adapter<TopCreatorAdaptor.ParentViewHolder > {
    public Context context;
    ArrayList<PersonsModel> dataList;


    public interface OnItemClickListener  {
        void onItemClick(View view, int position, PersonsModel item);
    }


    public TopCreatorAdaptor.OnItemClickListener listener;

    public TopCreatorAdaptor(Context context, ArrayList<PersonsModel> arrayList, TopCreatorAdaptor.OnItemClickListener listener) {
        this.context = context;
        for (PersonsModel personsModel : dataList = arrayList) {

        }
        ;
        this.listener = (OnItemClickListener) listener;
    }

    @Override
    public TopCreatorAdaptor.ParentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.item_icon_topcreator,
                        viewGroup, false);


        return new TopCreatorAdaptor.ParentViewHolder (view);
        /*View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_icon_topcreator, viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        TopCreatorAdaptor.ParentViewHolder viewHolder = new TopCreatorAdaptor.ParentViewHolder (view);
        return viewHolder;*/
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }




    @Override
    public void onBindViewHolder(@NonNull TopCreatorAdaptor.ParentViewHolder holder, int position) {

        PersonsModel personsModel= dataList.get(position);

        //holder.userName.setText(  personsModel.getUsername () );

        Picasso.get()
                .load(personsModel.getUserImage())
                .centerCrop()
                .placeholder(ContextCompat.getDrawable(context, R.drawable.default_pic))
                .resize(200,200)
                .into(holder.userImage);

        //holder.fullName.setText(item.getUserFullName());
        holder.bind(position, dataList.get(position),listener );


    }
    class ParentViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage;
        TextView userName;
        //TextView fullName;
        ConstraintLayout mainLayout;

        public ParentViewHolder(@NonNull View itemView) {
            super ( itemView );
            mainLayout = itemView.findViewById(R.id.mainLayout);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.username);
            //fullName = view.findViewById(R.id.fullName);
        }

        public void bind(final int pos , final PersonsModel item, final TopCreatorAdaptor.OnItemClickListener listener) {
            mainLayout.setOnClickListener(v -> {
                listener.onItemClick ( v, pos, item );
            } );
        }

    }

}
