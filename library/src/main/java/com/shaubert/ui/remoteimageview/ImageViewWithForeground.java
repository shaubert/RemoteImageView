package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

public class ImageViewWithForeground extends ImageView {

    private Drawable mForeground;

    private int mForegroundGravity = Gravity.FILL;
    private boolean mForegroundInPadding = true;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();

    private boolean mForegroundBoundsChanged = true;

    private boolean dontPressWithParent = false;

    public ImageViewWithForeground(Context context) {
        super(context);
        init(null, 0);
    }

    public ImageViewWithForeground(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImageViewWithForeground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (attrs == null) {
            return;
        }

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ImageViewWithForeground,
                defStyle, 0);

        mForegroundGravity = a.getInt(R.styleable.ImageViewWithForeground_riv_foregroundGravity, mForegroundGravity);

        final Drawable d = a.getDrawable(R.styleable.ImageViewWithForeground_riv_foreground);
        if (d != null) {
            setForeground(d);
        }

        mForegroundInPadding = a.getBoolean(R.styleable.ImageViewWithForeground_riv_foregroundInsidePadding, true);
        dontPressWithParent = a.getBoolean(R.styleable.ImageViewWithForeground_riv_dontPressWithParent, false);
        a.recycle();
    }

    @Override
    public int getForegroundGravity() {
        return mForegroundGravity;
    }

    @Override
    public void setForegroundGravity(int foregroundGravity) {
        if (mForegroundGravity != foregroundGravity) {
            mForegroundBoundsChanged = true;

            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;
            requestLayout();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mForeground);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(getDrawableState());
        }
    }

    @Override
    public void setForeground(Drawable drawable) {
        if (mForeground != drawable) {
            if (mForeground != null) {
                mForeground.setCallback(null);
                unscheduleDrawable(mForeground);
            }

            mForeground = drawable;

            if (drawable != null) {
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
            }
            mForegroundBoundsChanged = true;
            requestLayout();
            invalidate();
        }
    }

    @Override
    public Drawable getForeground() {
        return mForeground;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mForegroundBoundsChanged = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    @Override
    @SuppressLint("NewApi")
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        if (mForeground != null) {
            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

                final int w = getRight()-getLeft();
                final int h = getBottom()-getTop();

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    final int layoutDirection = getLayoutDirection();
                    Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                            foreground.getIntrinsicHeight(), selfBounds, overlayBounds,
                            layoutDirection);
                } else {
                    Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                            foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                }
                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }

    public void setDontPressWithParent(boolean dontPressWithParent) {
        this.dontPressWithParent = dontPressWithParent;
    }

    @Override
    public void setPressed(boolean pressed) {
        if (dontPressWithParent && pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);

        if (mForeground != null) {
            mForeground.setHotspot(x, y);
        }
    }
}
