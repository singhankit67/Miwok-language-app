package com.reelvideos.app.models;

public class HashTagsModel {
    boolean isSuggested;
    String text;

    public HashTagsModel(boolean isSuggested, String text) {
        this.isSuggested = isSuggested;
        this.text = text;
    }

    public boolean isSuggested() {
        return isSuggested;
    }

    public void setSuggested(boolean suggested) {
        isSuggested = suggested;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
