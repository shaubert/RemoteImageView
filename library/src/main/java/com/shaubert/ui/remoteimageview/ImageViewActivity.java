package com.shaubert.ui.remoteimageview;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.util.List;

public class ImageViewActivity extends FullscreenActivity {

    public static final String IMAGE_URL_EXTRA = "_image_url_extra";

    private ImageView photoView;

    private boolean animating;
    private Handler handler = new Handler();
    private PhotoViewAttacher photoViewAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoView = new ImageView(this);
        photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        setContentView(photoView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA);
        RemoteImageView.getImageLoader().displayImage(imageUrl, photoView, RemoteImageView.getDisplayImageOptions(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startPostponedEnterTransition();
                }
                scheduleConnectPhotoAttacher();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                scheduleConnectPhotoAttacher();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoView.setTransitionName(imageUrl);

            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    animating = true;
                }

                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                    setEnterSharedElementCallback(null);
                    animating = false;
                    connectPhotoAttacher();
                }
            });
        }
    }

    private void connectPhotoAttacher() {
        if (animating) {
            scheduleConnectPhotoAttacher();
            return;
        }


        if (photoViewAttacher == null) {
            photoViewAttacher = new PhotoViewAttacher(photoView);
            photoViewAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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

}