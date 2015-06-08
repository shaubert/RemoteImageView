package com.makeramen;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RoundedDrawableContainer extends AbstractRoundedDrawable {

    private DrawableContainer drawableContainer;
    private Map<Drawable, RoundedDrawable> roundedDrawables = new HashMap<>();

    public RoundedDrawableContainer(DrawableContainer wrappedDrawable) {
        this.drawableContainer = wrappedDrawable;
    }

    @Override
    public RoundedDrawable setCornerRadii(float[] radii) {
        super.setCornerRadii(radii);
        for (RoundedDrawable roundedDrawable : roundedDrawables.values()) {
            roundedDrawable.setCornerRadii(radii);
        }
        return this;
    }

    @Override
    protected void updateBorderPaint() {
        for (RoundedDrawable roundedDrawable : roundedDrawables.values()) {
            roundedDrawable.setBorderColors(getBorderColors());
            roundedDrawable.setBorderWidth(getBorderWidth());
        }
    }

    @Override
    public RoundedDrawable setOval(boolean oval) {
        super.setOval(oval);
        for (RoundedDrawable roundedDrawable : roundedDrawables.values()) {
            roundedDrawable.setOval(oval);
        }
        return this;
    }

    @Override
    protected void onScaleTypeChanged() {
        ImageView.ScaleType scaleType = getScaleType();
        for (RoundedDrawable roundedDrawable : roundedDrawables.values()) {
            roundedDrawable.setScaleType(scaleType);
        }
    }

    @Override
    public Drawable getDrawable() {
        return this;
    }

    @Override
    public void draw(Canvas canvas) {
        Drawable current = drawableContainer.getCurrent();
        if (current == null) {
            return;
        }

        RoundedDrawable roundedDrawable = roundedDrawables.get(current);
        if (roundedDrawable == null) {
            roundedDrawable = RoundedDrawablesFactory.wrapDrawable(current);
            roundedDrawable.setOval(isOval());
            roundedDrawable.setScaleType(getScaleType());
            roundedDrawable.setBorderColors(getBorderColors());
            roundedDrawable.setBorderWidth(getBorderWidth());
            roundedDrawable.setCornerRadii(getCornerRadii());
            roundedDrawables.put(current, roundedDrawable);
        }
        Drawable drawable = roundedDrawable.getDrawable();
        if (drawable != current) {
            drawable.setState(current.getState());
            drawable.setLevel(current.getLevel());
            drawable.setBounds(current.getBounds());
        }
        drawable.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        drawableContainer.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        drawableContainer.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return drawableContainer.getOpacity();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        drawableContainer.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(Rect bounds) {
        drawableContainer.setBounds(bounds);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        drawableContainer.setChangingConfigurations(configs);
    }

    @Override
    public int getChangingConfigurations() {
        return drawableContainer.getChangingConfigurations();
    }

    @Override
    public void setDither(boolean dither) {
        drawableContainer.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        drawableContainer.setFilterBitmap(filter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Callback getCallback() {
        return drawableContainer.getCallback();
    }

    @Override
    public void invalidateSelf() {
        drawableContainer.invalidateSelf();
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        drawableContainer.scheduleSelf(what, when);
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        drawableContainer.unscheduleSelf(what);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getAlpha() {
        return drawableContainer.getAlpha();
    }

    @Override
    public void setColorFilter(int color, PorterDuff.Mode mode) {
        drawableContainer.setColorFilter(color, mode);
    }

    @Override
    public void clearColorFilter() {
        drawableContainer.clearColorFilter();
    }

    @Override
    public boolean isStateful() {
        return drawableContainer.isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        return drawableContainer.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return drawableContainer.getState();
    }

    @Override
    protected boolean onLevelChange(int level) {
        return drawableContainer.setLevel(level);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void jumpToCurrentState() {
        drawableContainer.jumpToCurrentState();
    }

    @Override
    public Drawable getCurrent() {
        return drawableContainer.getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return drawableContainer.setVisible(visible, restart);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setAutoMirrored(boolean mirrored) {
        drawableContainer.setAutoMirrored(mirrored);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean isAutoMirrored() {
        return drawableContainer.isAutoMirrored();
    }

    @Override
    public Region getTransparentRegion() {
        return drawableContainer.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return drawableContainer.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return drawableContainer.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return drawableContainer.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return drawableContainer.getMinimumHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return drawableContainer.getPadding(padding);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void getOutline(Outline outline) {
        drawableContainer.getOutline(outline);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setHotspot(float x, float y) {
        drawableContainer.setHotspot(x, y);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        drawableContainer.setHotspotBounds(left, top, right, bottom);
    }

    @Override
    public Drawable mutate() {
        drawableContainer = (StateListDrawable) drawableContainer.mutate();
        return super.mutate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTint(int tint) {
        drawableContainer.setTint(tint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintList(ColorStateList tint) {
        drawableContainer.setTintList(tint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        drawableContainer.setTintMode(tintMode);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ColorFilter getColorFilter() {
        return drawableContainer.getColorFilter();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void applyTheme(Resources.Theme t) {
        drawableContainer.applyTheme(t);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean canApplyTheme() {
        return drawableContainer.canApplyTheme();
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        drawableContainer.inflate(r, parser, attrs);
    }

    @Override
    public ConstantState getConstantState() {
        return drawableContainer.getConstantState();
    }
}
