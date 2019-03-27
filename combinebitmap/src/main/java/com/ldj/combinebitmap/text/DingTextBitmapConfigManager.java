package com.ldj.combinebitmap.text;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class DingTextBitmapConfigManager implements ITextBitmapConfigManager {
    @ColorInt
    private int textColor=Color.WHITE;
    @ColorInt
    private int backgroundColor=Color.parseColor("#5D72A0");

    public DingTextBitmapConfigManager() {

    }

    public DingTextBitmapConfigManager(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public TextBitmapConfig getTextConfig(int count, int index, int size, int subSize, String text) {
        TextBitmapConfig textBitmapConfig = new TextBitmapConfig();
        textBitmapConfig.setTextColor(textColor);
        textBitmapConfig.setTextBackgroundColor(backgroundColor);
        textBitmapConfig.setWidth(size);
        textBitmapConfig.setHeight(size);
        if (text != null && text.length() != 0) {
            if (count == 1) {
                if (text.length() >= 2) {
                    textBitmapConfig.setText(text.substring(text.length() - 2, text.length()));
                    textBitmapConfig.setTextSize(subSize / 3);
                } else {
                    textBitmapConfig.setText(text);
                    textBitmapConfig.setTextSize(subSize * 2 / 3);
                }
            } else if (count == 2) {
                textBitmapConfig.setText(text.substring(text.length() - 1, text.length()));
                textBitmapConfig.setTextSize(subSize * 2 / 3);
            } else if (count == 3) {
                if (index == 0) {
                    textBitmapConfig.setText(text.substring(text.length() - 1, text.length()));
                    textBitmapConfig.setTextSize(subSize * 2 / 3);
                } else {
                    textBitmapConfig.setText(text.substring(text.length() - 1, text.length()));
                    textBitmapConfig.setTextSize(subSize * 2 / 3);
                    textBitmapConfig.setWidth(subSize);
                    textBitmapConfig.setHeight(subSize);
                }
            } else {
                textBitmapConfig.setText(text.substring(text.length() - 1, text.length()));
                textBitmapConfig.setTextSize(subSize * 2 / 3);
                textBitmapConfig.setWidth(subSize);
                textBitmapConfig.setHeight(subSize);
            }
        }
        return textBitmapConfig;
    }
}
