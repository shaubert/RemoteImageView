# RemoteImageView

ImageView to load images from urls. With optional foreground and round corners. Also you can preview images, take photos from camera, pick images from documents, and crop them.

## Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.remoteimageview:library:1.1.2'
    }

## References
  *  [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)
  *  [RoundedImageView](https://github.com/vinc3m1/RoundedImageView)
  *  [Cropper](https://github.com/edmodo/cropper)
  *  [PhotoView](https://github.com/chrisbanes/PhotoView)

## How-to

Setup `RemoteImageView`:
    
    new RemoteImageView.Setup()
        .imageLoader() //[Optional]
        .displayImageOptions() //[Optional]
        .openImageActivityClass() //[Optional] Activity class to show fullscreen image
        .cropImageActivityClass() //[Optional] Activity class to show fullscreen image
        .apply();
    
To load image into view call:
    
    remoteImageView.setDefaultImage(); //[optional] to set dummy image while loading
    remoteImageView.setImageUrl();
    
To open image fullscreen you need to add `ImageViewActivity` to your `AndroidManifest.xml` and call `remoteImageView.enableOpenOnClick()`.

List of supported xml attributes:

    "app:riv_defaultImage" //dummy image to show while loading
    "app:riv_url" //image url to load
    "app:riv_enableOpenOnClick" //open loaded image in openImageActivityClass on click

    "app:riv_cornerRadius" //all corners radius
    "app:riv_cornerRadiusLT" //left top corner radius
    "app:riv_cornerRadiusRT" //right top corner radius
    "app:riv_cornerRadiusRB" //right bottom corner radius
    "app:riv_cornerRadiusLB" //left bottom corner radius
    "app:riv_oval" //oval shape
    "app:riv_borderWidth"
    "app:riv_borderColor"
    "app:riv_modifyBackground" //apply corners and borders to background and foreground    

    "app:riv_dontPressWithParent" //do not copy parent's pressed state
    "app:riv_foreground" //foreground (draw on top)
    "app:riv_foregroundGravity"
    "app:riv_foregroundInsidePadding"
    
To pick/crop image use `ImagePicker`:

    ImagePicker imagePicker = new ImagePicker(this, "picker");    
    imagePicker.setDefaultImageUrl("http://sipi.usc.edu/database/preview/misc/4.2.05.png", true);
    
    imagePicker.setupViews(remoteImageView, //required
          pickButton, //optional
          loadingProgress, //optional
          errorView); //optional
          
    //optional comperssion of picked image
    imagePicker.setCompressionOptions(CompressionOptions.newBuilder()
                    .maxFileSize(1024 * 200)
                    .targetHeight(512)
                    .targetWidth(512)
                    .targetScaleType(ViewScaleType.CROP)
                    .build());
                    
    //optional callback to enable cropping
    imagePicker.setCropCallback(new ImagePickerController.CropCallback() {
        @Override
        public boolean shouldCrop(@NonNull File imageFile) {
            return true;
        }

        @Override
        public void setupCropOptions(@NonNull File imageFile, @NonNull CropOptions.Builder builder) {
            builder.minHeight(200) //if image frame smaller error message will be shown
                    .minWidth(200) //if image frame smaller error message will be shown
                    .maxHeight(400) //if image frame bigger image will be downscaled
                    .maxWidth(400) //if image frame bigger image will be downscaled
                    .aspectX(1) //image frame aspect ratio
                    .aspectY(1); //image frame aspect ratio
        }
    });
    
    //to hide took photos from gallery 
    imagePicker.setPrivatePhotos(true);
    
    //you need to attach it to `LifecycleDelegate` or manualy call all `distach*` methods of `LifecycleDispatcher`.
    attachToLifecycle(imagePicker);