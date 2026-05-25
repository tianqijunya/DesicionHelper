package com.example.desicionhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {

    private List<String> options = new ArrayList<>();
    private Paint arcPaint, textPaint, shadowPaint, pointerPaint, centerPaint, centerTextPaint, linePaint;
    private float currentRotation = 0f;
    private boolean isSpinning = false;
    private OnSpinCompleteListener listener;

    private final int[] COLORS = {
            0xFFE8A838,
            0xFFE85D3A,
            0xFFE84393,
            0xFF6C5CE7,
            0xFF00B894,
            0xFF0984E3,
            0xFFE17055,
            0xFF00CEC9,
            0xFFFDCB6E,
            0xFFA29BFE
    };

    public interface OnSpinCompleteListener {
        void onSpinComplete(int selectedIndex, String selectedOption);
    }

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0x88000000);
        shadowPaint.setTextAlign(Paint.Align.CENTER);
        shadowPaint.setFakeBoldText(true);

        pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerPaint.setColor(0xFFE8C547);
        pointerPaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(0xFFE8C547);
        centerPaint.setStyle(Paint.Style.FILL);

        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(Color.WHITE);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setFakeBoldText(true);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0x66FFFFFF);
        linePaint.setStrokeWidth(3f);
    }

    public void setOptions(List<String> opts) {
        this.options = new ArrayList<>(opts);
        invalidate();
    }

    public void setOnSpinCompleteListener(OnSpinCompleteListener l) {
        this.listener = l;
    }

    public boolean isSpinning() {
        return isSpinning;
    }

    public void spin() {
        if (isSpinning || options.isEmpty()) return;
        isSpinning = true;

        final int n = options.size();
        final float segAngle = 360f / n;
        final int targetIndex = (int) (Math.random() * n);

        float targetAngle = 360f - targetIndex * segAngle - segAngle / 2f;
        float extra = 360f * 6;
        final float finalRotation = currentRotation + extra
                + ((targetAngle - currentRotation % 360 + 360) % 360);

        ValueAnimator animator = ValueAnimator.ofFloat(currentRotation, finalRotation);
        animator.setDuration(4500);
        animator.setInterpolator(new DecelerateInterpolator(3f));

        animator.addUpdateListener(a -> {
            currentRotation = (float) a.getAnimatedValue();
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator a) {
                isSpinning = false;
                if (listener != null) {
                    listener.onSpinComplete(targetIndex, options.get(targetIndex));
                }
            }
        });

        animator.start();
    }

    public int getColor(int index) {
        return COLORS[index % COLORS.length];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (options.isEmpty()) return;

        int n = options.size();
        float segAngle = 360f / n;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 50f;

        if (radius < 30f) return;

        RectF arcRect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        float startAngle = currentRotation - 90f;

        for (int i = 0; i < n; i++) {
            int color = COLORS[i % COLORS.length];

            // 画扇区
            arcPaint.setColor(color);
            canvas.drawArc(arcRect, startAngle, segAngle, true, arcPaint);

            // 画分隔线
            double rad = Math.toRadians(startAngle);
            canvas.drawLine(cx, cy,
                    cx + (float) Math.cos(rad) * radius,
                    cy + (float) Math.sin(rad) * radius, linePaint);

            // 计算扇区中心位置
            float midAngleDeg = startAngle + segAngle / 2f;
            double midAngleRad = Math.toRadians(midAngleDeg);
            float textRadius = radius * 0.6f;
            float tx = cx + (float) Math.cos(midAngleRad) * textRadius;
            float ty = cy + (float) Math.sin(midAngleRad) * textRadius;

            // 字体大小
            float textSize;
            if (n <= 3) {
                textSize = radius * 0.16f;
            } else if (n <= 5) {
                textSize = radius * 0.14f;
            } else if (n <= 8) {
                textSize = radius * 0.11f;
            } else {
                textSize = radius * 0.09f;
            }
            textPaint.setTextSize(textSize);
            shadowPaint.setTextSize(textSize);

            // 截断过长文字
            String displayText = options.get(i);
            float maxWidth = radius * 0.5f;
            while (textPaint.measureText(displayText) > maxWidth && displayText.length() > 2) {
                displayText = displayText.substring(0, displayText.length() - 1);
            }
            if (!displayText.equals(options.get(i))) {
                displayText += "..";
            }

            // 画文字（先阴影再白色）
            canvas.drawText(displayText, tx + 2, ty + 2, shadowPaint);
            canvas.drawText(displayText, tx, ty, textPaint);

            startAngle += segAngle;
        }

        // 外圈边框
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(0x44FFFFFF);
        canvas.drawCircle(cx, cy, radius, borderPaint);

        // 指针
        Paint outerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCircle.setColor(0xFF222222);
        canvas.drawCircle(cx, cy - radius - 30f, 14f, outerCircle);
        canvas.drawCircle(cx, cy - radius - 30f, 10f, pointerPaint);

        Path tri = new Path();
        tri.moveTo(cx, cy - radius + 2f);
        tri.lineTo(cx - 14f, cy - radius - 26f);
        tri.lineTo(cx + 14f, cy - radius - 26f);
        tri.close();
        canvas.drawPath(tri, pointerPaint);

        // 中心圆
        float centerRadius = radius * 0.22f;
        canvas.drawCircle(cx, cy, centerRadius, centerPaint);
        centerTextPaint.setTextSize(centerRadius * 0.7f);
        canvas.drawText("开始", cx, cy + centerTextPaint.getTextSize() / 3f, centerTextPaint);
    }
}

