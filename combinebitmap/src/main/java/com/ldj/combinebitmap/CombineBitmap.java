package com.ldj.combinebitmap;

import android.content.Context;
import android.widget.ImageView;

import com.ldj.combinebitmap.helper.Builder;

public class CombineBitmap {
    public static Builder get(Context context) {
        return new Builder(context);
    }

    public static Builder get(ImageView imageView) {
        return new Builder(imageView);
    }
}
