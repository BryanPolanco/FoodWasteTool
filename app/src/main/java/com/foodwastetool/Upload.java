package com.foodwastetool;

import com.google.firebase.firestore.Exclude;

public class Upload {
    private String mImageUrl;
    private String mKey;

    public Upload(){
        // needs empty constructor
    }
    public Upload(String imageUrl){
        mImageUrl = imageUrl;
    }
    public String getImageUrl(){
        return mImageUrl;
    }
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
    @Exclude
    public String getKey() {
        return mKey;
    }
    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}
