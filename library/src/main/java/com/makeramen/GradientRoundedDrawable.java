package com.makeramen;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import java.lang.reflect.Field;

public class GradientRoundedDrawable extends AbstractRoundedDrawable {

    private static Field mUseColorField;

    private GradientDrawable gradientDrawable;

    @SuppressLint("NewApi")
    public static GradientDrawable convert(ColorDrawable colorDrawable) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            gradientDrawable.setColor(colorDrawable.getColor());
        } else {
            ConstantState constantState = colorDrawable.getConstantState();
            if (constantState != null) {
                try {
                    if (mUseColorField == null) {
                        mUseColorField = constantState.getClass().getDeclaredField("mUseColor");
                        mUseColorField.setAccessible(true);
                    }
                    int color = (Integer) mUseColorField.get(constantState);
                    gradientDrawable.setColor(color);
                } catch (Exception ignored) {
                }
            }
        }
        return gradientDrawable;
    }

    public GradientRoundedDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
    }

    @Override
    public RoundedDrawable setCornerRadii(float[] radii) {
        super.setCornerRadii(radii);
        gradientDrawable.setCornerRadii(radii);
        return this;
    }

    @Override
    protected void updateBorderPaint() {
        if (shouldChangeBorder()) {
            gradientDrawable.setStroke(getBorderWidth(),
                    getBorderColors().getColorForState(gradientDrawable.getState(), getBorderColor()));
        }
    }

    @Override
    public RoundedDrawable setOval(boolean oval) {
        super.setOval(oval);
        gradientDrawable.setShape(oval ? GradientDrawable.OVAL : GradientDrawable.RECTANGLE);
        return this;
    }

    @Override
    protected void onScaleTypeChanged() {
    }

    @Override
    public Drawable getDrawable() {
        return gradientDrawable;
    }

    @Override
    public void draw(Canvas canvas) {
        throw new UnsupportedOperationException("use getDrawable()");
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("use getDrawable()");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("use getDrawable()");
    }

    @Override
    public int getOpacity() {
        throw new UnsupportedOperationException("use getDrawable()");
    }

}