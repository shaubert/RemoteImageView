package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class ImageViewActivity extends Activity {

    public static final String IMAGE_URL_EXTRA = "_image_url_extra";
    public static final String SCALE_TYPE_EXTRA = "_image_scale_type_extra";

    private ImageView photoView;

    private boolean animating = true;
    private boolean loading = true;
    private Handler handler = new Handler();
    private PhotoViewAttacher photoViewAttacher;
    private ImageView.ScaleType scaleType;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA);
        String scaleTypeStr = getIntent().getStringExtra(SCALE_TYPE_EXTRA);
        scaleType = ImageView.ScaleType.CENTER_INSIDE;
        if (!TextUtils.isEmpty(scaleTypeStr)) {
            scaleType = ImageView.ScaleType.valueOf(scaleTypeStr);
        }

        photoView = new ImageView(this);
        photoView.setScaleType(scaleType);
        setContentView(photoView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().getDecorView().setBackground(new ColorDrawable(Color.BLACK));

        RemoteImageView.getImageLoader().displayImage(imageUrl, photoView, RemoteImageView.getDisplayImageOptions(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                loading = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
                connectPhotoAttacher();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                loading = false;
                ImagePickerController.showLoadingError(ImageViewActivity.this);
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);

            photoView.setTransitionName(imageUrl);

            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    disconnectPhotoAttacher();
                    animating = true;
                    super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
                }

                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                    animating = false;
                    connectPhotoAttacher();
                }
            });
        } else {
            animating = false;
            connectPhotoAttacher();
        }
    }

    private void connectPhotoAttacher() {
        if (isFinishing() || photoViewAttacher != null) {
            return;
        }

        if (animating || loading) {
            scheduleConnectPhotoAttacher();
            return;
        }

        photoViewAttacher = new PhotoViewAttacher(photoView);
        photoViewAttacher.setScaleType(scaleType);
    }

    private void disconnectPhotoAttacher() {
        if (photoViewAttacher != null) {
            photoViewAttacher.cleanup();
            photoViewAttacher = null;
        }
    }

    private void scheduleConnectPhotoAttacher() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectPhotoAttacher();
            }
        }, 500);
    }

    public static void start(Activity activity, RemoteImageView view) {
        start(activity, view, view.getImageUrl());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void start(Activity activity, ImageView sharedImageView, String imageUrl) {
        ImageDownloader.Scheme scheme = ImageDownloader.Scheme.ofUri(imageUrl);
        if (scheme == ImageDownloader.Scheme.FILE) {
            try {
                imageUrl = URLDecoder.decode(imageUrl, "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
            }
        }
        String transitionName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? sharedImageView.getTransitionName() : null;
        ActivityOptionsCompat activityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedImageView, transitionName);
        Intent intent = new Intent(activity, RemoteImageView.getOpenImageActivityClass());
        intent.putExtra(ImageViewActivity.IMAGE_URL_EXTRA, imageUrl);
        intent.putExtra(ImageViewActivity.SCALE_TYPE_EXTRA, sharedImageView.getScaleType().name());
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }

}