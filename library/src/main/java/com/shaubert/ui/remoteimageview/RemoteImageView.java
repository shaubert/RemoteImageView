package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

public class RemoteImageView extends OptimizedImageView {

    private static ImageLoader imageLoader;
    private static DisplayImageOptions displayImageOptions;
    private static Class<? extends Activity> openImageActivityClass;

    public static class Setup {
        private ImageLoader imageLoader;
        private DisplayImageOptions displayImageOptions;
        private Class<? extends Activity> openImageActivityClass;

        public Setup imageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            return this;
        }

        public Setup displayImageOptions(DisplayImageOptions displayImageOptions) {
            this.displayImageOptions = displayImageOptions;
            return this;
        }

        public Setup openImageActivityClass(Class<? extends Activity> openImageActivityClass) {
            this.openImageActivityClass = openImageActivityClass;
            return this;
        }

        public void apply() {
            if (imageLoader == null) {
                imageLoader = ImageLoader.getInstance();
            }
            if (displayImageOptions == null) {
                displayImageOptions = DisplayImageOptions.createSimple();
            }
            if (openImageActivityClass == null) {
                openImageActivityClass = ImageViewActivity.class;
            }

            RemoteImageView.imageLoader = imageLoader;
            RemoteImageView.displayImageOptions = displayImageOptions;
            RemoteImageView.openImageActivityClass = openImageActivityClass;
        }
    }

    private String imageUrl;
    private Drawable defaultImage;
    private DisplayImageOptions options;
    private ImageViewAware imageAware;

    private ImageLoadingListener activeListener;
    private ImageLoadingListener listener;
    private ImageLoadingListener openImageLoadingListener = new ImageLoadingListener() {
        @Override
        @SuppressLint("NewApi")
        public void onLoadingComplete(final String imageUri, View view, final Bitmap loadedImage) {
            openImageClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;
                        String transitionName = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getTransitionName() : null;
                        ActivityOptionsCompat activityOptions =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                        activity, RemoteImageView.this, transitionName);
                        Intent intent = new Intent(activity, getOpenImageActivityClass());
                        intent.putExtra(ImageViewActivity.IMAGE_URL_EXTRA, imageUrl);
                        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
                    }
                }
            };
            setupClickListeners();
            if (listener != null) {
                listener.onLoadingComplete(imageUri, view, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (listener != null) {
                listener.onLoadingCancelled(imageUri, view);
            }
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            openImageClickListener = null;
            setupClickListeners();
            if (listener != null) {
                listener.onLoadingStarted(imageUri, view);
            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (listener != null) {
                listener.onLoadingFailed(imageUri, view, failReason);
            }
        }
    };

    private OnClickListener openImageClickListener;
    private OnClickListener clickListener;
    private OnClickListener multipleClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (openImageClickListener != null) {
                openImageClickListener.onClick(v);
            }
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        }
    };

    public RemoteImageView(Context context) {
        super(context);
        init();
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        imageAware = new ImageViewAware(this);
    }

    @Override
    public void setOnClickListener(final OnClickListener clickListener) {
        this.clickListener = clickListener;
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (clickListener != null || openImageClickListener != null) {
            super.setOnClickListener(multipleClickListener);
        } else {
            super.setOnClickListener(null);
        }
    }

    public static ImageLoader getImageLoader() {
        if (imageLoader == null) {
            imageLoader = ImageLoader.getInstance();
        }
        return imageLoader;
    }

    public static DisplayImageOptions getDisplayImageOptions() {
        if (displayImageOptions == null) {
            displayImageOptions = DisplayImageOptions.createSimple();
        }
        return displayImageOptions;
    }

    public static Class<? extends Activity> getOpenImageActivityClass() {
        if (openImageActivityClass == null) {
            openImageActivityClass = ImageViewActivity.class;
        }
        return openImageActivityClass;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void cancelLoading() {
        imageUrl = null;
        getImageLoader().cancelDisplayTask(this);
        if (defaultImage != null) {
            setImageDrawable(defaultImage);
        }
    }

    public void setDefaultImage(int defaultImageResId) {
        if (defaultImageResId > 0) {
            setDefaultImage(getContext().getResources().getDrawable(defaultImageResId));
        } else {
            setDefaultImage(null);
        }
    }

    public void setDefaultImage(Drawable drawable) {
        this.defaultImage = drawable;
        if (defaultImage != null) {
            if (getDrawable() == null) {
                setImageDrawable(defaultImage);
            }
            options = new DisplayImageOptions.Builder()
                    .cloneFrom(getDisplayImageOptions())
                    .showImageOnLoading(defaultImage)
                    .showImageOnFail(defaultImage)
                    .showImageForEmptyUri(defaultImage)
                    .build();
        } else {
            options = null;
        }
    }

    public void reload() {
        if (!TextUtils.isEmpty(imageUrl)) {
            String url = imageUrl;
            imageUrl = null;
            setImageUrl(url);
        }
    }

    public void setImageUrl(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransitionName(url);
        }

        if (!TextUtils.equals(url, imageUrl)) {
            cancelLoading();
            if (!TextUtils.isEmpty(url)) {
                imageUrl = url;
                if (options != null) {
                    getImageLoader().displayImage(url, imageAware, options, activeListener);
                } else {
                    getImageLoader().displayImage(url, imageAware, activeListener);
                }
            }
        }
    }

    public boolean inCache(String url) {
        return inMemoryCache(url) || inDiscCache(url);
    }

    public boolean inMemoryCache(String url) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        ImageSize targetSize = ImageSizeUtils.defineTargetSizeForView(imageAware,
                new ImageSize(displayMetrics.widthPixels, displayMetrics.heightPixels));
        String memoryCacheKey = MemoryCacheUtils.generateKey(url, targetSize);
        Bitmap bitmap = getImageLoader().getMemoryCache().get(memoryCacheKey);
        return bitmap != null && !bitmap.isRecycled();
    }

    public boolean inDiscCache(String url) {
        return DiskCacheUtils.findInCache(url, getImageLoader().getDiskCache()) != null;
    }

    public void setListener(ImageLoadingListener listener) {
        if (activeListener == null || activeListener == this.listener) {
            activeListener = listener;
        }
        this.listener = listener;
    }

    public void enableOpenOnClick() {
        activeListener = openImageLoadingListener;
    }

    public void disableOpenOnClick() {
        activeListener = listener;
    }

}