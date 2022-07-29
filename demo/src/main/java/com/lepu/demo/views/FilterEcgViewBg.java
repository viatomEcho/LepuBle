package com.lepu.demo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FilterEcgViewBg extends View /*implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener*/ {
    public static final short NULL_VALUE = Short.MAX_VALUE;
    // Starting position of drawing
    private final float chartStartX = 0, chartStartY = 0;
    // Sampling step
    private final int sampleStep = 1;
    //view height
    private float height = 0;
    private long startTime;
    // line number of wave
    private int lineNum;
    // the distance(px) between two point
    private float xDis;
    // the distance(px) between two lines
    private float lineDis;
    // the padding of line
    private float linePadding;
    // the total length(px) of per line
    private float chartLineLength;
    // lineDis + 2 * linePadding
    private float wholeLineDis;

    // About ECG Ruler
    private float rulerStandardWidth = 20;
    private float rulerZeroWidth = 13;
    private final float rulerTotalWidth = rulerStandardWidth + 2 * rulerZeroWidth;
    //    private final float standard1mV = (32767 / 4033) * 12 * 8; //
    private final double standard1mV = (float) ((1.0035 * 1800) / (4096 * 178.74));
    //    private final double[] standardNmV = {standard1mV * 8.0, standard1mV * 4,standard1mV * 2,standard1mV};
    private final double[] standardNmV = {0.5, 1.0, 2.0};
    private double rulerStandard;
    // The distance between ruler and chart
    private final float disOfRulerChart = 10;

    // About drawing
    private float screenW;
    private short[] chartY;
    private int validValueLength;
    private Paint linePaint, textPaint, recPaint, axisPaint;
    private double minY, maxY;

    private Paint bkg_paint_1;
    private Paint bkg_paint_2;
    private float mGrid1mmLength;
    private float mSpeed = 6.25f;
    private int mAxisIndex;

    private GestureDetector detector;
    OnPageScrolledListener mOnPageScrolledListener;
    ViewGroup parent;
    float x, y, x1, y1;
    private int currentZoomPosition = 1;
    private boolean enableClick = true;
    private int startPoint = 0;
    public float preTouchY = 0;
    public boolean isTouching = false;
    public float currentMoveY = 0;
    public float touchY = 0;
    private float yOffSet;

    HashSet<Float> hashSet = new HashSet<>();
    Float temp = 0f; // 去重画线

    //point frequency 125Hz
    static  int HZ = 125;
    private float SECONDS_PER_LINE = 10;

    private static final int ONE_PAGE_LINES = 4;

    private SeekBar seekBar;
    private long firstLineTime;
    private boolean touchable = true;
    public int POINTS_PER_LINE = (int)(SECONDS_PER_LINE * 125);
    public int ONE_PAGE_POINTS = (int)(SECONDS_PER_LINE * ONE_PAGE_LINES * 125);
    public int PREPARED_DRAW_POINTS = ONE_PAGE_POINTS + POINTS_PER_LINE;
    int deviceType ;
    // About selection rectangle
//    private float recWidth, recHeight;
//    private float recX, recY, preRecX, preRecY;
//    private boolean canSelect = true;

    // Delegate interface
//    private ECGViewDelegate delegate;
    public FilterEcgViewBg(Context context) {
        this(context, null);
    }

    public FilterEcgViewBg(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterEcgViewBg(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public FilterEcgViewBg(Context context, short[] Y, int ScreenW, int currentZoomPosition) {
        super(context);
        this.currentZoomPosition = currentZoomPosition;
        chartY = Y;
        if (chartY.length == 0) {
            return;
        }
        this.screenW = ScreenW;

        initPaint();
        initGestureDetector(context);
        InitFixParams();

        getMinAndMax();
    }

    public FilterEcgViewBg(Context context, long startTime,
                           short[] Y, int validValueLength,
                           float ScreenW, float viewHeight,
                           int currentZoomPosition, boolean enableClick
    , int deviceType) {
        super(context);
        this.deviceType = deviceType;
        mGrid1mmLength = (float) 25.4 / getResources().getDisplayMetrics().xdpi;
        SECONDS_PER_LINE = ScreenW / (1 / mGrid1mmLength) / mSpeed;
        HZ = 125;
        POINTS_PER_LINE = (int)(SECONDS_PER_LINE * HZ);
        ONE_PAGE_POINTS = (int)(SECONDS_PER_LINE * ONE_PAGE_LINES * HZ);
        PREPARED_DRAW_POINTS = ONE_PAGE_POINTS + POINTS_PER_LINE;

        this.currentZoomPosition = currentZoomPosition;
        chartY = Y;

        this.height = viewHeight;
        if (chartY.length == 0) {
            return;
        }

        this.startTime = startTime;
        this.validValueLength = validValueLength;
        this.screenW = ScreenW;
        this.enableClick = enableClick;

        initPaint();
        initGestureDetector(context);
        InitFixParams();

        getMinAndMax();
    }

    public FilterEcgViewBg(Context context, long startTime, short[] Y, int validValueLength, int ScreenW, int currentZoomPosition, boolean enableClick) {
        super(context);
        this.currentZoomPosition = currentZoomPosition;
        chartY = Y;
        if (chartY.length == 0) {
            return;
        }

        this.startTime = startTime;
        this.validValueLength = validValueLength;
        this.screenW = ScreenW;
        this.enableClick = enableClick;

        initPaint();
        initGestureDetector(context);
        InitFixParams();

        getMinAndMax();
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        this.seekBar.setProgress(0);
    }

    /**
     * Initialize paints
     */
    public void initPaint() {
        axisPaint = new Paint();
        axisPaint.setAntiAlias(true);
        axisPaint.setStyle(Paint.Style.FILL);
        axisPaint.setStrokeWidth((float) 2.0);
        axisPaint.setColor(Color.parseColor("#f2f2f2"));

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setTextSize(15);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth((float) 1.5);
        linePaint.setColor(Color.parseColor("#4E596F"));

        textPaint = new Paint();
        textPaint.setTextSize(36);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth((float) 1.5);
        textPaint.setColor(Color.parseColor("#999999"));
//        textPaint.setFakeBoldText(true);

        recPaint = new Paint();
        recPaint.setAntiAlias(true);
        recPaint.setStyle(Paint.Style.STROKE);
        recPaint.setStrokeWidth(4);
        recPaint.setColor(Color.argb(255, 48, 100, 0));

        bkg_paint_1 = new Paint();
        bkg_paint_1.setColor(Color.parseColor("#4DFF0000"));
        bkg_paint_1.setStyle(Paint.Style.STROKE);
        bkg_paint_1.setStrokeWidth(2.0f);

        bkg_paint_2 = new Paint();
        bkg_paint_2.setColor(Color.parseColor("#1AFF0000"));
        bkg_paint_2.setStyle(Paint.Style.STROKE);
        bkg_paint_2.setStrokeWidth(1.0f);
    }

    /**
     * 初始化手势识别
     *
     * @param context
     */
    private void initGestureDetector(Context context) {
        if (context == null) {
            return;
        }
       /* detector = new GestureDetector(context, this);
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                detector.onTouchEvent(arg1);
                return false;
            }
        });*/
    }

    /**
     * Initialize parameters
     */
    public void InitFixParams() {
        chartLineLength = screenW;

        xDis = (float) (screenW / (SECONDS_PER_LINE * HZ - 1)) * sampleStep;
        rulerZeroWidth = 1 / mGrid1mmLength;
        rulerStandardWidth = rulerZeroWidth * 3;

        linePadding = 2.0f;
        if (height == 0) {
            lineDis = (float) (screenW * 8 / (7.0 * 5.0));
            wholeLineDis = lineDis + 2 * linePadding;
        } else {
            wholeLineDis = (5 / mGrid1mmLength) * 5;
            lineDis = wholeLineDis - 2 * linePadding * ONE_PAGE_LINES;
        }
//        lineNum = 16;
        lineNum = (int) Math.ceil(validValueLength * 1.0f / POINTS_PER_LINE);

        // 小于等于30s不需要画全屏
        if (lineNum <= ONE_PAGE_LINES) {
            height = wholeLineDis * lineNum;
            mAxisIndex = lineNum + 1;
        } else {
            mAxisIndex = ONE_PAGE_LINES + 1 + 1;
        }
    }


    /**
     * Get the maximum and minimum in ECG data
     * And select a ruler in the meantime
     */
    public void getMinAndMax() {
//        maxY = -100;
//        minY = 100;
//        for (int i = 0; i < chartY.length; i++) {
//            double value = chartY[i];
//            if ((maxY) < value)
//                maxY = value;
//            if ((minY) > value)
//                minY = value;
//        }
//
//        maxY = Math.min(maxY, standardNmV[0] / 2);
//        minY = Math.max(minY, -standardNmV[0] / 2);
//
//        for(int i = 0; i < standardNmV.length; i++){
//            if((maxY - minY) <= standardNmV[i])
//                rulerStandard = standardNmV[i];
//            currentZoomPosition = i;
//        }
//        // Expand the scope of the maximum and minimum
//        maxY = (maxY + minY) / 2 + rulerStandard;
//        minY = (maxY + minY) / 2 - rulerStandard;
        rulerStandard = standardNmV[currentZoomPosition];
    }

    public int getCurrentZoomPosition() {
        return currentZoomPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (chartY == null || chartY.length == 0) {
            return;
        }
//        canvas.drawColor(Color.WHITE);
        drawBkg(canvas);
        drawAxis(canvas);
        drawPath(canvas, chartY);
    }

    public void drawBkg(Canvas canvas) {
        // 1mm x
        for (int i = 0; i < screenW/(1/ mGrid1mmLength) + 1; i++) {
            Path p = new Path();
            p.moveTo(i/ mGrid1mmLength, 0);
            p.lineTo(i/ mGrid1mmLength, height);
            canvas.drawPath(p, bkg_paint_2);
        }

        // 5mm x
        for (int i = 0; i < screenW/(5/ mGrid1mmLength) + 1; i++) {
            Path p = new Path();
            p.moveTo(i*5/ mGrid1mmLength, 0);
            p.lineTo(i*5/ mGrid1mmLength, height);
            canvas.drawPath(p, bkg_paint_1);
        }

        // 1mm y
        for (int i = 0; i < height/(1/mGrid1mmLength); i ++) {
            Path p = new Path();
            p.moveTo(0,  i*(1/mGrid1mmLength));
            p.lineTo(screenW,  i*(1/mGrid1mmLength));
            canvas.drawPath(p, bkg_paint_2);
        }
    }

    /**
     * Draw axes and ruler
     *
     * @param canvas
     */
    public void drawAxis(Canvas canvas) {
        hashSet.clear();
        for (int i = 0; i < mAxisIndex; i++) {

            for (int j=0; j<=i*5; j++) {
                temp = (wholeLineDis * i / (i*5) * j) + yOffSet;
                if (!hashSet.contains(temp)) {
                    hashSet.add(temp);
                    canvas.drawLine(chartStartX, temp, chartStartX
                            + chartLineLength, temp, bkg_paint_1);
                }
            }

            long time;
            time = (startPoint + POINTS_PER_LINE * (i-1)) / HZ + TimeUnit.MILLISECONDS.toSeconds(startTime);
            if (i == 0) {
                setFirstLineTime(time);
            }
            Date date = new Date(TimeUnit.SECONDS.toMillis(time));
            textPaint.setTextSize(SizeUtils.dp2px(12));
            linePaint.setTextSize(SizeUtils.dp2px(12));
            String timeStamp = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(date);
//                canvas.drawText(timeStamp, chartStartX+5, wholeLineDis * i  + yOffSet - 20, linePaint);
            canvas.drawText(timeStamp, 50, wholeLineDis * i + yOffSet - 20, textPaint);
        }

        if (startPoint < POINTS_PER_LINE) {
            canvas.drawText(mSpeed+"mm/s", screenW - 280, wholeLineDis + yOffSet - 20, textPaint);
            drawRuler(canvas);
        }
    }

    private void setFirstLineTime(long time) {
        firstLineTime = time;
    }


    private void drawRuler(Canvas canvas) {
        float zeroLineY = wholeLineDis / 5.0f * 3.0f + yOffSet;
        float standardLineY = wholeLineDis / 5.0f + yOffSet;

        canvas.drawLine(chartStartX, zeroLineY, chartStartX + rulerZeroWidth,
                zeroLineY, linePaint);
        // Draw first vertical line
        canvas.drawLine(chartStartX + rulerZeroWidth, zeroLineY, chartStartX
                + rulerZeroWidth, standardLineY, linePaint);
        // Draw 1mV line
        canvas.drawLine(chartStartX + rulerZeroWidth, standardLineY, chartStartX
                + rulerZeroWidth + rulerStandardWidth, standardLineY, linePaint);
        // Draw second vertical line
        canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
                zeroLineY, chartStartX + rulerZeroWidth + rulerStandardWidth,
                standardLineY, linePaint);
        // Draw second zero line
        canvas.drawLine(chartStartX + rulerZeroWidth + rulerStandardWidth,
                zeroLineY, chartStartX + rulerZeroWidth * 2 + rulerStandardWidth,
                zeroLineY, linePaint);

        String rulerStr = "1mV";
        double ruleNum = 1 / rulerStandard;
        if (ruleNum > 0.5) {
            int ruleNumInt = (int) ruleNum;
            rulerStr = ruleNumInt + "mV";
        } else {
            rulerStr = ruleNum + "mV";
        }
//        String rulerStr =  1 / rulerStandard + "mV";

        canvas.drawText(rulerStr, chartStartX + 5, standardLineY - 20, linePaint);
    }

    /**
     * Draw ECG Path
     *
     * @param canvas
     * @param Y
     */
    public void drawPath(Canvas canvas, short[] Y) {
        int line = 0;// The line drawing currently
        float preTempX = 0, preTempY = 0, preChartY = 0;

        //小于一页的数据取实际长度
        int length = Math.min(Math.min(PREPARED_DRAW_POINTS, validValueLength), validValueLength - startPoint);
/*        short[] drawPoints = Arrays.copyOfRange(chartY, startPoint, startPoint + length);
        drawPoints = HomeActivity.shortfilter(drawPoints);*/
        for (int i = 0, k = 0; i < length/*Y.length*/; i += sampleStep) {
            float tempX;
            // Staring position are different from first row and other line
           /* if(line == 0) {
                tempX = chartStartX + rulerZeroWidth * 2 + rulerStandardWidth
                        + disOfRulerChart + k * xDis;
            } else {
                tempX = chartStartX + k * xDis;
            }*/

            tempX = chartStartX + k * xDis;
            int index = startPoint + i;
            if (index < 0 || index > Y.length-1) {
                break;
            }
            float yVal = (float) ((Y[index])/*drawPoints[i]*/ * (1.0035 * 1800) / (4096 * 178.74));
            float tempY = yOffSet + (float) (wholeLineDis / 5.0 * 3.0 + line - (1.0 * rulerStandard) * (1/mGrid1mmLength*10) * (yVal));
            if (i == 0) { // First point
                preTempX = NULL_VALUE;
                preTempY = NULL_VALUE;
                preChartY = NULL_VALUE;

            }

            if (preTempX != NULL_VALUE && Y[index] != NULL_VALUE && preChartY != NULL_VALUE) {
//                if(preTempY>)
                /*
                * */
                //rjz 修改，避免绘制得波形超出范围 start
//                    int lineNumber = (int) ((i+yOffSet)/(1250));
//                    float size = (float) (lineNumber* wholeLineDis + yOffSet);
//                    if(preTempY>size&&preTempY<(lineNumber+1)*size&&tempY>size&&tempY<(lineNumber+1)*size){
                        canvas.drawLine(preTempX, preTempY, tempX, tempY, linePaint);
//                    }
//                    Log.v("drawPath======",""+i+"=====yVal==="+yOffSet);
                //rjz 修改，避免绘制得波形超出范围 end
            }
            preTempX = tempX;
            preTempY = tempY;
            preChartY = Y[index];
            k++;

            //If draw a line full, move to next line
            if (preTempX >= chartStartX + chartLineLength) {
//                Logger.d(FilterEcgView.class, xDis  + " " + chartLineLength +" chartLineLength | next line : i " + i);
                line += wholeLineDis;
//                canvas.drawLine(chartStartX, lineDis + line, chartStartX
//                        + chartLineLength, lineDis + line, axisPaint);
                i--;
                preTempX = NULL_VALUE;
                preTempY = NULL_VALUE;
                preChartY = NULL_VALUE;
                k = 0;
            }
        }
    }

    /*@Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float deltaX = Math.abs(e1.getX() - e2.getX());
        float deltaY = Math.abs(e1.getY() - e2.getY());
        if(deltaX > deltaY) {
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaX = Math.abs(e1.getX() - e2.getX());
        float deltaY = Math.abs(e1.getY() - e2.getY());
        if(deltaX < deltaY) {
            return false;
        }
        if (e1.getX() - e2.getX() > 120) {
            LogUtils.d("左划");
            if(mOnPageScrolledListener != null) {
                mOnPageScrolledListener.scrollLeft();
            }
            return true;
        } else if (e1.getX() - e2.getX() < -120) {
            LogUtils.d("右划");
            if(mOnPageScrolledListener != null) {
                mOnPageScrolledListener.scrollRight();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if(enableClick) {
            changeZoomPosition();
        }
        return true;
    }*/

    public int changeZoomPosition() {
        if (currentZoomPosition == (standardNmV.length - 1)) {
            currentZoomPosition = 0;
        } else {
            currentZoomPosition = currentZoomPosition + 1;
        }
        rulerStandard = standardNmV[currentZoomPosition];
        postInvalidate();
        return currentZoomPosition;
    }

    public void setCurrentZoomPosition(int zoomPosition) {
        rulerStandard = standardNmV[zoomPosition];
        postInvalidate();
    }

    public void setOnPageScrolledListener(OnPageScrolledListener onPageScrolledListener) {
        this.mOnPageScrolledListener = onPageScrolledListener;
    }

    public void setParent(ViewGroup viewGroup) {
        this.parent = viewGroup;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public float getProgressPercent() {
        if(validValueLength <= ONE_PAGE_POINTS) {
            return 0;
        }
        return ((float)startPoint)/(validValueLength - ONE_PAGE_POINTS);
    }

    public void setYOffSetInPoints(float points) {
        LogUtils.d("offset points", points);
        yOffSet = -wholeLineDis * (points / POINTS_PER_LINE);
    }

    public short[] getCurPageFilterData() {
        //小于一页的数据取实际长度
        int length = Math.min(Math.min(PREPARED_DRAW_POINTS, validValueLength), validValueLength - startPoint);
        short[] drawPoints = Arrays.copyOfRange(chartY, startPoint, startPoint + length);
//        drawPoints = HomeActivity.shortfilter(drawPoints);//如果不滤波注释此行
        return drawPoints;
    }

    public long getFirstLineTime() {
        return firstLineTime;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    public interface OnPageScrolledListener {
        void scrollLeft();

        void scrollRight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!touchable) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            preTouchY = event.getY();
            isTouching = true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
        }
        if (isTouching) {
            float TempY = event.getY();
            touchY = TempY - preTouchY;
            preTouchY = TempY;
//            startPoint += touchY;
            if (startPoint == 0 && Math.abs(yOffSet) < touchY && touchY > 0) {
                return true;
            }
            //到达最后一分钟
            if (touchY < 0 && (validValueLength - startPoint) <= ONE_PAGE_POINTS) {
                return true;
            }
            if (yOffSet < 0 && touchY > 0 && (yOffSet + touchY > 0)) {
                if (startPoint > POINTS_PER_LINE) {
                    startPoint -= POINTS_PER_LINE;

                    if (yOffSet > 0) {
                        yOffSet = wholeLineDis - Math.abs(yOffSet);
                    } else {
                        yOffSet = -(wholeLineDis - Math.abs(yOffSet));
                    }
                }
               /* yOffSet += touchY;
                invalidate();
                return true;*/
            }
            yOffSet += touchY;
            float abs = Math.abs(yOffSet);
            if (abs > wholeLineDis) {
                abs %= wholeLineDis;
                if (yOffSet < 0) {
                    startPoint += (HZ * SECONDS_PER_LINE);
                } else {
                    startPoint -= (HZ * SECONDS_PER_LINE);
                }
                yOffSet = yOffSet > 0 ? (abs - wholeLineDis) : (-abs);
            }
            if (startPoint < 0) {
                startPoint = 0;
            }
            /*if (currentMoveY > 0)
                currentMoveY = 0;
            if (-currentMoveY > chartLineLength - screenWidth + 100)
                currentMoveY = -(chartLineLength - screenWidth + 100);*/
//            seekBar.setProgress((int) -currentMoveY);
//            Er1Event.post(Er1Event.REFRESH_ECG_PROGRESS);
            if(seekBar != null) {
                int progress = (int) (getProgressPercent() * seekBar.getMax());
                if(progress != seekBar.getProgress()) {
                    seekBar.setProgress(progress);
                }
                LogUtils.d("progress", progress, getProgressPercent());
            }

            invalidate();
        }
        return true;
    }

}
