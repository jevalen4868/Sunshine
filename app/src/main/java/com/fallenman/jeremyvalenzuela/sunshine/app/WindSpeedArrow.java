package com.fallenman.jeremyvalenzuela.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jeremyvalenzuela on 8/22/15.
 */
public class WindSpeedArrow extends View {
    private static final String LOG_TAG = WindSpeedArrow.class.getSimpleName();
    // For drawing our wind speed arrow.
    Paint mPaint = new Paint();

    public WindSpeedArrow(Context context) {
        super(context);
        init();
    }

    public WindSpeedArrow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindSpeedArrow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(6f);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        this.setWillNotDraw(false);
        this.invalidate();
        Log.d(LOG_TAG, "init called.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(LOG_TAG, "I'm drawing a pretty picture!");
        super.onDraw(canvas);
        canvas.drawLine(0, 0, 20, 40, mPaint);
        canvas.drawLine(50, 0, 0, 20, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        int minh = MeasureSpec.getSize(w)+ getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

        Log.d(LOG_TAG, "w=" + w + ",h=" + h);
        setMeasuredDimension(w, h);
    }
}
