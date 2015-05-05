package com.shaubert.remoteimageviewsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.shaubert.lifecycle.objects.dispatchers.support.LifecycleDispatcherActionBarActivity;
import com.shaubert.ui.remoteimageview.CompressionOptions;
import com.shaubert.ui.remoteimageview.CropOptions;
import com.shaubert.ui.remoteimageview.ImagePicker;
import com.shaubert.ui.remoteimageview.ImagePickerController;

import java.io.File;


public class MainActivity extends LifecycleDispatcherActionBarActivity {

    private ActivityMainHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewHolder = new ActivityMainHolder(this);
        viewHolder.getLoad1Button().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.getImage1().setDefaultImage(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
                viewHolder.getImage1().setImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.03.png");
            }
        });

        viewHolder.getLoad2Button().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.getImage2().setImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.04.png");
            }
        });

        ImagePicker imagePicker1 = new ImagePicker(this, "picker");
        imagePicker1.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png", true);
        imagePicker1.setupViews(viewHolder.getImage3(), viewHolder.getLoad3Button(), viewHolder.getImage3Progress(), viewHolder.getImage3Error());
        imagePicker1.setCompressionOptions(CompressionOptions.newBuilder()
                        .maxFileSize(1024 * 200)
                        .targetHeight(512)
                        .targetWidth(512)
                        .targetScaleType(ViewScaleType.CROP)
                        .build());
        attachToLifecycle(imagePicker1);

        ImagePicker imagePicker2 = new ImagePicker(this, "cropper");
        imagePicker2.setupViews(viewHolder.getImage4(), viewHolder.getLoad4Button(), null, null);
        imagePicker2.setCropCallback(new ImagePickerController.CropCallback() {
            @Override
            public boolean shouldCrop(@NonNull File imageFile) {
                return true;
            }

            @Override
            public void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder) {
                builder.minHeight(200)
                        .minWidth(200)
                        .aspectX(1)
                        .aspectY(1);
            }
        });
        imagePicker2.setPrivatePhotos(true);
        attachToLifecycle(imagePicker2);
    }

}
