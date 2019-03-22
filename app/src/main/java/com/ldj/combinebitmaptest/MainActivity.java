package com.ldj.combinebitmaptest;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.ldj.combinebitmap.CombineBitmap;
import com.ldj.combinebitmap.layout.DingLayoutManager;
import com.ldj.combinebitmap.layout.WechatLayoutManager;
import com.ldj.combinebitmap.provider.DefaultBitmapAndKeyProvider;
import com.ldj.combinebitmap.listener.OnSubItemClickListener;
import com.shehuan.niv.NiceImageView;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private String[] IMG_URL_ARR = {
            "",
            "http://img.hb.aicdn.com/eca438704a81dd1fa83347cb8ec1a49ec16d2802c846-laesx2_fw658",
            "http://img.hb.aicdn.com/85579fa12b182a3abee62bd3fceae0047767857fe6d4-99Wtzp_fw658",
            "http://img.hb.aicdn.com/2814e43d98ed41e8b3393b0ff8f08f98398d1f6e28a9b-xfGDIC_fw658",
            "http://img.hb.aicdn.com/a1f189d4a420ef1927317ebfacc2ae055ff9f212148fb-iEyFWS_fw658",
            "http://img.hb.aicdn.com/69b52afdca0ae780ee44c6f14a371eee68ece4ec8a8ce-4vaO0k_fw658",
            "http://img.hb.aicdn.com/9925b5f679964d769c91ad407e46a4ae9d47be8155e9a-seH7yY_fw658",
            "http://img.hb.aicdn.com/e22ee5730f152c236c69e2242b9d9114852be2bd8629-EKEnFD_fw658",
            "http://img.hb.aicdn.com/73f2fbeb01cd3fcb2b4dccbbb7973aa1a82c420b21079-5yj6fx_fw658",
    };

    NiceImageView imageView1;
    NiceImageView imageView2;
    NiceImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;
    ImageView imageView6;
    ImageView imageView7;
    ImageView imageView8;
    ImageView imageView9;
    ImageView imageView10;
    ImageView imageView11;


    private int[] getResourceIds(int count) {
        int[] res = new int[count];
        for (int i = 0; i < count; i++) {
            res[i] = R.drawable.cat;
        }
        return res;
    }

    private String[] getUrls(int count) {
        String[] urls = new String[count];
        System.arraycopy(IMG_URL_ARR, 0, urls, 0, count);
        return urls;
    }

    private Bitmap[] getBitmaps(int count) {
        Bitmap[] bitmaps = new Bitmap[count];
        for (int i = 0; i < count; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
        }
        return bitmaps;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScreenUtil.init(this);
        imageView1 = findViewById(R.id.nice_iv1);
        imageView2 = findViewById(R.id.nice_iv2);
        imageView3 = findViewById(R.id.nice_iv3);
        imageView4 = findViewById(R.id.iv4);
        imageView5 = findViewById(R.id.iv5);
        imageView6 = findViewById(R.id.iv6);
        imageView7 = findViewById(R.id.iv7);
        imageView8 = findViewById(R.id.iv8);
        imageView9 = findViewById(R.id.iv9);
        imageView10 = findViewById(R.id.iv10);
        imageView11 = findViewById(R.id.iv11);
        requestStoragePermission();
    }

    @AfterPermissionGranted(1000)
    private void requestStoragePermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            loadDingBitmap(imageView1, 2);

            loadDingBitmap(imageView2, 3);

            loadDingBitmap(imageView3, 4);

            loadWechatBitmap(imageView4, 2);

            loadWechatBitmap(imageView5, 3);

            loadWechatBitmap(imageView6, 4);

            loadWechatBitmap(imageView7, 5);

            loadWechatBitmap(imageView8, 6);

            loadWechatBitmap(imageView9, 7);

            loadWechatBitmap(imageView10, 8);

            loadWechatBitmap(imageView11, 9);

        } else {
            EasyPermissions.requestPermissions(this, "need storage permission", 1000, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (1000 == requestCode) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this)
                        .setTitle("tip")
                        .setRationale("need storage permission")
                        .build()
                        .show();
            }
        }
    }

    private void loadWechatBitmap(ImageView imageView, int count) {
        CombineBitmap.get(imageView)
                .setLayoutManager(new WechatLayoutManager())
                .setSize(180)
                .setGap(3)
                .setGapColor(Color.parseColor("#E8E8E8"))
                .setUrls(getUrls(count))
                .setOnSubItemClickListener(new OnSubItemClickListener() {
                    @Override
                    public void onSubItemClick(int index) {
                        Log.e("SubItemIndex", "--->" + index);
                    }
                })
                .setDefaultBitmapAndKeyProvider(new DefaultBitmapAndKeyProvider() {
                    @Override
                    public Bitmap provide(int count, int index) {
                        return  drawableToBitmap(AvatarHelper.getInstance().makeDefaultDrawable("测",false));
                    }

                    @Override
                    public String getKey(int index) {
                        return "测";
                    }
                })
                .load();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
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

    private void loadDingBitmap(final ImageView imageView, int count) {
        CombineBitmap.get(imageView)
                .setLayoutManager(new DingLayoutManager())
                .setSize(180)
                .setGap(2)
                .setUrls(getUrls(count))
                .setDefaultBitmapAndKeyProvider(new DefaultBitmapAndKeyProvider() {
                    @Override
                    public Bitmap provide(int count, int index) {
                        return  drawableToBitmap(AvatarHelper.getInstance().makeDefaultDrawable("测",false));
                    }

                    @Override
                    public String getKey(int index) {
                        return "测";
                    }
                })
                .load();
    }
}