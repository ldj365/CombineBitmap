package com.ldj.combinebitmap.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.ldj.combinebitmap.bean.ImageData;
import com.ldj.combinebitmap.text.TextDrawable;
import com.ldj.combinebitmap.cache.DiskLruCacheHelper;
import com.ldj.combinebitmap.cache.LruCacheHelper;
import com.ldj.combinebitmap.text.TextBitmapConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitmapLoader {
    private static String TAG = BitmapLoader.class.getSimpleName();
    private static final int BUFFER_SIZE = 8 * 1024;

    private LruCacheHelper lruCacheHelper;
    private DiskLruCache mDiskLruCache;
    private CompressHelper compressHelper;

    private volatile static BitmapLoader manager;

    public static BitmapLoader getInstance(Context context) {
        if (manager == null) {
            synchronized (BitmapLoader.class) {
                if (manager == null) {
                    manager = new BitmapLoader(context);
                }
            }
        }
        return manager;
    }

    // 存储线程池中的任务
    private Map<String, Runnable> doingTasks;
    // 存储暂时不能进入线程池的任务
    private Map<String, List<Runnable>> undoTasks;


    private BitmapLoader(Context context) {
        mDiskLruCache = new DiskLruCacheHelper(context).mDiskLruCache;
        lruCacheHelper = new LruCacheHelper();
        compressHelper = CompressHelper.getInstance();

        doingTasks = new HashMap<>();
        undoTasks = new HashMap<>();
    }

    public void asyncLoad(Builder builder, Handler handler) {
        for (int i = 0; i < builder.getCount(); i++) {
            asyncLoad(i, builder, handler);
        }
    }

    private void asyncLoad(final int index, final Builder builder, final Handler handler) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(index, builder);
                if (bitmap != null) {
                    handler.obtainMessage(1, index, -1, bitmap).sendToTarget();
                } else {
                    handler.obtainMessage(2, index, -1, null).sendToTarget();
                }
            }
        };

        if (collectUndoTasks(builder.getKey(index), task)) {
            return;
        }

        ThreadPool.getInstance().execute(task);
    }


    private Bitmap loadBitmap(int index, Builder builder) {
        String key = builder.getKey(index);

        // 尝试从内存缓存中读取
        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
        if (bitmap != null) {
            Log.e(TAG, "load from memory:" + key);
            return bitmap;
        }

        try {
            // 尝试从磁盘缓存中读取
            bitmap = loadBitmapFromDiskCache(index, builder);
            if (bitmap != null) {
                Log.e(TAG, "load from disk:" + key);
                return bitmap;
            }

            // 尝试下载
            bitmap = loadBitmapFromHttp(index, builder);
            if (bitmap != null) {
                Log.e(TAG, "load from http:" + key);
                return bitmap;
            }

            //尝试生成文字 Bitmap
            bitmap = createBitmapFromText(index, builder);
            if (bitmap != null) {
                Log.e(TAG, "load from text:" + key);
                return bitmap;
            }

            //尝试加载默认图
            bitmap = loadBitmapFromRes(index, builder);
            if (bitmap != null) {
                Log.e(TAG, "load from res:" + key);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 从硬盘读取
     */
    private Bitmap loadBitmapFromDiskCache(int index, Builder builder) throws IOException {
        Bitmap bitmap = null;
        String key = builder.getKey(index);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(0);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = compressHelper.compressDescriptor(fileDescriptor, builder.getSubSize(), builder.getSubSize());
            if (bitmap != null) {
                lruCacheHelper.addBitmapToMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }


    /**
     * 从网络下载
     */
    private Bitmap loadBitmapFromHttp(int index, Builder builder)
            throws IOException {
        String url = builder.getPayLoadUrl(index);
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        DiskLruCache.Editor editor = mDiskLruCache.edit(builder.getKey(index));
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();

            executeUndoTasks(builder.getKey(index));
        }
        return loadBitmapFromDiskCache(index, builder);
    }

    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        if (!urlString.startsWith("http")) {
            Utils.close(outputStream);
            return false;
        }

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
            if (urlConnection.getResponseCode() == 302 || urlConnection.getResponseCode() == 301) {
                String newUrl = urlConnection.getHeaderField("Location");
                urlConnection.disconnect();
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
            }
            in = new BufferedInputStream(urlConnection.getInputStream(), BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "downloadBitmap failed." + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            Utils.close(out);
            Utils.close(in);
        }
        return false;
    }

    /**
     * 生成带文字的 bitmap
     */
    private Bitmap createBitmapFromText(int index, Builder builder) throws IOException {
        if (builder.getTextConfigManager() == null || builder.getImageDatas() == null || TextUtils.isEmpty(builder.getImageDatas()[index].getText())) {
            return null;
        }
        TextBitmapConfig textBitmapConfig = builder.getTextConfigManager().getTextConfig(builder.getCount(), index, builder.getSize(), builder.getSubSize(), builder.getImageDatas()[index].getText());
        Drawable drawable = TextDrawable.builder()
                .beginConfig()
                .fontSize(textBitmapConfig.getTextSize())
                .textColor(textBitmapConfig.getTextColor())
                .height(textBitmapConfig.getHeight())
                .width(textBitmapConfig.getWidth())
                .endConfig()
                .buildRect(textBitmapConfig.getText(), textBitmapConfig.getTextBackgroundColor());

        Bitmap bitmap = drawableToBitmap(drawable);
        String key = builder.getKey(index);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
            lruCacheHelper.addBitmapToMemoryCache(key, bitmap);
            executeUndoTasks(key);
        }
        return bitmap;
    }

    /**
     * 把 drawable 转成 bitmap
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 从资源文件加载定制的图片
     */
    private Bitmap loadBitmapFromRes(int index, Builder builder) throws IOException {
        if (builder.getTextConfigManager() == null || builder.getImageDatas() == null || builder.getImageDatas()[index].getPlaceHolder() == 0) {
            return null;
        }
        TextBitmapConfig textBitmapConfig = builder.getTextConfigManager().getTextConfig(builder.getCount(), index, builder.getSize(), builder.getSubSize(), builder.getImageDatas()[index].getText());
        Bitmap bitmap = CompressHelper.getInstance().compressResource(builder.getContext().getResources(), builder.getImageDatas()[index].getPlaceHolder(), textBitmapConfig.getWidth(), textBitmapConfig.getHeight());
        String key = builder.getKey(index);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
            lruCacheHelper.addBitmapToMemoryCache(key, bitmap);
            executeUndoTasks(key);
        }
        return bitmap;
    }


    /**
     * 收集撤销任务
     */
    private synchronized boolean collectUndoTasks(String key, Runnable task) {
        if (lruCacheHelper.getBitmapFromMemCache(key) != null) {
            return false;
        }

        DiskLruCache.Snapshot snapShot = null;
        try {
            snapShot = mDiskLruCache.get(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (snapShot != null) {
            return false;
        }

        // 如果当前url下载操作过程的磁盘缓存的Editor未结束，又来了一个新的url，则不能正常生成新Editor
        // 则将新url对应的任务先保存起来
        if (doingTasks.containsKey(key)) {
            if (undoTasks.containsKey(key)) {
                List<Runnable> tasks = undoTasks.get(key);
                tasks.add(task);
                undoTasks.put(key, tasks);
            } else {
                List<Runnable> tasks = new ArrayList<>();
                tasks.add(task);
                undoTasks.put(key, tasks);
            }
            return true;
        }
        doingTasks.put(key, task);
        return false;
    }


    /**
     * 执行撤销任务
     */
    private synchronized void executeUndoTasks(String key) {
        // 检查undoTasks中是否有要执行的任务
        if (undoTasks.containsKey(key)) {
            for (Runnable task : undoTasks.get(key)) {
                ThreadPool.getInstance().execute(task);
            }
            undoTasks.remove(key);
        }
        // 从doingTasks中移除已经执行完的任务
        doingTasks.remove(key);
    }
}
