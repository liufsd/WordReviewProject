
package com.coleman.tools.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;

import com.coleman.kingword.R;
import com.coleman.kingword.provider.KingWord.THistory;

public class ChartManager {
    private static ChartManager chartManager;

    private ChartManager() {
    }

    public static ChartManager getInstance() {
        if (chartManager == null) {
            chartManager = new ChartManager();
        }
        return chartManager;
    }

    public View getChartView(Context context) {
        String[] titles = new String[] {
            context.getString(R.string.chart_desc)
        };
        List<Date[]> dates = new ArrayList<Date[]>();
        List<int[]> values = new ArrayList<int[]>();
        Date[] dateValues = new Date[25];
        long dt = 24 * 3600 * 1000;
        long ct = System.currentTimeMillis()/dt*dt;
        for (int i = 0; i < dateValues.length; i++) {
            dateValues[i] = new Date(ct - dt * (dateValues.length - i));
        }
        dates.add(dateValues);
        int[] v = new int[dateValues.length];
        final String projection[] = new String[] {
            "count(*) as count"
        };
        int max = 10;
        for (int i = 0; i < v.length; i++) {
            String where = "";
            if (i < v.length - 1) {
                where = THistory.REVIEW_TIME + ">" + dateValues[i].getTime() + " and "
                        + THistory.REVIEW_TIME + "<" + dateValues[i + 1].getTime();
            } else {
                where = THistory.REVIEW_TIME + ">" + dateValues[i].getTime();
            }
            Cursor cursor = context.getContentResolver().query(THistory.CONTENT_URI, projection,
                    where, null, null);
            if (cursor.moveToFirst()) {
                v[i] = cursor.getInt(0);
            }
            if (v[i] > max) {
                max = v[i];
            }
            cursor.close();
        }
        values.add(v);
        int[] colors = new int[] {
            Color.BLUE
        };
        PointStyle[] styles = new PointStyle[] {
            PointStyle.POINT
        };
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
        setChartSettings(renderer, context.getString(R.string.chart_title),
                context.getString(R.string.chart_x_label),
                context.getString(R.string.chart_y_label), dateValues[0].getTime(),
                dateValues[dateValues.length - 1].getTime(), -4, 11, Color.GRAY, Color.LTGRAY);
        renderer.setYLabels(10);
        renderer.setYAxisMin(-10);
        renderer.setYAxisMax(max);
        int bgColor = context.getResources().getColor(R.color.cornsilk);
        renderer.setBackgroundColor(bgColor);
        renderer.setMarginsColor(bgColor);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        TimeChart chart = new TimeChart(buildDateDataset(titles, dates, values), renderer);
        chart.setDateFormat("MM/dd");
        GraphicalView mView = new GraphicalView(context, chart);

        return mView;
    }

    /**
     * Builds an XY multiple time dataset using the provided values.
     * 
     * @param titles the series titles
     * @param xValues the values for the X axis
     * @param yValues the values for the Y axis
     * @return the XY multiple time dataset
     */
    protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
            List<int[]> yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = titles.length;
        for (int i = 0; i < length; i++) {
            TimeSeries series = new TimeSeries(titles[i]);
            Date[] xV = xValues.get(i);
            int[] yV = yValues.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    /**
     * Sets a few of the series renderer settings.
     * 
     * @param renderer the renderer to set the properties to
     * @param title the chart title
     * @param xTitle the title for the X axis
     * @param yTitle the title for the Y axis
     * @param xMin the minimum value on the X axis
     * @param xMax the maximum value on the X axis
     * @param yMin the minimum value on the Y axis
     * @param yMax the maximum value on the Y axis
     * @param axesColor the axes color
     * @param labelsColor the labels color
     */
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
            String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
            int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
    }

    /**
     * Builds an XY multiple series renderer.
     * 
     * @param colors the series rendering colors
     * @param styles the series point styles
     * @return the XY multiple series renderers
     */
    protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, colors, styles);
        return renderer;
    }

    protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] {
                20, 30, 15, 20
        });
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
    }
}
