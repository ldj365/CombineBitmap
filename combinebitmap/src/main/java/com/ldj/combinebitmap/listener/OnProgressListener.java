package com.ldj.combinebitmap.listener;

import android.graphics.Bitmap;

public interface OnProgressListener {
    void onStart();

    void onComplete(Bitmap bitmap);
}
