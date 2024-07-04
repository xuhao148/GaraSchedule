package org.aya.garaschedule;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;

public class DocTableView extends View {
    private Paint titlePaint;
    private Paint framePaint;
    private Paint markerPaint;
    private Paint beginTimePaint;
    private Paint endTimePaint;
    private float textAreaHeight;
    private float textHeight;
    private float graphAreaHeight;
    private float lineFrameX;
    private float lineFrameY;
    private float lineFrameWidth;
    private float lineFrameHeight;
    private float lineSepWidth;
    private float lineSepHeight;
    private float weekdayMarkerHeight;
    private float smallTextSize;

    private final int[] bgColorSet = {0xff66ccff,0xff39c5bb,0xffdc9fb4,0xfffbe251,0xff1b813e,0xff3a8fb7,0xff6d2e5b};
    private final int[] fgColorSet = {0xff000000,0xffffffff,0xff000000,0xff000000,0xffffffff,0xffffffff,0xffffffff};
    private ArrayList<PeriodItem> schedule;
    public DocTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0xffffffff);
        recalculateArgs(getWidth(),getHeight());
        initGraphics();
        schedule = new ArrayList<>();
        try {
            schedule = SchDBManager.getInstance().getPeriods();
        } catch (Exception ex) {
            return;
        }
    }

    public DocTableView(Context context) {
        super(context);
        setBackgroundColor(0xffffffff);
        recalculateArgs(getWidth(),getHeight());
        initGraphics();
        try {
            schedule = SchDBManager.getInstance().getPeriods();
        } catch (Exception ex) {
            return;
        }
    }

    private void initGraphics() {
        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xff000000);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(textHeight);
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(0xff707070);
        framePaint.setStrokeWidth(1);
        framePaint.setStyle(Paint.Style.STROKE);
        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(0xff101010);
        markerPaint.setTextAlign(Paint.Align.CENTER);
        beginTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        endTimePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        beginTimePaint.setTextAlign(Paint.Align.RIGHT);
        beginTimePaint.setColor(0xff101010);
        endTimePaint.setTextAlign(Paint.Align.LEFT);
        endTimePaint.setColor(0xff101010);

    }

    private void recalculateArgs(int w, int h) {
        float scale_factor = 0.9F * w / h / 2;
        if (scale_factor > 0.4) scale_factor = 0.4F;
        textAreaHeight = h / 20.0F;
        textHeight = textAreaHeight * scale_factor;
        graphAreaHeight = h - textAreaHeight;
        lineFrameX = w * 0.1F;
        lineFrameY = textAreaHeight + graphAreaHeight * 0.1F;
        if (lineFrameY > textAreaHeight + lineFrameX) lineFrameY = textAreaHeight + lineFrameX;
        lineFrameWidth = w * 0.8F;
        lineFrameHeight = graphAreaHeight - lineFrameY;
        lineSepWidth = lineFrameWidth / 7.0F;
        lineSepHeight = lineFrameHeight / 13.0F;
        weekdayMarkerHeight = lineFrameY - textAreaHeight;
        if (titlePaint != null)
            titlePaint.setTextSize(textHeight);
        if (markerPaint != null)
            markerPaint.setTextSize(weekdayMarkerHeight * scale_factor);
        // Get a good size for time markers
        final String tester = "99：99";
        Paint paint_tester = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_tester.setTextSize(12);
        Rect test_bounds = new Rect();
        paint_tester.getTextBounds(tester,0,tester.length(),test_bounds);
        float factor = lineFrameX * 1.0F / test_bounds.width() * 0.8F;
        float factor_2 = lineSepHeight * 1.0F / test_bounds.height() * 0.8F;
        if (factor_2 < factor) factor = factor_2;
        if (beginTimePaint != null)
            beginTimePaint.setTextSize(12 * factor);
        if (endTimePaint != null)
            endTimePaint.setTextSize(12 * factor);
        smallTextSize = 12 * factor;
        if (smallTextSize > 24) smallTextSize = 24;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateArgs(w,h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((textAreaHeight / 2) - ((titlePaint.descent() + titlePaint.ascent()) / 2));
            // canvas.drawText("スケジュール",xPos,yPos,titlePaint);
            // canvas.drawRect(new RectF(0,0,60,60),framePaint);
            // Draw the line frame
            canvas.drawRect(new RectF(lineFrameX, lineFrameY, lineFrameX + lineFrameWidth, lineFrameY + lineFrameHeight), framePaint);
            float frameX = lineFrameX + lineSepWidth;
            float endY = lineFrameY + lineFrameHeight;
            for (int i = 1; i < 7; i++) {
                canvas.drawLine(frameX, lineFrameY, frameX, endY, framePaint);
                int cx = (int) (frameX - lineSepWidth / 2);
                int cy = (int) textAreaHeight + (int) ((weekdayMarkerHeight / 2) - ((markerPaint.descent() + markerPaint.ascent()) / 2));
                canvas.drawText(getContext().getString(NewPeriodActivity.weekdays[i - 1]), cx, cy, markerPaint);
                frameX += lineSepWidth;
            }
            int cx = (int) (frameX - lineSepWidth / 2);
            int cy = (int) textAreaHeight + (int) ((weekdayMarkerHeight / 2) - ((markerPaint.descent() + markerPaint.ascent()) / 2));
            canvas.drawText(getContext().getString(NewPeriodActivity.weekdays[6]), cx, cy, markerPaint);
            float frameY = lineFrameY + lineSepHeight;
            float endX = lineFrameX + lineFrameWidth;
            for (int i = 1; i <= 13; i++) {
                if (i != 13)
                    canvas.drawLine(lineFrameX, frameY, endX, frameY, framePaint);
                String beginTime = String.format(getContext().getString(R.string.time_format), TimeDataUtil.hours_begin[i], TimeDataUtil.minutes_begin[i]);
                String endTime = String.format(getContext().getString(R.string.time_format), TimeDataUtil.hours_end[i], TimeDataUtil.minutes_end[i]);
                float cy2 = frameY - lineSepHeight + ((lineSepHeight / 2) - ((beginTimePaint.descent() + beginTimePaint.ascent()) / 2));
                canvas.drawText(beginTime, lineFrameX * 9 / 10, cy2, beginTimePaint);
                canvas.drawText(endTime, endX + lineFrameX / 10, cy2, endTimePaint);
                frameY += lineSepHeight;
            }

            for (PeriodItem item : schedule) {
                LinearLayout itemLayout = new LinearLayout(getContext());
                TextView itemView = new TextView(getContext());
                itemView.setVisibility(View.VISIBLE);
                float x, y, width, height;
                if (item.week1 && item.week2) {
                    x =  (lineFrameX + item.weekday * lineSepWidth);
                    y =  (lineFrameY + (item.start - 1) * lineSepHeight);
                    width =  lineSepWidth;
                    height =  (lineSepHeight * (item.end - item.start + 1));
                } else if (item.week1) {
                    x =  (lineFrameX + item.weekday * lineSepWidth);
                    y =  (lineFrameY + (item.start - 1) * lineSepHeight);
                    width =  lineSepWidth / 2;
                    height = (lineSepHeight * (item.end - item.start + 1));
                } else if (item.week2) {
                    x = (lineFrameX + item.weekday * lineSepWidth + lineSepWidth / 2);
                    y = (lineFrameY + (item.start - 1) * lineSepHeight);
                    width = lineSepWidth / 2;
                    height = (lineSepHeight * (item.end - item.start + 1));
                } else continue;
                itemView.setGravity(Gravity.CENTER);
                itemView.setText(String.format(getContext().getString(R.string.next_sched_view_format), item.course, item.loc));
                int hash = item.course.hashCode();
                if (hash < 0) hash = -hash;
                hash = hash % bgColorSet.length;
                itemView.setBackgroundColor(bgColorSet[hash]);
                itemView.setTextColor(fgColorSet[hash]);
                itemView.setTextSize(TypedValue.COMPLEX_UNIT_PX,smallTextSize);
                itemView.setWidth((int)width);
                itemView.setHeight((int)height);
                itemLayout.addView(itemView);
                itemLayout.measure(canvas.getWidth(), canvas.getHeight());
                itemLayout.layout(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.save();
                canvas.translate(x, y);
                itemLayout.draw(canvas);
                canvas.restore();
            }
        } catch (Exception ex) {
            Paint errPaint = new Paint();
            errPaint.setTextSize(18);
            errPaint.setColor(0xffff0000);
            canvas.restore();
            canvas.drawText(ex.toString(),0,30,errPaint);
        }
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
