package com.makeramen;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public interface RoundedDrawable {
    float[] getCornerRadii();

    RoundedDrawable setCornerRadii(float[] radius);

    boolean hasRoundedCorners();

    boolean shouldChangeBorder();

    int getBorderWidth();

    RoundedDrawable setBorderWidth(int width);

    int getBorderColor();

    RoundedDrawable setBorderColor(int color);

    ColorStateList getBorderColors();

    RoundedDrawable setBorderColors(ColorStateList colors);

    boolean isOval();

    RoundedDrawable setOval(boolean oval);

    boolean handleScaleType();

    ImageView.ScaleType getScaleType();

    RoundedDrawable setScaleType(ImageView.ScaleType scaleType);

    Drawable getDrawable();

}
