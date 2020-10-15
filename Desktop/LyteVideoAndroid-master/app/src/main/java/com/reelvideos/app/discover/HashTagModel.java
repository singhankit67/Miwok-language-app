package com.reelvideos.app.discover;

import com.reelvideos.app.models.HomeVideosModel;

import java.util.ArrayList;

public class HashTagModel extends HomeVideosModel {
    String tagID;
    String tagName;
    String tagViews;

    public String getNextAPI() {
        return nextAPI;
    }

    public void setNextAPI(String nextAPI) {
        this.nextAPI = nextAPI;
    }

    String nextAPI;
    ArrayList<HomeVideosModel> dataList = new ArrayList<>();

    public ArrayList<HomeVideosModel> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<HomeVideosModel> dataList) {
        this.dataList = dataList;
    }

    public String getTagID() {
        return tagID;
    }

    public void setTagID(String tagID) {
        this.tagID = tagID;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagViews() {
        return tagViews;
    }

    public void setTagViews(String tagViews) {
        this.tagViews = tagViews;
    }
}
