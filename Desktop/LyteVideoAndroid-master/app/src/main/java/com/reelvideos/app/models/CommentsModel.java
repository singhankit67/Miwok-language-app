package com.reelvideos.app.models;

public class CommentsModel {
    String commentId;
    String commentById;
    String commentByUsername;
    String commentTime;
    String likesOnComment;
    boolean isCommentLiked, isVerified;
    String userImage;

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentById() {
        return commentById;
    }

    public void setCommentById(String commentById) {
        this.commentById = commentById;
    }

    public String getCommentByUsername() {
        return commentByUsername;
    }

    public void setCommentByUsername(String commentByUsername) {
        this.commentByUsername = commentByUsername;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }

    public String getLikesOnComment() {
        return likesOnComment;
    }

    public void setLikesOnComment(String likesOnComment) {
        this.likesOnComment = likesOnComment;
    }

    public boolean getIsCommentLiked() {
        return isCommentLiked;
    }

    public void setIsCommentLiked(boolean isCommentLiked) {
        this.isCommentLiked = isCommentLiked;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    String commentText;
}
