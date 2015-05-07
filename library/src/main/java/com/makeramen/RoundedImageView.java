package com.makeramen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.shaubert.ui.remoteimageview.ImageViewWithForeground;
import com.shaubert.ui.remoteimageview.R;

public class RoundedImageView extends ImageViewWithForeground {

    public static final String TAG = "RoundedImageView";

    public static final int DEFAULT_RADIUS = 0;
    public static final int DEFAULT_BORDER_WIDTH = 0;

    private static final ImageView.ScaleType[] SCALE_TYPES = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    private float[] mCornerRadii = new float[8];
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private ColorStateList mBorderColor;
    private boolean mOval = false;
    private boolean mModifyBackground = false;

    private int mResourceId;
    private Drawable mForegroundDrawable;
    private RoundedDrawable mRoundedForegroundDrawable;
    private Drawable mDrawable;
    private RoundedDrawable mRoundedDrawable;
    private Drawable mBackgroundDrawable;
    private RoundedDrawable mRoundedBackgroundDrawable;

    private boolean innerSetBgCall;

    private ScaleType mScaleType;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);

        int index = a.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index]);
        } else {
            setScaleType(super.getScaleType());
        }

        int mCornerRadiusLT = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_cornerRadiusLT, DEFAULT_RADIUS);
        int mCornerRadiusRT = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_cornerRadiusRT, DEFAULT_RADIUS);
        int mCornerRadiusRB = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_cornerRadiusRB, DEFAULT_RADIUS);
        int mCornerRadiusLB = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_cornerRadiusLB, DEFAULT_RADIUS);
        int mCornerRadius = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_cornerRadius, -1);
        if (mCornerRadius > 0) {
            mCornerRadiusLT = mCornerRadiusRT = mCornerRadiusRB = mCornerRadiusLB = mCornerRadius;
        }
        mCornerRadii[0] = mCornerRadii[1] = mCornerRadiusLT;
        mCornerRadii[2] = mCornerRadii[3] = mCornerRadiusRT;
        mCornerRadii[4] = mCornerRadii[5] = mCornerRadiusRB;
        mCornerRadii[6] = mCornerRadii[7] = mCornerRadiusLB;

        mBorderWidth = a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_borderWidth, DEFAULT_BORDER_WIDTH);
        mBorderColor = a.getColorStateList(R.styleable.RoundedImageView_riv_borderColor);

        mModifyBackground = a.getBoolean(R.styleable.RoundedImageView_riv_modifyBackground, false);
        mOval = a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);

        updateAllDrawableAttrs();

        a.recycle();
    }

    protected boolean shouldUseRoundedDrawable() {
        return (mBorderWidth > 0 && mBorderColor != null)
                || hasRoundedCorners()
                || mOval;
    }

    private boolean hasRoundedCorners() {
        if (mCornerRadii == null) return false;

        for (float rad : mCornerRadii) {
            if (rad > 0) return true;
        }
        return false;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see android.widget.ImageView.ScaleType
     */
    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    @Override
    public void setScaleType(ScaleType scaleType) {
        assert scaleType != null;

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            if (mRoundedDrawable != null && mRoundedDrawable.handleScaleType()) {
                switch (scaleType) {
                    case CENTER:
                    case CENTER_CROP:
                    case CENTER_INSIDE:
                    case FIT_CENTER:
                    case FIT_START:
                    case FIT_END:
                    case FIT_XY:
                        super.setScaleType(ScaleType.FIT_XY);
                        break;
                    default:
                        super.setScaleType(scaleType);
                        break;
                }
            } else {
                super.setScaleType(scaleType);
            }

            updateAllDrawableAttrs();
            invalidate();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        setupImage(drawable);
    }

    private void setupImage(Drawable drawable) {
        mDrawable = drawable;
        if (drawable != null && shouldUseRoundedDrawable()) {
            mRoundedDrawable = RoundedDrawablesFactory.wrapDrawable(drawable);
            updateDrawableAttrs();
            super.setImageDrawable(mRoundedDrawable.getDrawable());
        } else {
            mRoundedDrawable = null;
            setScaleType(mScaleType);
            super.setImageDrawable(mDrawable);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setupImage(new BitmapDrawable(getResources(), bm));
    }

    @Override
    public void setImageResource(int resId) {
        if (mResourceId != resId) {
            mResourceId = resId;
            setupImage(resolveResource());
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setupImage(getDrawable());
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;

        if (mResourceId != 0) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    d = getContext().getDrawable(mResourceId);
                } else {
                    d = rsrc.getDrawable(mResourceId);
                }
            } catch (Exception e) {
                Log.w(TAG, "Unable to find resource: " + mResourceId, e);
                mResourceId = 0;
            }
        }
        return d;
    }

    @Override
    public void setBackground(Drawable background) {
        if (innerSetBgCall) {
            super.setBackground(background);
        } else {
            setupBgDrawable(background);
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
    public void setBackgroundDrawable(Drawable background) {
        if (innerSetBgCall) {
            super.setBackgroundDrawable(background);
        } else {
            setupBgDrawable(background);
        }
    }

    @SuppressWarnings("deprecation")
    private void setupBgDrawable(Drawable background) {
        mBackgroundDrawable = background;
        Drawable resDrawable = background;
        if (background != null && mModifyBackground && shouldUseRoundedDrawable()) {
            mRoundedBackgroundDrawable = RoundedDrawablesFactory.wrapDrawable(background);
            updateBackgroundDrawableAttrs();
            resDrawable = mRoundedBackgroundDrawable.getDrawable();
        } else {
            mRoundedBackgroundDrawable = null;
        }
        innerSetBgCall = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(resDrawable);
        } else {
            super.setBackgroundDrawable(resDrawable);
        }
        innerSetBgCall = false;
    }

    @Override
    public void setForeground(Drawable drawable) {
        setupForegroundDrawable(drawable);
    }

    private void setupForegroundDrawable(Drawable foreground) {
        mForegroundDrawable = foreground;
        Drawable resDrawable = foreground;
        if (foreground != null && mModifyBackground && shouldUseRoundedDrawable()) {
            mRoundedForegroundDrawable = RoundedDrawablesFactory.wrapDrawable(foreground);
            updateForegroundDrawableAttrs();
            resDrawable = mRoundedForegroundDrawable.getDrawable();
        } else {
            mRoundedForegroundDrawable = null;
        }
        super.setForeground(resDrawable);
    }

    private void updateDrawableAttrs() {
        if (mRoundedDrawable == null)  {
            if (shouldUseRoundedDrawable()) {
                setupImage(mDrawable);
                return;
            }
        }

        if (mRoundedDrawable != null) {
            if (mRoundedDrawable.handleScaleType()) {
                super.setScaleType(ScaleType.FIT_XY);
            } else {
                super.setScaleType(mScaleType);
            }
            updateAttrs(mRoundedDrawable);
        }
    }

    private void updateBackgroundDrawableAttrs() {
        if (mRoundedBackgroundDrawable == null && mModifyBackground) {
            if (shouldUseRoundedDrawable()) {
                setupBgDrawable(mBackgroundDrawable);
                return;
            }
        } else if (!mModifyBackground && mRoundedBackgroundDrawable != null) {
            setupBgDrawable(mBackgroundDrawable);
            return;
        }

        updateAttrs(mRoundedBackgroundDrawable);
    }

    private void updateForegroundDrawableAttrs() {
        if (mRoundedForegroundDrawable == null && mModifyBackground) {
            if (shouldUseRoundedDrawable()) {
                setupForegroundDrawable(mForegroundDrawable);
                return;
            }
        } else if (!mModifyBackground && mRoundedForegroundDrawable != null) {
            setupBgDrawable(mForegroundDrawable);
            return;
        }

        updateAttrs(mRoundedForegroundDrawable);
    }

    private void updateAllDrawableAttrs() {
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs();
        updateForegroundDrawableAttrs();
    }

    private void updateAttrs(RoundedDrawable drawable) {
        if (drawable == null) {
            return;
        }

        drawable.setScaleType(mScaleType)
                .setCornerRadii(mCornerRadii)
                .setBorderWidth(mBorderWidth)
                .setBorderColors(mBorderColor)
                .setOval(mOval);
    }


    public float[] getCornerRadius() {
        return mCornerRadii;
    }

    public void setCornerRadius(int radius) {
        for (int i = 0; i < mCornerRadii.length; i++) {
            mCornerRadii[i] = radius;
        }

        updateAllDrawableAttrs();
    }

    public void setCornerRadii(float[] radii) {
        for (int i = 0; i < mCornerRadii.length; i++) {
            mCornerRadii[i] = radii[i];
        }

        updateAllDrawableAttrs();
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int width) {
        if (mBorderWidth == width) {
            return;
        }

        mBorderWidth = width;
        updateAllDrawableAttrs();
        invalidate();
    }

    public int getBorderColor() {
        return mBorderColor != null ? mBorderColor.getDefaultColor() : Color.TRANSPARENT;
    }

    public void setBorderColor(int color) {
        setBorderColors(ColorStateList.valueOf(color));
    }

    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    public void setBorderColors(ColorStateList colors) {
        if (mBorderColor != null && mBorderColor.equals(colors)) {
            return;
        }

        mBorderColor =
                (colors != null) ? colors : ColorStateList.valueOf(BitmapRoundedDrawable.DEFAULT_BORDER_COLOR);
        updateAllDrawableAttrs();
        if (mBorderWidth > 0) {
            invalidate();
        }
    }

    public boolean isOval() {
        return mOval;
    }

    public void setOval(boolean oval) {
        mOval = oval;
        updateAllDrawableAttrs();
        invalidate();
    }

    public boolean isMutateBackground() {
        return mModifyBackground;
    }

    public void setMutateBackground(boolean mutate) {
        if (mModifyBackground == mutate) {
            return;
        }

        mModifyBackground = mutate;
        updateBackgroundDrawableAttrs();
        updateForegroundDrawableAttrs();
        if (shouldUseRoundedDrawable()) {
            invalidate();
        }
    }
}
