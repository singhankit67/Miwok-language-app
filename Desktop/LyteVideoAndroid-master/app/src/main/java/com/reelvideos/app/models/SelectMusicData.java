package com.reelvideos.app.models;

public class SelectMusicData {
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreator(long creator) {
        this.creator = creator;
    }

    private String title;
    private String musicURL;
    long id;
    long creator;
    private boolean _isPlaying;
    private String posterURL;
    private String singer;
    private boolean is_localMusic=false;


    public boolean isIs_localMusic() {
        return is_localMusic;
    }

    public void setIs_localMusic(boolean is_localMusic) {
        this.is_localMusic = is_localMusic;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public long getCreator() {
        return creator;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMusicURL() {
        return musicURL;
    }

    public boolean is_isPlaying() {
        return _isPlaying;
    }

    public void set_isPlaying(boolean _isPlaying) {
        this._isPlaying = _isPlaying;
    }
}
