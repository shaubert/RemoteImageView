# RemoteImageView

ImageView based on [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader) to load images, with foreground and optional round corners (fork of [RoundedImageView](https://github.com/vinc3m1/RoundedImageView)).

## Gradle
    
    repositories {
        maven{url "https://github.com/shaubert/maven-repo/raw/master/releases"}
    }
    dependencies {
        compile 'com.shaubert.ui.remoteimageview:library:1.0'
    }

## How-to

Setup `RemoteImageView`:
    
    new RemoteImageView.Setup()
        .imageLoader()
        .displayImageOptions()
        .openImageActivityClass() //Activity class to show fullscreen image
        .apply();
    
To load image into view call:
    
    remoteImageView.setDefaultImage(); //[optional] to set dummy image while loading
    remoteImageView.setImageUrl();
    
To open image fullscreen you need to add `ImageViewActivity` to your `AndroidManifest.xml` and call `remoteImageView.enableOpenOnClick()`.

Also check `attrs.xml` for supported xml attributes.