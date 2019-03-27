package com.ldj.combinebitmap.bean;

import android.support.annotation.DrawableRes;

public class ImageData {
    private String url;
    private String text;
    @DrawableRes
    private int placeHolder;

    public ImageData(String url) {
        this.url = url;
    }

    public ImageData(@DrawableRes int placeHolder) {
        this.placeHolder = placeHolder;
    }

    public ImageData(String url, String text) {
        this.url = url;
        this.text = text;
    }

    public ImageData(String url, String text, @DrawableRes int placeHolder) {
        this.url = url;
        this.text = text;
        this.placeHolder = placeHolder;
    }

    public ImageData setUrl(String url) {
        this.url = url;
        return this;
    }

    public ImageData setText(String text) {
        this.text = text;
        return this;
    }

    public ImageData setPlaceHolder(@DrawableRes int placeHolder) {
        this.placeHolder = placeHolder;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public int getPlaceHolder() {
        return placeHolder;
    }

    public String getTag() {
        return "ImageData : url = " + url + "; " +
                " textOriginal = " + text + "; " +
                " placeHolder = " + placeHolder + "; ";

    }
}
