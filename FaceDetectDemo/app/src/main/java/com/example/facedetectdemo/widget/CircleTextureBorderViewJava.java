package com.example.facedetectdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.facedetectdemo.R;
import com.example.facedetectdemo.util.ScreenUtils;

public class CircleTextureBorderViewJava extends View {

    private Paint mPaint;

    private Paint mAnimatePaint;

    private int mTextureViewWidth = getMeasuredWidth();

    private int mColor = Color.CYAN;

    private Paint mTextPaint;

    private String mTipsText = "请放入人脸";

    private float mTextHeight = 0F;

    public CircleTextureBorderViewJava(Context context) {
        this(context, null);
    }

    public CircleTextureBorderViewJava(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setStrokeWidth(ScreenUtils.dip2px(context, 3F));
        mPaint.setStyle(Paint.Style.STROKE);
        mAnimatePaint.setStyle(Paint.Style.FILL);
        mAnimatePaint.setColor(Color.parseColor("#7F000000"));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(ScreenUtils.dip2px(context, 12F));
        mTextPaint.setStrokeWidth(1F);
        mTextHeight = mTextPaint.getFontMetrics().descent - mTextPaint.getFontMetrics().ascent;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleTextureBorderViewJava);
            mColor = a.getColor(
                    R.styleable.CircleTextureBorderViewJava_circleTextureBorderColorJava, Color.CYAN
            );
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mColor);
        float radius = mTextureViewWidth / 2;
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredWidth() / 2, radius, mPaint);
        // 外边框比内部大10-12dp 边框的厚度为1
        mPaint.setStrokeWidth(1F);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredWidth() / 2,
                radius + ScreenUtils.dip2px_20(getContext()), mPaint);
        mPaint.setStrokeWidth(ScreenUtils.dip2px(getContext(), 3F));

        final float left = (getMeasuredWidth() - mTextureViewWidth) / 2;
        final float right = (getMeasuredWidth() + mTextureViewWidth) / 2;
        final float top = (getMeasuredHeight() - mTextureViewWidth) / 2;
        final float bottom = (getMeasuredHeight() + mTextureViewWidth) / 2;

        // x (measuredWidth.toFloat() - mTextPaint.measureText(mTipsText)) / 2
        // y (measuredHeight.toFloat() + mTextureViewWidth / 2) +
        //                    (mTextPaint.fontMetrics.descent - mTextPaint.fontMetrics.ascent) / 2
        canvas.drawArc(left, top, right, bottom, 150F, -120F, false, mAnimatePaint);
        canvas.drawText(mTipsText, ((float) getMeasuredWidth() - mTextPaint.measureText(mTipsText)) / 2,
                ((float) getMeasuredHeight() + (float) mTextureViewWidth / 2) / 2 + mTextHeight * 1.5F, mTextPaint);
    }

    public void setTipsText(String str) {
        this.mTipsText = str;
        postInvalidate();
    }

    public void setCircleTextureWidth(int width) {
        this.mTextureViewWidth = width;
        postInvalidate();
    }
}
