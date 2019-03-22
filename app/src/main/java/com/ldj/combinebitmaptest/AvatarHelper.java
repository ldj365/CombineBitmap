package com.ldj.combinebitmaptest;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.text.TextUtils;

/**
 * Created by lurending on 2017/11/23.
 */

public class AvatarHelper {
    public static final int FONT_SIZE_NORMAL = 16;
    public static final int FONT_SIZE_BIG = 32;

    private static AvatarHelper instance;

    private AvatarHelper() {

    }

    public static AvatarHelper getInstance() {
        if (instance == null) {
            synchronized (AvatarHelper.class) {
                if (instance == null) {
                    instance = new AvatarHelper();
                }
            }
        }
        return instance;
    }

    private String makeText(String name, boolean cutOutFromFront, int cutCount) {
        if (TextUtils.isEmpty(name)) {
            name = "未知";
        } else if (name.length() > cutCount) {
            if (cutOutFromFront) {
                name = name.substring(0, cutCount);
            } else {
                name = name.substring(name.length() - cutCount, name.length());
            }
        }
        return name;
    }


    private @ColorInt
    int getColor() {
        return Color.parseColor("#5D72A0");
    }

    public Drawable makeDefaultDrawable(String name, boolean cutOutFromFront) {
        return makeRectDrawable(name, cutOutFromFront, 2, 16, getColor());
    }


    public Drawable makeDefaultDrawable(String name, boolean cutOutFromFront, int cutCount, int fontSize, @ColorInt int color) {
        return makeRectDrawable(name, cutOutFromFront, cutCount, fontSize, color);
    }


    public Drawable makeRoundRectDrawable(String name, boolean cutOutFromFront, int cutCount, int fontSize, @ColorInt int color) {
        return TextDrawable.builder()
                .beginConfig()
                .fontSize(ScreenUtil.dip2px(fontSize))
                .height(ScreenUtil.dip2px(50))
                .width(ScreenUtil.dip2px(50))
                .endConfig()
                .buildRoundRect(makeText(name, cutOutFromFront, cutCount), color, 5);
    }

    public Drawable makeRectDrawable(String name, boolean cutOutFromFront, int cutCount, int fontSize, @ColorInt int color) {
        return TextDrawable.builder()
                .beginConfig()
                .fontSize(ScreenUtil.dip2px(fontSize))
                .height(ScreenUtil.dip2px(50))
                .width(ScreenUtil.dip2px(50))
                .endConfig()
                .buildRect(makeText(name, cutOutFromFront, cutCount), color);
    }

}
