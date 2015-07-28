package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.shaubert.lifecycle.objects.LifecycleBasedObject;
import com.shaubert.ui.dialogs.DialogManager;
import com.shaubert.ui.dialogs.ListDialogManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class ImagePickerController extends LifecycleBasedObject {

    public static final String TAG = ImagePickerController.class.getSimpleName();

    private static final String TEMP_IMAGE_OUTPUT_FILE_NAME = "temp_image_output_file_name";
    private static final String CURRENT_IMAGE_FILE = "current_image_file_extra";
    private static final String STATE = "state_extra";
    public static final String WAITING_FOR_ACTIVITY_RESULT = "waiting_for_activity_result";
    public static final String USER_PICKED_IMAGE = "user_picked_image";

    public static final String RIV_TEMP_DIR_NAME = "riv-temp";

    private final int REQUEST_CROP = 6709;
    private final int REQUEST_PICK = 9162;
    private final int REQUEST_TAKE_PHOTO = 9163;

    public static final int DEFAULT_MAX_FILE_SIZE = 300 * 1024;
    public static final int DEFAULT_TARGET_IMAGE_WIDTH = 1024;
    public static final int DEFAULT_TARGET_IMAGE_HEIGHT = 1024;
    public static final ViewScaleType DEFAULT_TARGET_SCALE_TYPE = ViewScaleType.CROP;

    private static SimpleDateFormat PUBLIC_FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("[^\\p{L}0-9_\\.]");

    private static String APP_LABEL;

    private Fragment fragment;
    private FragmentActivity fragmentActivity;

    private boolean userPickedImage;
    private File imageFile;
    private File tempImageOutput;

    private ImageListener listener;
    private Callback callback;
    private String tag;
    private CropCallback cropCallback;
    private boolean privatePhotos;

    private State state;
    private boolean readonly;

    private boolean waitingForActivityResult;

    private ListDialogManager editImageDialog;
    private ListDialogManager editNotLoadedImageDialog;
    private ListDialogManager addImageDialog;

    public ImagePickerController(@NonNull FragmentActivity fragmentActivity, @NonNull Callback callback, String tag) {
        this(null, fragmentActivity, callback, tag);
    }

    public ImagePickerController(@NonNull Fragment fragment, @NonNull Callback callback, String tag) {
        this(fragment, null, callback, tag);
    }

    private ImagePickerController(Fragment fragment, FragmentActivity activity, @NonNull Callback callback, String tag) {
        this.fragment = fragment;
        this.fragmentActivity = activity;
        this.callback = callback;
        this.tag = tag;

        state = State.EMPTY;

        Context context = getActivity();
        final EditAction[] editActions = new EditAction[] {
            new EditAction(context.getString(R.string.riv_image_edit_dialog_option_open), new Runnable() {
                @Override
                public void run() {
                    showImageFullScreen();
                }
            }),
            new EditAction(context.getString(R.string.riv_image_edit_dialog_option_take_photo), new Runnable() {
                @Override
                public void run() {
                    onTakePhotoClicked();
                }
            }),
            new EditAction(context.getString(R.string.riv_image_edit_dialog_option_choose_picture), new Runnable() {
                @Override
                public void run() {
                    onPickPictureClicked();
                }
            }),
            new EditAction(context.getString(R.string.riv_image_edit_dialog_option_remove), new Runnable() {
                @Override
                public void run() {
                    onRemoveImageClicked();
                }
            })
        };

        String[] names = new String[editActions.length];
        int i = 0;
        for (EditAction editAction : editActions) {
            names[i++] = editAction.name;
        }
        editImageDialog = createImageOptionsDialog(names, editActions, "edit-image-dialog");
        editNotLoadedImageDialog = createImageOptionsDialog(
                new String[] {names[1], names[2], names[3]},
                new EditAction[] {editActions[1], editActions[2], editActions[3]},
                "edit-not-loaded-image-dialog"
        );
        addImageDialog = createImageOptionsDialog(
                new String[] {names[1], names[2]},
                new EditAction[] {editActions[1], editActions[2]},
                "add-image-dialog"
        );
    }

    @Override
    protected String getBundleTag() {
        return super.getBundleTag() + tag;
    }

    public FragmentActivity getActivity() {
        if (fragmentActivity != null) {
            return fragmentActivity;
        } else {
            return fragment.getActivity();
        }
    }

    public FragmentManager getFragmentManager() {
        if (fragment != null) {
            return fragment.getFragmentManager();
        } else {
            return fragmentActivity.getSupportFragmentManager();
        }
    }

    private ListDialogManager createImageOptionsDialog(String[] labels, final EditAction[] actions, String tag) {
        final ListDialogManager dialog = new ListDialogManager(getFragmentManager(), tag);
        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.riv_select_dialog_item, labels);
        dialog.setListAdapter(dialogAdapter);
        dialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                actions[which].action.run();
                dialog.hideDialog();
            }
        });
        dialog.setCancellable(true);

        return dialog;
    }

    public File generateTempFileOrShowError() {
        return generateTempFileOrShowError(getActivity());
    }

    public File generatePublicTempFileOrShowError() {
        return generatePublicTempFileOrShowError(getActivity());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generateTempFileOrShowError(Context context) {
        File file = generateTempFile(context);
        if (file == null) {
            showStorageError(context);
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generatePublicTempFileOrShowError(Context context) {
        File file = generatePublicTempFile(context);
        if (file == null) {
            showStorageError(context);
        }
        return file;
    }

    public File generateTempFile() {
        return generateTempFile(getActivity());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generateTempFile(Context context) {
        File tempRoot = getTempRoot(context);
        if (tempRoot != null) {
            return new File(tempRoot, UUID.randomUUID().toString());
        } else {
            return null;
        }
    }

    public static File getTempRoot(Context context) {
        File photosDir = StorageUtils.getCacheDirectory(context);
        if (photosDir != null) {
            File innerDir = new File(photosDir, RIV_TEMP_DIR_NAME);
            if (innerDir.exists() || innerDir.mkdirs()) {
                return innerDir;
            }
        }
        return null;
    }

    public File generatePublicTempFile() {
        return generatePublicTempFile(getActivity());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File generatePublicTempFile(Context context) {
        File photosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (photosDir.exists()) {
            String label = getAppLabel(context);
            if (TextUtils.isEmpty(label)) {
                label = "Camera";
            }
            File appPhotosDir = new File(photosDir, label);
            if (!appPhotosDir.exists() && !appPhotosDir.mkdirs()) {
                return null;
            }

            String filename = "IMG_" + PUBLIC_FILE_NAME_FORMAT.format(new Date());
            File resultFile;
            int i = 1;
            while (true) {
                resultFile = new File(appPhotosDir, filename + (i == 1 ? "" : ("_" + i)) + ".jpg");
                if (!resultFile.exists()) break;
                i++;
            }

            return resultFile;
        } else {
            return null;
        }
    }

    private static String getAppLabel(Context context) {
        if (!TextUtils.isEmpty(APP_LABEL)) {
            return APP_LABEL;
        }

        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            CharSequence label = applicationInfo.nonLocalizedLabel;
            if (label != null) {
                String name = FILE_NAME_PATTERN.matcher(label).replaceAll("");
                if (TextUtils.equals(".", name) || TextUtils.equals("..", name)) {
                    return null;
                }
                return APP_LABEL = name;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean isTempFile(File file) {
        return isTempFile(getActivity(), file);
    }

    public static boolean isTempFile(Context context, File file) {
        File tempRoot = getTempRoot(context);
        if (file == null || tempRoot == null) return false;
        return file.getAbsolutePath().contains(tempRoot.getAbsolutePath());
    }

    public static boolean isPublicFile(File file) {
        File photosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (photosDir == null || file == null) return false;
        return file.getAbsolutePath().contains(photosDir.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle stateBundle) {
        String path = stateBundle.getString(TEMP_IMAGE_OUTPUT_FILE_NAME);
        if (!TextUtils.isEmpty(path)) {
            tempImageOutput = new File(path);
        } else {
            tempImageOutput = null;
        }
        waitingForActivityResult = stateBundle.getBoolean(WAITING_FOR_ACTIVITY_RESULT, false);

        state = Enums.fromBundle(State.class, stateBundle, STATE);

        String imagePath = stateBundle.getString(CURRENT_IMAGE_FILE);
        if (!TextUtils.isEmpty(imagePath)) {
            imageFile = new File(imagePath);
            userPickedImage = stateBundle.getBoolean(USER_PICKED_IMAGE, false);
            callback.onImageFileSet(imageFile);
        } else {
            imageFile = null;
            userPickedImage = false;
            callback.onImageFileSet(null);
        }

        setState(state);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (tempImageOutput != null) {
            outState.putString(TEMP_IMAGE_OUTPUT_FILE_NAME, tempImageOutput.getAbsolutePath());
        }
        Enums.toBundle(state, outState, STATE);
        if (imageFile != null) {
            outState.putString(CURRENT_IMAGE_FILE, imageFile.getAbsolutePath());
            outState.putBoolean(USER_PICKED_IMAGE, userPickedImage);
        }
        outState.putBoolean(WAITING_FOR_ACTIVITY_RESULT, waitingForActivityResult);
    }

    public SimpleImageLoadingListener createSimpleImageLoadingListener() {
        return new SimpleImageLoadingListener() {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                ImagePickerController.this.onLoadingFailed(imageUri);
            }

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                ImagePickerController.this.onLoadingStarted(imageUri);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ImagePickerController.this.onLoadingComplete(imageUri);
            }
        };
    }

    public void onLoadingFailed(String imageUri) {
        if (imageFile == null) return;
        if (!ImageDownloader.Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        if (state == State.PROCESSING) {
            setState(State.EMPTY);
            showLoadingError();
        } else {
            setState(State.ERROR);
        }
    }

    public void onLoadingStarted(String imageUri) {
        if (imageFile == null) return;
        if (!ImageDownloader.Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        if (state != State.PROCESSING) {
            setState(State.LOADING);
        }
    }

    public void onLoadingComplete(String imageUri) {
        if (imageFile == null) return;
        if (!ImageDownloader.Scheme.FILE.wrap(imageFile.getAbsolutePath()).equals(imageUri)) {
            return;
        }

        State oldState = state;
        setState(State.WITH_IMAGE);
        if (listener != null) {
            listener.onImageLoaded(imageUri);
            if (oldState == State.PROCESSING) {
                listener.onImageTaken(getImageFile());
            }
        }
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
        setState(state);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setImageListener(ImageListener listener) {
        this.listener = listener;
    }

    public void setCropCallback(CropCallback cropCallback) {
        this.cropCallback = cropCallback;
    }

    public boolean isPrivatePhotos() {
        return privatePhotos;
    }

    public void setPrivatePhotos(boolean privatePhotos) {
        this.privatePhotos = privatePhotos;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeTempFiles() {
        if (isTempFile(tempImageOutput)) {
            tempImageOutput.delete();
        }
        tempImageOutput = null;

        if (isTempFile(imageFile)) {
            imageFile.delete();
            imageFile = null;
            userPickedImage = false;
            setState(State.EMPTY);
            callback.onImageFileSet(null);
        }
    }

    public File getImageFile() {
        if (state == State.WITH_IMAGE) {
            return imageFile;
        } else {
            return null;
        }
    }

    public void setImageFile(File imageFile) {
        if (this.imageFile == null || !this.imageFile.equals(imageFile)) {
            removeTempFiles();
        }
        if (this.imageFile == imageFile || (this.imageFile != null && this.imageFile.equals(imageFile))) {
            return;
        }

        this.imageFile = imageFile;
        userPickedImage = false;
        if (imageFile == null) {
            setState(State.EMPTY);
        } else {
            setState(State.LOADING);
            callback.onImageFileSet(imageFile);
        }
    }

    public Uri getImageUri() {
        File imageFile = getImageFile();
        if (imageFile != null) {
            return Uri.fromFile(imageFile);
        } else {
            return null;
        }
    }

    public String getImageUrl() {
        File imageFile = getImageFile();
        if (imageFile != null) {
            return ImageDownloader.Scheme.FILE.wrap(imageFile.getAbsolutePath());
        } else {
            return null;
        }
    }

    public boolean hasImage() {
        return state == State.WITH_IMAGE;
    }

    public boolean hasUserImage() {
        if (state == State.WITH_IMAGE) {
            return userPickedImage;
        } else {
            return false;
        }
    }

    public void showImageFullScreen() {
        final File imageFile = getImageFile();
        if (state == State.WITH_IMAGE && imageFile != null) {
            ImageView sharedImageView = callback.getImageView();
            ImageViewActivity.start(getActivity(), sharedImageView, getImageUrl());
        }
    }

    public void showEditDialog() {
        if (state == State.WITH_IMAGE) {
            showDialogIfNotReadonly(editImageDialog);
        } else {
            showDialogIfNotReadonly(editNotLoadedImageDialog);
        }
    }

    public void showAddDialog() {
        showDialogIfNotReadonly(addImageDialog);
    }

    private void showDialogIfNotReadonly(DialogManager dialog) {
        if (!readonly) {
            dialog.showDialog();
        }
    }

    public void onTakePhotoClicked() {
        Intent chooser = getTakePhotoIntentOrShowError();
        if (chooser != null) {
            startActivityForResult(chooser, REQUEST_TAKE_PHOTO);
        }
    }

    public void onPickPictureClicked() {
        startActivityForResult(getPickImageIntent(getActivity()), REQUEST_PICK);
    }

    private void startActivityForResult(Intent intent, int requestCode) {
        waitingForActivityResult = true;
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            fragmentActivity.startActivityForResult(intent, requestCode);
        }
    }

    public void retryLoading() {
        if (state == State.ERROR && imageFile != null) {
            setState(State.LOADING);
            callback.onImageFileSet(imageFile);
        }
    }

    public void onRemoveImageClicked() {
        clear();
        if (listener != null) {
            listener.onImageRemoved();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clear() {
        removeTempFiles();
        if (imageFile != null) {
            imageFile = null;
            userPickedImage = false;
            setState(State.EMPTY);
            callback.onImageFileSet(null);
        }
    }

    public void setState(State state) {
        this.state = state;
        callback.onStateChanged(state);
    }

    public State getState() {
        return state;
    }

    public static Intent getPickImageIntent(Context context) {
        Intent intent = pickImageIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return intent;
        } else {
            return Intent.createChooser(intent, context.getString(R.string.riv_take_picture_chooser_title));
        }
    }

    private Intent getTakePhotoIntentOrShowError() {
        if (privatePhotos) {
            tempImageOutput = generateTempFileOrShowError();
        } else {
            tempImageOutput = generatePublicTempFileOrShowError();
        }

        if (tempImageOutput != null) {
            return takePhotoIntent(tempImageOutput);
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Intent pickImageIntent() {
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("image/*")
                    .setFlags(flags);
        } else {
            return new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setFlags(flags);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static Intent takePhotoIntent(@NonNull File output) {
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .setFlags(flags);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!waitingForActivityResult) {
            return;
        }

        waitingForActivityResult = false;
        if (resultCode != Activity.RESULT_OK) {
            if (requestCode == REQUEST_CROP) {
                if (isTempFile(tempImageOutput) && !tempImageOutput.equals(imageFile)) {
                    //noinspection ResultOfMethodCallIgnored
                    tempImageOutput.delete();
                    tempImageOutput = null;
                }
            }
            return;
        }

        switch (requestCode) {
            case REQUEST_PICK:
                handleImagePick(data);
                break;
            case REQUEST_TAKE_PHOTO:
                handleTakePhoto(data);
                break;
            case REQUEST_CROP:
                handleImageCrop(data);
                break;
        }
    }

    private void handleTakePhoto(Intent data) {
        Uri dataUri = data != null ? data.getData() : null;
        final File imageFile;
        if (dataUri != null) {
            imageFile = getFileFromIntentResult(dataUri, privatePhotos);
        } else {
            imageFile = tempImageOutput;
        }
        if (!privatePhotos && imageFile != null) {
            MediaScannerConnection.scanFile(getActivity(),
                    new String[] { imageFile.getAbsolutePath() }, new String[] {"image/jpeg"}, null);
        }
        processResultImage(imageFile);
    }

    private File getFileFromIntentResult(Uri resultUri, boolean copyAndRemoveSource) {
        if (resultUri == null) return null;

        String path = Files.getPath(getActivity(), resultUri);
        if (TextUtils.isEmpty(path)) {
            showFileReadingError();
        } else {
            File source = new File(path);
            if (copyAndRemoveSource && !isTempFile(source)) {
                File output = generateTempFileOrShowError();
                if (output != null) {
                    if (Files.copy(source, output)) {
                        //noinspection ResultOfMethodCallIgnored
                        source.delete();
                        return output;
                    } else {
                        showStorageError();
                    }
                }
            } else {
                return source;
            }
        }
        return null;
    }

    private void handleImagePick(Intent data) {
        Uri dataUri = data != null ? data.getData() : null;
        File imageFile = getFileFromIntentResult(dataUri, false);
        processResultImage(imageFile);
    }

    private void processResultImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            showProcessingError();
            return;
        }

        if (cropCallback != null && cropCallback.shouldCrop(imageFile)) {
            File cropOutput = generateTempFileOrShowError();
            if (cropOutput == null) {
                return;
            }

            CropOptions.Builder builder = CropOptions.newBuilder();
            cropCallback.setupCropOptions(imageFile, builder);
            builder.inUri(Uri.fromFile(imageFile));
            builder.outUri(Uri.fromFile(cropOutput));
            CropOptions cropOptions = builder.build();

            startCrop(cropOptions);
            return;
        }

        processImage(imageFile, isTempFile(imageFile));
    }

    private void processImage(File imageFile, boolean removeInputAfter) {
        CompressionOptions compressionOptions = callback.getCompressionOptions(imageFile);
        if (compressionOptions != null
                && compressionOptions.maxFileSize > 0
                && imageFile.length() > compressionOptions.maxFileSize) {
            File tempImageScaled = generateTempFileOrShowError();
            if (tempImageScaled != null) {
                setState(State.PROCESSING);
                compressImageAsync(imageFile, tempImageScaled, compressionOptions, removeInputAfter);
            }
        } else {
            setState(State.PROCESSING);
            onImageProcessingFinished(imageFile);
        }
    }

    private void startCrop(CropOptions cropOptions) {
        Intent intent = new Intent(getActivity(), RemoteImageView.getCropImageActivityClass());
        intent.putExtras(CropImageActivity.buildCropImageExtras(cropOptions));
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void handleImageCrop(Intent data) {
        CropOptions cropOptions = CropImageActivity.getCropOptions(data);
        if (cropOptions == null
                || cropOptions.getOutUri() == null) {
            showProcessingError();
            return;
        }

        String inPath = Files.getPath(getActivity(), cropOptions.getInUri());
        if (!TextUtils.isEmpty(inPath)) {
            File inFile = new File(inPath);
            if (isTempFile(inFile)) {
                //noinspection ResultOfMethodCallIgnored
                inFile.delete();
            }
        }

        String outPath = Files.getPath(getActivity(), cropOptions.getOutUri());
        if (!TextUtils.isEmpty(outPath)) {
            File croppedImageFile = new File(outPath);
            processImage(croppedImageFile, true);
        } else {
            showProcessingError();
        }
    }

    private void compressImageAsync(final File input, final File output,
                                    final CompressionOptions compressionOptions,
                                    final boolean removeInputAfter) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                if (compressImage(input, output, compressionOptions)) {
                    if (removeInputAfter) {
                        //noinspection ResultOfMethodCallIgnored
                        input.delete();
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    if (result) {
                        onImageProcessingFinished(output);
                    } else {
                        setState(State.EMPTY);
                        showProcessingError(activity);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onImageProcessingFinished(File imageFile) {
        if (isTempFile(this.imageFile)) {
            //noinspection ResultOfMethodCallIgnored
            this.imageFile.delete();
        }
        this.imageFile = imageFile;
        userPickedImage = true;
        callback.onImageFileSet(imageFile);
    }

    public static boolean compressImage(File source, File destination, CompressionOptions compressionOptions) {
        return compressImage(source, destination, compressionOptions.targetWidth,
                compressionOptions.targetHeight, compressionOptions.maxFileSize, compressionOptions.targetScaleType);
    }

    public static boolean compressImage(File source, File destination,
                                        int targetWidth, int targetHeight, long maxSize, ViewScaleType targetScaleType) {
        InputStream boundsStream = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            boundsStream = new FileInputStream(source);
            BitmapFactory.decodeStream(boundsStream, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            if (w > 0 && h > 0) {
                options.inSampleSize = ImageSizeUtils.computeImageSampleSize(new ImageSize(w, h),
                        new ImageSize(targetWidth, targetHeight), targetScaleType, true);
            }
            options.inJustDecodeBounds = false;
            is = new FileInputStream(source);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            int quality = 100;
            long currentSize;
            do {
                quality -= 10;
                fos = new FileOutputStream(destination);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.close();
                currentSize = destination.length();
            } while (quality > 50
                    && currentSize > maxSize
                    && maxSize > 0);
            bitmap.recycle();
            copyExif(source, destination);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "failed to create compressed image", e);
        } finally {
            if (boundsStream != null) {
                try {
                    boundsStream.close();
                } catch (IOException ignored) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    private static void copyExif(File source, File destination) {
        try {
            ExifInterface oldExif = new ExifInterface(source.getAbsolutePath());
            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);
            ExifInterface newExif = new ExifInterface(destination.getAbsolutePath());
            if (exifOrientation != null) {
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
            }
            newExif.saveAttributes();
        } catch (Exception ex) {
            Log.d(TAG, "failed to copy exif", ex);
        }
    }

    public void showProcessingError() {
        showProcessingError(getActivity());
    }

    public static void showProcessingError(Context context) {
        Toast.makeText(context, R.string.riv_take_photo_processing_error, Toast.LENGTH_SHORT).show();
    }

    public void showLoadingError() {
        showLoadingError(getActivity());
    }

    public static void showLoadingError(Context context) {
        Toast.makeText(context, R.string.riv_take_photo_loading_error, Toast.LENGTH_SHORT).show();
    }

    public void showStorageError() {
        showStorageError(getActivity());
    }

    public static void showStorageError(Context context) {
        Toast.makeText(context, R.string.riv_storage_not_available_message, Toast.LENGTH_SHORT).show();
    }

    public void showFileReadingError() {
        showFileReadingError(getActivity());
    }

    public static void showFileReadingError(Context context) {
        Toast.makeText(context, R.string.riv_unable_to_read_file_message, Toast.LENGTH_SHORT).show();
    }

    public interface ImageListener {
        void onImageTaken(File imageFile);
        void onImageLoaded(String imageUri);
        void onImageRemoved();
    }

    public interface Callback {
        void onImageFileSet(@Nullable File imageFile);
        void onStateChanged(State state);
        ImageView getImageView();
        CompressionOptions getCompressionOptions(@NonNull File imageFile);
    }

    public interface CropCallback {
        boolean shouldCrop(@NonNull File imageFile);
        void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder);
    }

    private static class EditAction {
        String name;
        Runnable action;

        private EditAction(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }

    public enum State {
        EMPTY,
        PROCESSING,
        LOADING,
        WITH_IMAGE,
        ERROR,
    }

}