package com.makeramen;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.widget.ImageView;

public class RippleRoundedDrawable extends AbstractRoundedDrawable {

    private RippleDrawable rippleDrawable;
    private RoundedDrawable[] roundedDrawables;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RippleRoundedDrawable(RippleDrawable wrappedDrawable) {
        rippleDrawable = (RippleDrawable) wrappedDrawable.getConstantState().newDrawable(null);
        int numberOfLayers = wrappedDrawable.getNumberOfLayers();
        roundedDrawables = new RoundedDrawable[numberOfLayers];
        for (int i = 0; i < numberOfLayers; i++) {
            int id = wrappedDrawable.getId(i);
            Drawable drawable = wrappedDrawable.getDrawable(i);
            roundedDrawables[i] = RoundedDrawablesFactory.wrapDrawable(drawable);
            rippleDrawable.setDrawableByLayerId(id, roundedDrawables[i].getDrawable());
        }
    }

    @Override
    public RoundedDrawable setCornerRadii(float[] radii) {
        super.setCornerRadii(radii);
        for (RoundedDrawable roundedDrawable : roundedDrawables) {
            roundedDrawable.setCornerRadii(radii);
        }
        return this;
    }

    @Override
    protected void updateBorderPaint() {
        for (RoundedDrawable roundedDrawable : roundedDrawables) {
            roundedDrawable.setBorderColors(getBorderColors());
            roundedDrawable.setBorderWidth(getBorderWidth());
        }
    }

    @Override
    public RoundedDrawable setOval(boolean oval) {
        super.setOval(oval);
        for (RoundedDrawable roundedDrawable : roundedDrawables) {
            roundedDrawable.setOval(oval);
        }
        return this;
    }

    @Override
    protected void onScaleTypeChanged() {
        ImageView.ScaleType scaleType = getScaleType();
        for (RoundedDrawable roundedDrawable : roundedDrawables) {
            roundedDrawable.setScaleType(scaleType);
        }
    }

    @Override
    public Drawable getDrawable() {
        return rippleDrawable;
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
