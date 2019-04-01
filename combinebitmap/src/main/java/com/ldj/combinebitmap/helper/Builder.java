package com.ldj.combinebitmap.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Region;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.ldj.combinebitmap.bean.ImageData;
import com.ldj.combinebitmap.layout.DingLayoutManager;
import com.ldj.combinebitmap.layout.ILayoutManager;
import com.ldj.combinebitmap.layout.WechatLayoutManager;
import com.ldj.combinebitmap.listener.OnProgressListener;
import com.ldj.combinebitmap.listener.OnSubItemClickListener;
import com.ldj.combinebitmap.region.DingRegionManager;
import com.ldj.combinebitmap.region.IRegionManager;
import com.ldj.combinebitmap.region.WechatRegionManager;
import com.ldj.combinebitmap.text.ITextBitmapConfigManager;

public class Builder {
    private Context context;
    private ImageView imageView;
    private int size; // 最终生成bitmap的尺寸
    private int gap; // 每个小bitmap之间的距离
    private int gapColor; // 间距的颜色
    private int placeholder; // 获取图片失败时的统一默认图片
    private int count; // 要加载的资源数量
    private int subSize; // 单个bitmap的尺寸

    private ILayoutManager layoutManager; // bitmap的组合样式

    private ITextBitmapConfigManager textConfigManager;//文字的大小颜色配置

    private Region[] regions;
    private OnSubItemClickListener onSubItemClickListener; // 单个bitmap点击事件回调

    private OnProgressListener onProgressListener; // 最终的组合bitmap回调接口

    private ImageData[] imageDatas;
    private String[] urls;
    private Bitmap[] bitmaps;
    private int[] resourceIds;

    public Builder(Context context) {
        this.context = context;
    }

    public Builder(ImageView imageView) {
        this.context = imageView.getContext();
        this.imageView = imageView;
    }

    public Builder setImageView(ImageView imageView) {
        this.imageView = imageView;
        return this;
    }

    public Builder setSize(int sizeInDP) {
        this.size = Utils.dp2px(context, sizeInDP);
        return this;
    }

    public Builder setGap(int gap) {
        this.gap = Utils.dp2px(context, gap);
        return this;
    }

    public Builder setGapColor(@ColorInt int gapColor) {
        this.gapColor = gapColor;
        return this;
    }

    public Builder setPlaceholder(@DrawableRes int placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public Builder setLayoutManager(ILayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return this;
    }

    public Builder setTextConfigManager(ITextBitmapConfigManager textConfigManager) {
        this.textConfigManager = textConfigManager;
        return this;
    }

    public Builder setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }

    public Builder setOnSubItemClickListener(OnSubItemClickListener onSubItemClickListener) {
        this.onSubItemClickListener = onSubItemClickListener;
        return this;
    }

    public Builder setImageDatas(ImageData... imageDatas) {
        this.imageDatas = imageDatas;
        this.count = imageDatas.length;
        urls = null;
        bitmaps = null;
        resourceIds = null;
        return this;
    }

    public Builder setUrls(String... urls) {
        this.urls = urls;
        this.count = urls.length;
        imageDatas = null;
        bitmaps = null;
        resourceIds = null;
        return this;
    }

    public Builder setBitmaps(Bitmap... bitmaps) {
        this.bitmaps = bitmaps;
        this.count = bitmaps.length;
        imageDatas = null;
        urls = null;
        resourceIds = null;
        return this;
    }

    public Builder setResourceIds(int... resourceIds) {
        this.resourceIds = resourceIds;
        this.count = resourceIds.length;
        imageDatas = null;
        urls = null;
        bitmaps = null;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getSize() {
        return size;
    }

    public int getGap() {
        return gap;
    }

    public int getGapColor() {
        return gapColor;
    }

    public int getPlaceholder() {
        return placeholder;
    }

    public int getCount() {
        return count;
    }

    public int getSubSize() {
        return subSize;
    }

    public ILayoutManager getLayoutManager() {
        return layoutManager;
    }

    public ITextBitmapConfigManager getTextConfigManager() {
        return textConfigManager;
    }

    public Region[] getRegions() {
        return regions;
    }

    public OnSubItemClickListener getOnSubItemClickListener() {
        return onSubItemClickListener;
    }

    public OnProgressListener getOnProgressListener() {
        return onProgressListener;
    }

    public ImageData[] getImageDatas() {
        return imageDatas;
    }

    public String[] getUrls() {
        return urls;
    }

    public Bitmap[] getBitmaps() {
        return bitmaps;
    }

    public int[] getResourceIds() {
        return resourceIds;
    }


    public String getPayLoadUrl(int index) {
        return getImageDatas() == null ? getUrls()[index] : getImageDatas()[index].getUrl();
    }

    public String getDefaultDesc(int index) {
        String key = "";
        if (imageDatas != null) {
            if (!TextUtils.isEmpty(imageDatas[index].getText()) && getTextConfigManager() != null) {
                key = getTextConfigManager().getTextConfig(getCount(), index, getSize(), getSubSize(), imageDatas[index].getText()).getDesc();
            } else {
                key = "PlaceHolder : " + imageDatas[index].getPlaceHolder();
            }
        }
        return key;
    }

    public String getKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TotalKey : ");
        for (int i = 0; i < count; i++) {
            stringBuilder.append(getPayLoadUrl(i)).append(" ").append(getDefaultDesc(i));
        }
        return Utils.hashKeyFormUrl(stringBuilder.toString());
    }

    public void load() {
        subSize = getSubSize(size, gap, layoutManager, count);
        initRegions();
        CombineHelper.init().load(this);
    }


    /**
     * 根据最终生成bitmap的尺寸，计算单个bitmap尺寸
     *
     * @param size
     * @param gap
     * @param layoutManager
     * @param count
     * @return
     */
    private int getSubSize(int size, int gap, ILayoutManager layoutManager, int count) {
        int subSize = 0;
        if (layoutManager instanceof DingLayoutManager) {
            if (count == 1) {
                subSize = size;
            } else {
                subSize = (size - gap) / 2;
            }
        } else if (layoutManager instanceof WechatLayoutManager) {
            if (count < 2) {
                subSize = size;
            } else if (count < 5) {
                subSize = (size - 3 * gap) / 2;
            } else if (count < 10) {
                subSize = (size - 4 * gap) / 3;
            }
        } else {
            throw new IllegalArgumentException("Must use DingLayoutManager or WechatRegionManager!");
        }
        return subSize;
    }

    /**
     * 初始化RegionManager
     */
    private void initRegions() {
        if (onSubItemClickListener == null || imageView == null) {
            return;
        }

        IRegionManager regionManager;

        if (layoutManager instanceof DingLayoutManager) {
            regionManager = new DingRegionManager();
        } else if (layoutManager instanceof WechatLayoutManager) {
            regionManager = new WechatRegionManager();
        } else {
            throw new IllegalArgumentException("Must use DingLayoutManager or WechatRegionManager!");
        }

        regions = regionManager.calculateRegion(size, subSize, gap, count);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            int initIndex = -1;
            int currentIndex = -1;
            Point point = new Point();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                point.set((int) event.getX(), (int) event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initIndex = getRegionIndex(point.x, point.y);
                        currentIndex = initIndex;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        currentIndex = getRegionIndex(point.x, point.y);
                        break;
                    case MotionEvent.ACTION_UP:
                        currentIndex = getRegionIndex(point.x, point.y);
                        if (currentIndex != -1 && currentIndex == initIndex) {
                            onSubItemClickListener.onSubItemClick(currentIndex);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        initIndex = currentIndex = -1;
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 根据触摸点计算对应的Region索引
     *
     * @param x
     * @param y
     * @return
     */
    private int getRegionIndex(int x, int y) {
        for (int i = 0; i < regions.length; i++) {
            if (regions[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

}
