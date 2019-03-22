package com.ldj.combinebitmap.helper;

import android.graphics.Bitmap;

import com.ldj.combinebitmap.listener.OnHandlerListener;


public class CombineHelper {
    public static CombineHelper init() {
        return CombineHelper.SingletonHolder.instance;
    }


    private CombineHelper() {

    }

    private static class SingletonHolder {
        private static final CombineHelper instance = new CombineHelper();
    }

    /**
     * 通过url加载
     *
     * @param builder
     */
    private void loadByUrls(final Builder builder) {
        Bitmap defaultBitmap = null;
        if (builder.getPlaceholder() != 0) {
            defaultBitmap = CompressHelper.getInstance()
                    .compressResource(builder.getContext().getResources(), builder.getPlaceholder(), builder.getSubSize(), builder.getSubSize());
        }
        ProgressHandler handler = new ProgressHandler(defaultBitmap, builder.getCount(), new OnHandlerListener() {
            @Override
            public void onComplete(Bitmap[] bitmaps) {
                setBitmap(builder, bitmaps);
            }
        });

        BitmapLoader.getInstance(builder.getContext()).asyncLoad(builder, handler);
    }

    /**
     * 通过图片的资源id、bitmap加载
     *
     * @param builder
     */
    private void loadByResBitmaps(Builder builder) {
        int subSize = builder.getSubSize();
        Bitmap[] compressedBitmaps = new Bitmap[builder.getCount()];
        for (int i = 0; i < builder.getCount(); i++) {
            if (builder.getResourceIds() != null) {
                compressedBitmaps[i] = CompressHelper.getInstance()
                        .compressResource(builder.getContext().getResources(), builder.getResourceIds()[i], subSize, subSize);
            } else if (builder.getBitmaps() != null) {
                compressedBitmaps[i] = CompressHelper.getInstance()
                        .compressResource(builder.getBitmaps()[i], subSize, subSize);
            }
        }
        setBitmap(builder, compressedBitmaps);
    }

    public void load(Builder builder) {
        if (builder.getOnProgressListener() != null) {
            builder.getOnProgressListener().onStart();
        }

        if (builder.getUrls() != null) {
            loadByUrls(builder);
        } else {
            loadByResBitmaps(builder);
        }
    }

    private void setBitmap(final Builder b, Bitmap[] bitmaps) {
        Bitmap result = b.getLayoutManager().combineBitmap(b.getSize(), b.getSubSize(), b.getGap(), b.getGapColor(), bitmaps);

        // 返回最终的组合Bitmap
        if (b.getOnProgressListener() != null) {
            b.getOnProgressListener().onComplete(result);
        }

        // 给ImageView设置最终的组合Bitmap
        if (b.getImageView() != null) {
            b.getImageView().setImageBitmap(result);
        }
    }
}
