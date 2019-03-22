package com.ldj.combinebitmap.provider;

import android.graphics.Bitmap;

public interface DefaultBitmapAndKeyProvider {
    Bitmap provide(int count,int index);
    String getKey(int index);
}
