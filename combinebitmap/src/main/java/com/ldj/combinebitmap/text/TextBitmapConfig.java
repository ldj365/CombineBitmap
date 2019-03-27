package com.ldj.combinebitmap.text;

import android.support.annotation.ColorInt;

public class TextBitmapConfig {
    private String text;
    @ColorInt
    private int textColor;
    @ColorInt
    private int textBackgroundColor;
    private int textSize;
    private int width;
    private int height;

    public TextBitmapConfig setText(String text) {
        this.text = text;
        return this;
    }

    public TextBitmapConfig setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TextBitmapConfig setTextBackgroundColor(@ColorInt int textBackgroundColor) {
        this.textBackgroundColor = textBackgroundColor;
        return this;
    }

    public TextBitmapConfig setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public TextBitmapConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    public TextBitmapConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    public String getText() {
        return text;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextBackgroundColor() {
        return textBackgroundColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTag() {
        return " TextBitmapConfig : textFinal = " + text + "; " +
                " textColor = " + textColor + "; " +
                " textBackgroundColor = " + textBackgroundColor + "; " +
                " textSize = " + textSize + "; " +
                " width = " + width + "; " +
                " height = " + height + "; ";
    }
}
