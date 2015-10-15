package com.shaubert.ui.remoteimageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.makeramen.RoundedImageView;

public class OptimizedImageView extends RoundedImageView {

    private boolean fixedSizeOptimizationEnabled;
    private boolean shouldBeLayouted;

    public OptimizedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OptimizedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptimizedImageView(Context context) {
        super(context);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        shouldBeLayouted = true;
        super.setLayoutParams(params);
        if (params != null) {
            if (params.width > 0 || params.height > 0) {
                fixedSizeOptimizationEnabled = true;
            }
        } else {
            fixedSizeOptimizationEnabled = false;
        }
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void requestLayout() {
        if (!fixedSizeOptimizationEnabled || shouldBeLayouted) {
            callRequestLayout();
            shouldBeLayouted = false;
        }
    }

    public boolean isFixedSizeOptimizationEnabled() {
        return fixedSizeOptimizationEnabled;
    }

    public void callRequestLayout() {
        super.requestLayout();
    }
}
