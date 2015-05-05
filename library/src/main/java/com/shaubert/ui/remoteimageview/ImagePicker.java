package com.shaubert.ui.remoteimageview;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.shaubert.lifecycle.objects.LifecycleObjectsGroup;

import java.io.File;

public class ImagePicker extends LifecycleObjectsGroup implements ImagePickerController.Callback {

    private RemoteImageView imageView;
    private View takePictureButton;
    private View progressView;
    private View errorView;

    private ImagePickerController controller;
    private String imageUrl;
    private String defaultImageUrl;
    private CompressionOptions compressionOptions;
    private String tag;

    public ImagePicker(@NonNull Fragment fragment, @NonNull String tag) {
        this.tag = tag;
        controller = new ImagePickerController(fragment, this);
        attachToLifecycle(controller);
    }

    public ImagePicker(@NonNull FragmentActivity fragmentActivity, @NonNull String tag) {
        this.tag = tag;
        controller = new ImagePickerController(fragmentActivity, this);
        attachToLifecycle(controller);
    }

    public void setupViews(@NonNull RemoteImageView imageView, @Nullable View takePictureButton, @Nullable View progressView, @Nullable View errorView) {
        if (this.takePictureButton != null) {
            this.takePictureButton.setOnClickListener(null);
        }
        if (this.errorView != null) {
            this.errorView.setOnClickListener(null);
        }

        this.takePictureButton = takePictureButton;
        this.progressView = progressView;
        this.errorView = errorView;

        setImageView(imageView);
        if (takePictureButton != null) {
            takePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImagePickerController imagePickerController = getController();
                    if (imagePickerController.getState() == ImagePickerController.State.EMPTY) {
                        imagePickerController.showAddDialog();
                    } else {
                        if (currentImageIsDefault()) {
                            imagePickerController.showAddDialog();
                        } else {
                            imagePickerController.showEditDialog();
                        }
                    }
                }
            });
            refreshTakePictureButtonVisibility();
        }
        if (errorView != null) {
            errorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getController().retryLoading();
                }
            });
        }
    }

    private void refreshTakePictureButtonVisibility() {
        if (takePictureButton != null) {
            takePictureButton.setVisibility(isReadonly() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onStateChanged(ImagePickerController.State state) {
        switch (state) {
            case EMPTY:
            case WITH_IMAGE:
                if (progressView != null) progressView.setVisibility(View.GONE);
                if (errorView != null) errorView.setVisibility(View.GONE);
                break;
            case LOADING:
            case PROCESSING:
                if (progressView != null) progressView.setVisibility(View.VISIBLE);
                if (errorView != null) errorView.setVisibility(View.GONE);
                break;
            case ERROR:
                if (errorView != null) errorView.setVisibility(View.VISIBLE);
                if (progressView != null) progressView.setVisibility(View.GONE);
                break;
        }
        refreshTakePictureButtonVisibility();
    }

    private void setImageView(final RemoteImageView imageView) {
        if (this.imageView != null) {
            this.imageView.setOnClickListener(null);
            this.imageView.setListener(null);
        }
        this.imageView = imageView;
        imageView.disableOpenOnClick();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller.getState() == ImagePickerController.State.WITH_IMAGE) {
                    if (controller.isReadonly()) {
                        imageView.getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                controller.showImageFullScreen();
                            }
                        }, 250);
                    } else {
                        controller.showEditDialog();
                    }
                } else if (!controller.isReadonly()
                        && controller.getState() == ImagePickerController.State.EMPTY) {
                    controller.showAddDialog();
                }
            }
        });
        imageView.setListener(controller.createSimpleImageLoadingListener());
        if (hasDefaultImageUrl()) {
            loadImage(defaultImageUrl);
        }
        onStateChanged(controller.getState());
    }

    public void setDefaultImageUrl(String imageUrl, boolean startLoad) {
        defaultImageUrl = imageUrl;
        if (startLoad) {
            loadImage(imageUrl);
        }
    }

    @Override
    public void onImageFileSet(File imageFile) {
        String imageUrl = imageFile != null ? ImageDownloader.Scheme.FILE.wrap(imageFile.getPath()) : null;
        if (imageUrl == null
                || imageUrl.equals(defaultImageUrl)) {
            this.imageUrl = null;
            loadImage(defaultImageUrl);
        } else {
            this.imageUrl = imageUrl;
            loadImage(imageUrl);
        }
    }

    private void loadImage(String imageUrl) {
        if (imageView != null) {
            imageView.setImageUrl(imageUrl);
        }
    }

    public boolean currentImageIsDefault() {
        return !hasImage() && hasDefaultImageUrl();
    }

    public void clear() {
        imageUrl = null;
        controller.clear();
        if (defaultImageUrl != null) {
            loadImage(defaultImageUrl);
        }
    }

    @Override
    protected void onPause(boolean isFinishing) {
        super.onPause(isFinishing);
        if (isFinishing) controller.removeTempFiles();
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    protected ImagePickerController getController() {
        return controller;
    }

    public void setPrivatePhotos(boolean privatePhotos) {
        controller.setPrivatePhotos(privatePhotos);
    }

    public boolean isPrivatePhotos() {
        return controller.isPrivatePhotos();
    }

    public void setCropCallback(ImagePickerController.CropCallback cropCallback) {
        controller.setCropCallback(cropCallback);
    }

    public void setCompressionOptions(CompressionOptions compressionOptions) {
        this.compressionOptions = compressionOptions;
    }

    public CompressionOptions getCompressionOptions() {
        return compressionOptions;
    }

    public File getImageFile() {
        return controller.getImageFile();
    }

    public Uri getImageUri() {
        return controller.getImageUri();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasDefaultImageUrl() {
        return !TextUtils.isEmpty(defaultImageUrl);
    }

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null && controller.hasImage();
    }

    public boolean hasUserImage() {
        return controller.hasUserImage();
    }

    public void setReadonly(boolean readonly) {
        controller.setReadonly(readonly);
        refreshTakePictureButtonVisibility();
    }

    public boolean isReadonly() {
        return controller.isReadonly();
    }

    public void setListener(ImagePickerController.ImageListener listener) {
        controller.setImageListener(listener);
    }

    public void setDefaultImageRes(int imageRes) {
        imageView.setDefaultImage(imageRes);
    }

    @Override
    public RemoteImageView getImageView() {
        return imageView;
    }

    @Override
    public CompressionOptions getCompressionOptions(@NonNull File imageFile) {
        return compressionOptions;
    }
}