package com.example.pomodorotimerapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularTimerView extends View {
    // 배경과 진행 상황을 그리기 위한 Paint 객체
    private Paint backgroundPaint;
    private Paint progressPaint;

    // 외부 및 내부 원을 그리기 위한 RectF 객체
    private RectF outerRectF;
    private RectF innerRectF;

    // 진행 상황을 나타내는 변수 (0에서 1 사이의 값)
    private float progress = 0;

    // 배경색과 진행 색상
    private int backgroundColor = Color.LTGRAY;
    private int progressColor = Color.RED;

    // 전체 시간과 남은 시간을 밀리초 단위로 저장
    private long totalTimeInMillis = 25 * 60 * 1000; // 기본값 25분
    private long timeLeftInMillis = totalTimeInMillis;

    // 마지막으로 그린 초를 저장 (불필요한 리드로우 방지)
    private long lastSecond = -1;

    // 생성자
    public CircularTimerView(Context context) {
        super(context);
        init();
    }

    public CircularTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 초기화 메서드
    private void init() {
        // Paint 객체 초기화
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.FILL);

        // RectF 객체 초기화
        outerRectF = new RectF();
        innerRectF = new RectF();
    }

    // 뷰 크기가 변경될 때 호출되는 메서드
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 외부 원을 뷰 전체 크기로 설정
        outerRectF.set(0, 0, w, h);
        // 내부 원의 두께를 설정 (반지름의 1/2)
        float thickness = Math.min(w, h) / 4f;
        innerRectF.set(thickness, thickness, w - thickness, h - thickness);
    }

    // 뷰를 그리는 메서드
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 배경 원 그리기
        backgroundPaint.setColor(backgroundColor);
        canvas.drawCircle(outerRectF.centerX(), outerRectF.centerY(), outerRectF.width() / 2, backgroundPaint);

        // 진행 상황 그리기
        progressPaint.setColor(progressColor);
        float sweepAngle = 360 * progress;

        // 외부 호 그리기
        canvas.drawArc(outerRectF, -90, sweepAngle, true, progressPaint);

        // 내부 원 그리기 (배경색)
        canvas.drawCircle(innerRectF.centerX(), innerRectF.centerY(), innerRectF.width() / 2, backgroundPaint);

        // 내부 호 그리기
        canvas.drawArc(innerRectF, -90, sweepAngle, true, progressPaint);
    }

    // 남은 시간을 업데이트하고 뷰를 다시 그리는 메서드
    public void updateTime(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
        long currentSecond = timeLeftInMillis / 1000;

        // 새로운 초가 시작될 때만 progress 업데이트
        if (currentSecond != lastSecond) {
            lastSecond = currentSecond;
            this.progress = 1 - ((float) currentSecond / (totalTimeInMillis / 1000));
        }

        // 마지막 1초 동안 부드럽게 채워지도록 함
        if (currentSecond == 0) {
            float millisProgress = 1 - ((float) (timeLeftInMillis % 1000) / 1000);
            this.progress = Math.max(this.progress, millisProgress);
        }

        // 시간이 0이면 프로그레스를 완전히 채움
        if (timeLeftInMillis == 0) {
            this.progress = 1;
        }

        invalidate(); // 뷰 다시 그리기
    }

    // 타이머를 리셋하는 메서드
    public void reset() {
        this.progress = 0;
        invalidate();
    }

    // 전체 시간을 설정하는 메서드
    public void setTotalTime(long totalTimeInMillis) {
        this.totalTimeInMillis = totalTimeInMillis;
        this.timeLeftInMillis = totalTimeInMillis;
        invalidate();
    }

    // 배경색을 설정하는 메서드
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
    }

    // 진행 색상을 설정하는 메서드
    public void setProgressColor(int color) {
        this.progressColor = color;
        invalidate();
    }
}