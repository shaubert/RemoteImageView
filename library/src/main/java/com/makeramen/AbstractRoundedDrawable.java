package com.makeramen;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public abstract class AbstractRoundedDrawable extends Drawable implements RoundedDrawable {

    public static final int DEFAULT_BORDER_COLOR = Color.TRANSPARENT;

    private float[] mCornerRadii = new float[] { 0, 0,
            0, 0,
            0, 0,
            0, 0,
    };
    private boolean mOval = false;
    private int mBorderWidth = 0;
    private ColorStateList mBorderColor = ColorStateList.valueOf(DEFAULT_BORDER_COLOR);
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

    @Override
    public RoundedDrawable setCornerRadii(float[] radii) {
        for (int i = 0; i < radii.length; i++) {
            mCornerRadii[i] = radii[i];
        }
        return this;
    }

    @Override
    public boolean hasRoundedCorners() {
        for (float rad : mCornerRadii) {
            if (rad > 0) return true;
        }
        return false;
    }

    @Override
    public float[] getCornerRadii() {
        return mCornerRadii;
    }

    @Override
    public boolean shouldChangeBorder() {
        return mBorderWidth > 0 && mBorderColor != null;
    }

    @Override
    public int getBorderWidth() {
        return mBorderWidth;
    }

    @Override
    public RoundedDrawable setBorderWidth(int width) {
        mBorderWidth = width;
        updateBorderPaint();
        return this;
    }

    protected abstract void updateBorderPaint();

    @Override
    public int getBorderColor() {
        return mBorderColor != null ? mBorderColor.getDefaultColor() : DEFAULT_BORDER_COLOR;
    }

    @Override
    public RoundedDrawable setBorderColor(int color) {
        return setBorderColors(ColorStateList.valueOf(color));
    }

    @Override
    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    @Override
    public RoundedDrawable setBorderColors(ColorStateList colors) {
        mBorderColor = colors != null ? colors : ColorStateList.valueOf(0);
        updateBorderPaint();
        return this;
    }

    @Override
    public boolean isOval() {
        return mOval;
    }

    @Override
    public RoundedDrawable setOval(boolean oval) {
        mOval = oval;
        return this;
    }

    @Override
    public ImageView.ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public RoundedDrawable setScaleType(ImageView.ScaleType scaleType) {
        if (scaleType == null) {
            scaleType = ImageView.ScaleType.FIT_CENTER;
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            onScaleTypeChanged();
        }
        return this;
    }

    protected abstract void onScaleTypeChanged();

    @Override
    public Drawable getDrawable() {
        return this;
    }

    @Override
    public boolean handleScaleType() {
        return false;
    }

}
