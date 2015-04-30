package com.makeramen;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.*;
import android.os.Build;
import android.util.Log;

public class RoundedDrawablesFactory {

    public static final String TAG = RoundedDrawablesFactory.class.getSimpleName();

    public static RoundedDrawable wrapDrawable(Drawable drawable) {
        if (drawable != null) {
            if (drawable instanceof RoundedDrawable) {
                return (RoundedDrawable) drawable;
            } else if (drawable instanceof BitmapDrawable) {
                BitmapRoundedDrawable roundedDrawable = new BitmapRoundedDrawable(((BitmapDrawable) drawable).getBitmap());
                roundedDrawable.setBounds(drawable.getBounds());
                return roundedDrawable;
            } else if (drawable instanceof GradientDrawable) {
                return new GradientRoundedDrawable((GradientDrawable) drawable);
            } else if (drawable instanceof ColorDrawable) {
                GradientDrawable gradientDrawable = GradientRoundedDrawable.convert((ColorDrawable) drawable);
                if (gradientDrawable != null) {
                    return new GradientRoundedDrawable(gradientDrawable);
                } else {
                    return new UnsupportedRoundedDrawable(drawable);
                }
            } else if (drawable instanceof LayerDrawable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && drawable instanceof RippleDrawable) {
                    return new RippleRoundedDrawable((RippleDrawable) drawable);
                } else {
                    return new LayerRoundedDrawable((LayerDrawable) drawable);
                }
            } else if (drawable instanceof DrawableContainer) {
                return new RoundedDrawableContainer((DrawableContainer) drawable);
            } else {
                return new UnsupportedRoundedDrawable(drawable);
            }
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap;
        int width = Math.max(drawable.getIntrinsicWidth(), 1);
        int height = Math.max(drawable.getIntrinsicHeight(), 1);
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            Log.d(TAG, "failed to convert drawable", e);
            bitmap = null;
        }

        return bitmap;
    }
}
