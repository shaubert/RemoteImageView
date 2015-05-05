# RemoteImageView

ImageView to load images from urls. With optional foreground and round corners. Also you can preview images, take photos from camera, pick images from documents, and crop them.

## Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.remoteimageview:library:1.1'
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

    "app:defaultImage" //dummy image to show while loading
    "app:url" //image url to load
    "app:enableOpenOnClick" //open loaded image in openImageActivityClass on click

    "app:cornerRadius" //all corners radius
    "app:cornerRadiusLT" //left top corner radius
    "app:cornerRadiusRT" //right top corner radius
    "app:cornerRadiusRB" //right bottom corner radius
    "app:cornerRadiusLB" //left bottom corner radius
    "app:oval" //oval shape
    "app:borderWidth"
    "app:borderColor"
    "app:modifyBackground" //apply corners and borders to background and foreground    

    "app:dontPressWithParent" //do not copy parent's pressed state
    "app:foreground" //foreground (draw on top)
    "app:foregroundGravity"
    "app:foregroundInsidePadding"
    
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
            builder.minHeight(200)
                    .minWidth(200)
                    .aspectX(1)
                    .aspectY(1);
        }
    });
    
    //to hide took photos from gallery 
    imagePicker.setPrivatePhotos(true);
    
    //you need to attach it to `LifecycleDelegate` or manualy call all `distach*` methods of `LifecycleDispatcher`.
    attachToLifecycle(imagePicker);