package com.lepu.demo.views;

import android.content.Context;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;

/**
 * @author chenyongfeng
 */
public class SpO2Chart extends BarLineChartBase<LineData> implements LineDataProvider {
    public SpO2Chart(Context context) {
        super(context);
    }

    public SpO2Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpO2Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new MyLineChartRenderer(this, mAnimator, mViewPortHandler);
    }

    @Override
    protected void calcMinMax() {
        super.calcMinMax();

        if (mXAxis.mAxisRange == 0 && mData.getYValCount() > 0) {
            mXAxis.mAxisRange = 1;
        }
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    protected void onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer instanceof MyLineChartRenderer) {
            ((MyLineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }
}