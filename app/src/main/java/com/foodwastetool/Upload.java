package com.foodwastetool;

public class Upload {
    private String mImageUrl;

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
}
