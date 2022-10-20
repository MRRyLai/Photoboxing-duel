package com.example.facedetectdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.example.facedetectdemo.R;

public class RoundTextureViewJava extends TextureView {

    private float mRadius = 0F;

    public RoundTextureViewJava(Context context) {
        this(context, null);
    }

    public RoundTextureViewJava(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundTextureViewJava);
            if (a.hasValue(R.styleable.RoundTextureViewJava_textureRadiusJava)) {
                mRadius = a.getFloat(R.styleable.RoundTextureViewJava_textureRadiusJava, (float) getMeasuredWidth() / 2);
            } else {
                mRadius = (float) Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
            }
            a.recycle();
        }


        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                final Rect rect;
                if (getMeasuredWidth() <= getMeasuredHeight()) {
                    rect = new Rect(0, (getMeasuredHeight() - getMeasuredWidth()) / 2, getMeasuredWidth(),
                            (getMeasuredHeight() + getMeasuredWidth()) / 2);
                } else {
                    rect = new Rect((getMeasuredWidth() - getMeasuredHeight()) / 2, 0, getMeasuredHeight(),
                            (getMeasuredHeight() + getMeasuredWidth()) / 2);
                }
                outline.setRoundRect(rect, mRadius);
            }
        });
        setClipToOutline(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public void turnRound() {
        invalidateOutline();
    }
}
