package com.shaubert.remoteimageviewsample;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import com.shaubert.ui.remoteimageview.RemoteImageView;

public class ActivityMainHolder {
    private RemoteImageView image1;
    private Button load1Button;
    private RemoteImageView image2;
    private Button load2Button;
    private RemoteImageView image3;
    private ProgressBar image3Progress;
    private View image3Error;
    private Button load3Button;
    private RemoteImageView image4;
    private Button load4Button;

    public ActivityMainHolder(Activity activity) {
        image1 = (RemoteImageView) activity.findViewById(R.id.image_1);
        load1Button = (Button) activity.findViewById(R.id.load_1_button);
        image2 = (RemoteImageView) activity.findViewById(R.id.image_2);
        load2Button = (Button) activity.findViewById(R.id.load_2_button);
        image3 = (RemoteImageView) activity.findViewById(R.id.image_3);
        image3Progress = (ProgressBar) activity.findViewById(R.id.progress_image_3);
        image3Error = activity.findViewById(R.id.error_image_3);
        load3Button = (Button) activity.findViewById(R.id.load_3_button);
        image4 = (RemoteImageView) activity.findViewById(R.id.image_4);
        load4Button = (Button) activity.findViewById(R.id.load_4_button);
    }

    public RemoteImageView getImage2() {
        return image2;
    }

    public RemoteImageView getImage3() {
        return image3;
    }

    public Button getLoad4Button() {
        return load4Button;
    }

    public Button getLoad2Button() {
        return load2Button;
    }

    public RemoteImageView getImage4() {
        return image4;
    }

    public Button getLoad1Button() {
        return load1Button;
    }

    public RemoteImageView getImage1() {
        return image1;
    }

    public Button getLoad3Button() {
        return load3Button;
    }

    public ProgressBar getImage3Progress() {
        return image3Progress;
    }

    public View getImage3Error() {
        return image3Error;
    }
}
