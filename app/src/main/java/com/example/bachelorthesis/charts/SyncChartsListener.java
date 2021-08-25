package com.example.bachelorthesis.charts;

import android.graphics.Matrix;
import android.view.MotionEvent;

import com.example.bachelorthesis.ContentFragment;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

/**
 * Syncs the charts to show the same viewport. Can be called manually, but also triggers when moving or scaling one chart
 */
public class SyncChartsListener implements OnChartGestureListener {

    private final Chart<?> sourceChart;
    private final ContentFragment contentFragment;

    public SyncChartsListener(Chart<?> sourceChart, ContentFragment contentFragment) {
        this.sourceChart = sourceChart;
        this.contentFragment = contentFragment;
    }

    public static void syncCharts(Chart<?> mainChart, Chart<?>[] otherCharts) {
        Matrix mainMatrix;
        float[] mainVals = new float[9];
        Matrix otherMatrix;
        float[] otherVals = new float[9];
        mainMatrix = mainChart.getViewPortHandler().getMatrixTouch();
        mainMatrix.getValues(mainVals);


        for (Chart<?> tempChart : otherCharts) {

            otherMatrix = tempChart.getViewPortHandler().getMatrixTouch();
            otherMatrix.getValues(otherVals);
            otherVals[Matrix.MSCALE_X] = mainVals[Matrix.MSCALE_X];
            otherVals[Matrix.MTRANS_X] = mainVals[Matrix.MTRANS_X];
            otherVals[Matrix.MSKEW_X] = mainVals[Matrix.MSKEW_X];
            otherMatrix.setValues(otherVals);
            tempChart.getViewPortHandler().refresh(otherMatrix, tempChart, true);

        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me,
                                    ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me,
                                  ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        syncCharts(sourceChart, contentFragment.getOtherCharts(sourceChart));
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        syncCharts(sourceChart, contentFragment.getOtherCharts(sourceChart));
    }
}
