package com.reelvideos.app.models;

import java.util.ArrayList;
import java.util.List;

public class MusicCategoryModel {

    //String nextAPI;
    List<SelectMusicData> DataList=new ArrayList<> (  );
    String categoryTag;
    long id;

    public MusicCategoryModel() {

    }



    public void setCategoryTag(String categoryTag) {
        this.categoryTag = categoryTag;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDataList(ArrayList<SelectMusicData> dataList) {
        DataList = dataList;
    }

//    public String getNextAPI() {
//        return nextAPI;
//    }
//
//    public void setNextAPI(String nextAPI) {
//        this.nextAPI = nextAPI;
//    }



    public long getId() {
        return id;
    }


    public List<SelectMusicData> getDataList() {
        return DataList;
    }


    public String getCategoryTag() {
        return categoryTag;
    }

}