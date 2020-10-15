package com.reelvideos.app.Music;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reelvideos.app.R;
import com.reelvideos.app.SelectMusic;
import com.reelvideos.app.models.SelectMusicData;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import static com.reelvideos.app.utils.Utils.selected_music_is_local;


public class NestedMusicListAdapter extends RecyclerView.Adapter<NestedMusicListAdapter.ChildViewHolder>{

    private int lastSelectedMusic;
    public static MediaPlayer mediaPlayer;
    private List<SelectMusicData> ChildItemList;
    Context context;
    //String nextAPI;

    public NestedMusicListAdapter(Context context, List<SelectMusicData> childItemList) {
        this.ChildItemList = childItemList;
        this.context = context;
//        this.nextAPI = nextAPI;
    }

    @NonNull
    @Override
    public NestedMusicListAdapter.ChildViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.select_music_list,
                        viewGroup, false);

        return new ChildViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull NestedMusicListAdapter.ChildViewHolder holder, int position) {

        // Create an instance of the ChildItem
        // class for the given position
        Picasso.get()
                .load(ChildItemList.get ( position ).getPosterURL ())
                .placeholder( ContextCompat.getDrawable(context, R.drawable.music_placeholder))
                .resize(100,100)
                .centerCrop()
                .into(holder.ImagePoster);
        holder.textView.setText(ChildItemList.get(position).getTitle());
        holder.singer.setText ( ChildItemList.get(position).getSinger () );
//        holder.textView.startAnimation((Animation) AnimationUtils.loadAnimation(context,R.anim.text_animation));




        if (ChildItemList.get(position).is_isPlaying()) {
            holder.imageView.setImageResource(R.drawable.ic_action_musicpause);
            holder.selectMusic.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_action_musicplay);
            holder.selectMusic.setVisibility(View.GONE);
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ChildItemList.get(position).is_isPlaying()){
                    stopMusic ();
                    ChildItemList.get(lastSelectedMusic).set_isPlaying(false);
                    holder.imageView.setImageResource(R.drawable.ic_action_musicplay);
                    holder.selectMusic.setVisibility(View.GONE);
                }
                else {
                    if (lastSelectedMusic != -1) {
                        ChildItemList.get(lastSelectedMusic).set_isPlaying(false);
                        notifyItemChanged(lastSelectedMusic);
                    }
                    lastSelectedMusic = position;
                    stopMusic ();
                    ChildItemList.get(position).set_isPlaying(true);
                    notifyItemChanged(position);

                    playMusic(ChildItemList.get(position).getMusicURL());
                }
            }
        });

        holder.selectMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_music_is_local=ChildItemList.get ( position ).isIs_localMusic ();
                SelectMusic.selectedMusic = ChildItemList.get(position).getMusicURL();
                SelectMusic.selectedMusicName = ChildItemList.get(position).getTitle();
                if (context instanceof SelectMusic)
                    ((SelectMusic) context).returnMusic();


//                Utils.downloadAudio((SelectMusic) context, data.get(position).getMusicURL(), "temp");
            }
        });


    }



    @Override
    public int getItemCount() {
        return ChildItemList.size();
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {

        public Button selectMusic;
        public RelativeLayout relativeLayout;
        public ImageView imageView,ImagePoster;
        public TextView textView,singer;
        public ChildViewHolder(@NonNull View v) {
            super ( v );

            textView = v.findViewById(R.id.select_music_list_textview_title);
            selectMusic = v.findViewById(R.id.select_music_button_select);
            relativeLayout = v.findViewById(R.id.select_music_relativelayout_main);
            imageView = v.findViewById(R.id.select_music_image_progress_indicator);
            ImagePoster=v.findViewById ( R.id.musicPoster );
            singer = v.findViewById ( R.id.select_music_list_textview_author );
        }
    }

    private void playMusic(String musicURL) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(musicURL);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
