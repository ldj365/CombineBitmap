package com.ldj.combinebitmap.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.ldj.combinebitmap.cache.DiskLruCacheHelper;
import com.ldj.combinebitmap.cache.LruCacheHelper;
import com.ldj.combinebitmap.provider.DefaultBitmapAndKeyProvider;

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
import java.util.regex.Pattern;

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


    public void asyncLoad(final int index, final Builder builder, final Handler handler) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(builder.getCount(), index, builder.getUrls()[index], builder.getSubSize(), builder.getSubSize(), builder.getDefaultBitmapAndKeyProvider());
                if (bitmap != null) {
                    handler.obtainMessage(1, index, -1, bitmap).sendToTarget();
                } else {
                    handler.obtainMessage(2, index, -1, null).sendToTarget();
                }
            }
        };

        if (collectUndoTasks(getOriginalUrl(index, builder.getUrls()[index], builder.getDefaultBitmapAndKeyProvider()), task)) {
            return;
        }

        ThreadPool.getInstance().execute(task);
    }

    private String getOriginalUrl(int index, String url, DefaultBitmapAndKeyProvider provider) {
        String key;
        if (TextUtils.isEmpty(url)) {
            if (provider != null) {
                key = provider.getKey(index);
            } else {
                key = "";
            }
        } else {
            key = url;
        }
        return key;
    }

    private Bitmap loadBitmap(int count, int index, String url, int reqWidth, int reqHeight, DefaultBitmapAndKeyProvider defaultBitmapAndKeyProvider) {
        url = getOriginalUrl(index, url, defaultBitmapAndKeyProvider);
        String key = Utils.hashKeyFormUrl(url);

        // 尝试从内存缓存中读取
        Bitmap bitmap = lruCacheHelper.getBitmapFromMemCache(key);
        if (bitmap != null) {
            Log.e(TAG, "load from memory:" + url);
            return bitmap;
        }

        try {
            // 尝试从磁盘缓存中读取
            bitmap = loadBitmapFromDiskCache(url, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.e(TAG, "load from disk:" + url);
                return bitmap;
            }
            // 尝试下载
            bitmap = loadBitmapFromHttp(url, reqWidth, reqHeight);
            if (bitmap != null) {
                Log.e(TAG, "load from http:" + url);
                return bitmap;
            }

            //尝试从DefaultBitmapProvider获取
            bitmap = loadBitmapFromProvider(count, index, reqWidth, reqHeight, defaultBitmapAndKeyProvider);
            if (bitmap != null) {
                Log.e(TAG, "load from provider:" + url);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap loadBitmapFromProvider(int count, int index, int reqWidth, int reqHeight, DefaultBitmapAndKeyProvider provider) throws IOException {
        if (provider == null) {
            return null;
        }
        String url = getOriginalUrl(index, "", provider);
        String key = Utils.hashKeyFormUrl(url);
        Bitmap bitmap = provider.provide(count, index);
        if (bitmap == null) {
            return null;
        }

        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
            executeUndoTasks(url);
        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }


    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight)
            throws IOException {
        String key = Utils.hashKeyFormUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(0);
            if (downloadUrlToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();

            executeUndoTasks(url);
        }
        return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
    }

    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        if( urlString == null || urlString.length() == 0 || !Pattern.matches("\"[a-zA-z]+://[^\\\\s]*\"", urlString)){
            return false;
        }

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
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

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        Bitmap bitmap = null;
        String key = Utils.hashKeyFormUrl(url);
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
        if (snapShot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapShot.getInputStream(0);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = compressHelper.compressDescriptor(fileDescriptor, reqWidth, reqHeight);
            if (bitmap != null) {
                lruCacheHelper.addBitmapToMemoryCache(key, bitmap);
            }
        }

        return bitmap;
    }

    private boolean collectUndoTasks(String url, Runnable task) {
        String key = Utils.hashKeyFormUrl(url);

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

    private void executeUndoTasks(String url) {
        String key = Utils.hashKeyFormUrl(url);
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