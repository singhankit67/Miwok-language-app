package com.reelvideos.app.persons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

public class PersonsAdapter extends RecyclerView.Adapter<PersonsAdapter.CustomViewHolder > {
    public Context context;
    ArrayList<PersonsModel> dataList;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, PersonsModel item);
    }

    public PersonsAdapter.OnItemClickListener listener;

    public PersonsAdapter(Context context, ArrayList<PersonsModel> arrayList, PersonsAdapter.OnItemClickListener listener) {
        this.context = context;
        dataList = arrayList;
        this.listener = listener;
    }

    @Override
    public PersonsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_persons_layout, viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        PersonsAdapter.CustomViewHolder viewHolder = new PersonsAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage;
        TextView userName;
        TextView fullName;
        ImageView verificationBadge;
        RelativeLayout mainLayout;

        public CustomViewHolder(View view) {
            super(view);
            mainLayout = view.findViewById(R.id.mainlayout);
            userImage = view.findViewById(R.id.userImage);
            userName = view.findViewById(R.id.usernameTv);
            fullName = view.findViewById(R.id.fullName);
            verificationBadge = view.findViewById ( R.id.verificationBadge );

        }

        public void bind(final int pos , final PersonsModel item, final PersonsAdapter.OnItemClickListener listener) {
            mainLayout.setOnClickListener(v -> {
                listener.onItemClick ( v, pos, item );
            } );
        }

    }

    @Override
    public void onBindViewHolder(final PersonsAdapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        PersonsModel item = dataList.get(i);

        holder.userName.setText(item.getUsername());

        Picasso.get()
                .load(item.getUserImage())
                .centerCrop()
                .placeholder(ContextCompat.getDrawable(context, R.drawable.default_pic))
                .resize(100,100)
                .centerCrop()
                .into(holder.userImage);

        holder.fullName.setText(StringEscapeUtils.unescapeJava(item.getUserFullName()));

        if (item.isVerified())
            holder.verificationBadge.setVisibility ( View.VISIBLE );

        holder.bind(i, dataList.get(i), listener);

    }

}