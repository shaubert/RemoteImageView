package com.makeramen;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class UnsupportedRoundedDrawable extends AbstractRoundedDrawable {

    private Drawable drawable;

    public UnsupportedRoundedDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    protected void updateBorderPaint() {
    }

    @Override
    protected void onScaleTypeChanged() {
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
