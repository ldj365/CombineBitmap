package com.ldj.combinebitmap.text;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class WechatTextBitmapConfigManager implements ITextBitmapConfigManager {
    @ColorInt
    private int textColor = Color.WHITE;
    @ColorInt
    private int backgroundColor = Color.parseColor("#5D72A0");

    public WechatTextBitmapConfigManager() {

    }

    public WechatTextBitmapConfigManager(int textColor, int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public TextBitmapConfig getTextConfig(int count, int index, int size, int subSize, String text) {
        TextBitmapConfig textBitmapConfig = new TextBitmapConfig();
        textBitmapConfig.setTextColor(textColor);
        textBitmapConfig.setTextBackgroundColor(backgroundColor);
        textBitmapConfig.setWidth(subSize);
        textBitmapConfig.setHeight(subSize);
        if (text != null && text.length() != 0) {
            if (count == 1) {
                if (text.length() >= 2) {
                    textBitmapConfig.setText(text.substring(text.length() - 2, text.length()));
                    textBitmapConfig.setTextSize(subSize / 3);
                } else {
                    textBitmapConfig.setText(text);
                    textBitmapConfig.setTextSize(subSize * 2 / 3);
                }
            } else {
                textBitmapConfig.setText(text.substring(text.length() - 1, text.length()));
                textBitmapConfig.setTextSize(subSize * 2 / 3);
            }
        }

        return textBitmapConfig;
    }
}
