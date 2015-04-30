package com.makeramen;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;

public class LayerRoundedDrawable extends AbstractRoundedDrawable {

    private LayerDrawable layerDrawable;
    private RoundedDrawable[] roundedDrawables;

    public LayerRoundedDrawable(LayerDrawable wrappedDrawable) {
        Drawable[] drawables = new Drawable[wrappedDrawable.getNumberOfLayers()];
        roundedDrawables = new RoundedDrawable[drawables.length];
        for (int i = 0; i < drawables.length; i++) {
            roundedDrawables[i] = RoundedDrawablesFactory.wrapDrawable(wrappedDrawable.getDrawable(i));
            drawables[i] = roundedDrawables[i].getDrawable();
        }
        layerDrawable = new LayerDrawable(drawables);
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
        return layerDrawable;
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
