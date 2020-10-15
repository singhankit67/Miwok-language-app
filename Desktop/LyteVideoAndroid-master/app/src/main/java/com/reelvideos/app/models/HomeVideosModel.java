package com.reelvideos.app.models;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;

public class HomeVideosModel implements Serializable {

    String videoID;
    String videoURL;
    String videoDescription;
    long creatorID;
    String creatorIMG;
    long videoLikesCount;
    long videoViewCount;
    long videoCommentsCount;
    boolean isLiked;
    boolean isFollowing;
    long sharesCount;
    long downloadsCount;
    String videoCreatorName;
    String soundName;
    String soundPic;
    String posterURL;
    String hashTags;
    long viewsCount;
    boolean isFollowed,isVerified;

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public void setHashTags(String hashTags) {
        this.hashTags = hashTags;
    }

    public long getVideoViewCount() {
        return videoViewCount;
    }

    public void setVideoViewCount(long videoViewCount) {
        this.videoViewCount = videoViewCount;
    }

    public String getHashTags() {
        return hashTags;
    }

    public void setHashTags(JSONArray hashTagsArray) {
        String temp = "";
        int count = 0;
        try {
            if (hashTagsArray != null) {
                for (int i = 0; i < hashTagsArray.length(); i++) {
                    temp = temp.concat("#" + hashTagsArray.getJSONObject(i).optString("name") + " ");
                    count += hashTagsArray.getJSONObject(i).optLong("total_views");
                }
            }
        } catch (JSONException e) {
            temp = "";
        }
        setViewsCount(count);
        this.hashTags = temp;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getSoundPic() {
        return soundPic;
    }

    public void setSoundPic(String soundPic) {
        this.soundPic = soundPic;
    }

    public String getSoundName() {
        return soundName;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(long videoID) {
        this.videoID = String.valueOf(videoID);
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public long getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(long creatorID) {
        this.creatorID = creatorID;
    }

    public String getCreatorIMG() {
        return creatorIMG;
    }

    public void setCreatorIMG(String creatorIMG) {
        this.creatorIMG = creatorIMG;
    }

    public long getVideoLikesCount() {
        return videoLikesCount;
    }

    public void setVideoLikesCount(long videoLikesCount) {
        this.videoLikesCount = videoLikesCount;
    }

    public long getVideoCommentsCount() {
        return videoCommentsCount;
    }

    public void setVideoCommentsCount(long videoCommentsCount) {
        this.videoCommentsCount = videoCommentsCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public long getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(long sharesCount) {
        this.sharesCount = sharesCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public String getVideoCreatorName() {
        return videoCreatorName;
    }

    public void setVideoCreatorName(String videoCreatorName) {
        this.videoCreatorName = videoCreatorName;
    }
}
