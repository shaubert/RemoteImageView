package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
    private static Class<? extends Activity> cropImageActivityClass;

    public static class Setup {
        private ImageLoader imageLoader;
        private DisplayImageOptions displayImageOptions;
        private Class<? extends Activity> openImageActivityClass;
        private Class<? extends Activity> cropImageActivityClass;

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

        public Setup cropImageActivityClass(Class<? extends Activity> cropImageActivityClass) {
            this.cropImageActivityClass = cropImageActivityClass;
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
            if (cropImageActivityClass == null) {
                cropImageActivityClass = CropImageActivity.class;
            }

            RemoteImageView.imageLoader = imageLoader;
            RemoteImageView.displayImageOptions = displayImageOptions;
            RemoteImageView.openImageActivityClass = openImageActivityClass;
            RemoteImageView.cropImageActivityClass = cropImageActivityClass;
        }
    }

    private boolean imageLoaded;
    private boolean openOnClickEnabled;
    private String imageUrl;
    private Drawable defaultImage;
    private DisplayImageOptions options;
    private ImageViewAware imageAware;

    private ImageLoadingListener listener;
    private ImageLoadingListener innerLoadingListener = new ImageLoadingListener() {
        @Override
        @SuppressLint("NewApi")
        public void onLoadingComplete(final String imageUri, View view, final Bitmap loadedImage) {
            imageLoaded = true;
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
            imageLoaded = false;
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

    private OnClickListener openImageClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!openOnClickEnabled
                    || !imageLoaded
                    || TextUtils.isEmpty(imageUrl)) return;

            final Context context = getContext();
            if (context instanceof Activity) {
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ImageViewActivity.start((Activity) context, RemoteImageView.this);
                    }
                }, 250);
            }
        }
    };
    private OnClickListener clickListener;
    private OnClickListener multipleClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            openImageClickListener.onClick(v);
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        }
    };

    public RemoteImageView(Context context) {
        super(context);
        init(null);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        imageAware = new ImageViewAware(this);

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RemoteImageView);

            setDefaultImage(typedArray.getDrawable(R.styleable.RemoteImageView_defaultImage));
            setImageUrl(typedArray.getString(R.styleable.RemoteImageView_url));
            if (typedArray.getBoolean(R.styleable.RemoteImageView_enableOpenOnClick, false)) {
                enableOpenOnClick();
            }

            typedArray.recycle();
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener clickListener) {
        this.clickListener = clickListener;
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (clickListener != null || openOnClickEnabled) {
            super.setOnClickListener(multipleClickListener);
        } else {
            super.setOnClickListener(null);
            setClickable(false);
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

    public static Class<? extends Activity> getCropImageActivityClass() {
        if (cropImageActivityClass == null) {
            cropImageActivityClass = CropImageActivity.class;
        }
        return cropImageActivityClass;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void cancelLoading() {
        if (!TextUtils.isEmpty(imageUrl)) {
            imageLoaded = false;
            imageUrl = null;
            getImageLoader().cancelDisplayTask(this);
            setImageDrawable(defaultImage);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void setDefaultImage(int defaultImageResId) {
        if (defaultImageResId > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setDefaultImage(getContext().getDrawable(defaultImageResId));
            } else {
                setDefaultImage(getContext().getResources().getDrawable(defaultImageResId));
            }
        } else {
            setDefaultImage(null);
        }
    }

    public void setDefaultImage(Drawable drawable) {
        this.defaultImage = drawable;
        if (defaultImage != null) {
            if (!imageLoaded) {
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
            if (!imageLoaded) {
                setImageDrawable(null);
            }
        }
    }

    public void reload() {
        if (!TextUtils.isEmpty(imageUrl)) {
            imageLoaded = false;
            String url = imageUrl;
            imageUrl = null;
            setImageUrl(url);
        }
    }

    @SuppressLint("NewApi")
    public void setImageUrl(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTransitionName(url);
        }

        if (!TextUtils.equals(url, imageUrl)) {
            cancelLoading();
            if (!TextUtils.isEmpty(url)) {
                imageUrl = url;
                if (options != null) {
                    getImageLoader().displayImage(url, imageAware, options, innerLoadingListener);
                } else {
                    getImageLoader().displayImage(url, imageAware, innerLoadingListener);
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
        this.listener = listener;
    }

    public void enableOpenOnClick() {
        openOnClickEnabled = true;
        setupClickListeners();
    }

    public void disableOpenOnClick() {
        openOnClickEnabled = false;
        setupClickListeners();
    }

}