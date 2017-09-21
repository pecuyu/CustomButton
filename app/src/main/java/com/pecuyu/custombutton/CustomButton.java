package com.pecuyu.custombutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/19.
 * TODO:自定义Button，实现点击状态转换，动态变化动画
 */

public class CustomButton extends View {
    private Paint mPaint;
    private GestureDetector gestureDetector;

    public static final int TYPE_START = 0;
    public static final int TYPE_STOP = 1;
    private int curType = TYPE_STOP;
    private float fraction = 0.0f;
    private float radius;
    private String msgText = "";
    private String btnText = "录屏";
    private boolean isInCycle = false;

    int textMsgSize;
    int textBtnSize;

    private Context mContext;
    private Controller mController;

    ValueAnimator stopAnimator;
    ValueAnimator startAnimator;

    public CustomButton(Context context) {
        this(context, null);
    }

    public CustomButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        textMsgSize = dp2px(context, 20);
        textBtnSize = dp2px(context, 35);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        CustomButtonViewGestureListener listener = new CustomButtonViewGestureListener();
        gestureDetector = new GestureDetector(context, listener);

        mController = new Controller();
        initAnimations();
    }

    private void initAnimations() {
        initStartAnimation();
        initStopAnimation();
        initCycleAnimation();
    }

    private void initCycleAnimation() {
        cycleAnimator = ValueAnimator.ofFloat(1, 0);
        cycleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        cycleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        cycleAnimator.setDuration(1000);
        cycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = 1.0f - animation.getAnimatedFraction() * 0.2f;
                radius = getRadius() * fraction;
                if (isCancelAnimatedCycle && fraction >= 0.99999f) {
                    cycleAnimator.cancel();
                    isCancelAnimatedCycle = false;
                }
                postInvalidate();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initStopAnimation() {
        stopAnimator = ValueAnimator.ofArgb(getResources().getColor(R.color.color_stop), getResources().getColor(R.color.color_start));
        stopAnimator.setEvaluator(new ArgbEvaluator());
        stopAnimator.setInterpolator(new LinearInterpolator());
        stopAnimator.setDuration(500);
        stopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                color = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        stopAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cancelBreathCycle();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initStartAnimation() {
        startAnimator = ValueAnimator.ofArgb(getResources().getColor(R.color.color_start), getResources().getColor(R.color.color_stop));
        startAnimator.setEvaluator(new ArgbEvaluator());
        startAnimator.setInterpolator(new LinearInterpolator());
        startAnimator.setDuration(500);
        startAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                color = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        startAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                startBreathCycle();
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        textBtnSize = Math.min(dp2px(mContext, width / 12), textBtnSize);
        textMsgSize = Math.min(dp2px(mContext, height / 20), textMsgSize);
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        radius = getRadius();
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = 200;
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = 200;
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制圆形
        mPaint.setColor(color);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mPaint);

        // 绘制文本
        mPaint.setColor(Color.RED);

        drawCenterText(msgText, canvas, mPaint, textMsgSize, getHeight() / 12);
        mPaint.setColor(Color.WHITE);
        drawCenterText(btnText, canvas, mPaint, textBtnSize, 0);

    }

    /**
     * 在中间绘制文字
     *
     * @param msgText  文字
     * @param canvas   画布
     * @param paint    画笔
     * @param textSize 文字大小
     * @param offsetY  Y反向偏移
     */
    private void drawCenterText(String msgText, Canvas canvas, Paint paint, int textSize, int offsetY) {
        int color = paint.getColor();
        if (color == 0) {
            paint.setColor(Color.WHITE);
        }
        paint.setTextSize(textSize);
        //获取paint中的字体信息 ， setTextSize方法要在他前面
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        // 计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字高度baseline
        float textBaseY = getHeight() / 2 + (fontHeight / 2 - fontMetrics.bottom);
        //获取字体的长度
        float fontWidth = paint.measureText(msgText);
        //计算文字长度的baseline
        float textBaseX = (getWidth() - fontWidth) / 2;
        canvas.drawText(msgText, textBaseX, textBaseY + offsetY, paint);
    }


    /**
     * 获取圆得半径
     *
     * @return radius
     */
    private int getRadius() {
        return Math.min(getWidth() / 2, getHeight() / 2);
    }

    private int color = getResources().getColor(R.color.color_start);


    /**
     * 点击开始时的颜色变化动画效果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startColorAnimation() {
        if (startAnimator != null && !startAnimator.isRunning())
            startAnimator.start();
    }

    /**
     * 点击结束时的颜色变化动画效果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopColorAnimation() {
        if (stopAnimator != null && !stopAnimator.isRunning())
            stopAnimator.start();
    }


    private class CustomButtonViewGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;  // 一定要返回true
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            if (mController.getType() == TYPE_STOP) {
                mController.setState(new StartState());
            } else {
                mController.setState(new StopState());
            }
            mController.switchType();
            mController.animateView();
            mController.publishListener(listener);

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * 设置指定状态
     *
     * @param condition condition
     */
    public void setToState(boolean condition) {
        if (condition) {
            curType = TYPE_START;
            startColorAnimation();
        } else {
            curType = TYPE_STOP;
            stopColorAnimation();
        }
    }

    /**
     * 重置状态
     */
    public void reset() {
        if (curType == TYPE_START) {
            curType = TYPE_STOP;
            stopColorAnimation();
        }
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
        postInvalidate();
    }

    public void setMsgText(int msgText) {
        this.msgText = getResources().getString(msgText);
        postInvalidate();
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
        postInvalidate();
    }

    public void setBtnText(int btnText) {
        this.btnText = getResources().getString(btnText);
        postInvalidate();
    }

    public int getCurType() {
        return curType;
    }

    /**
     * 状态改变监听
     */
    public interface OnStateChangeListener {
        /**
         * 开始时调用
         */
        void onStart();

        /**
         * 结束时调用
         */
        void onStop();
    }

    private OnStateChangeListener listener;

    public void setOnClickStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }

    ValueAnimator cycleAnimator;

    /**
     * 开始按钮呼吸动画效果
     */
    private void startBreathCycle() {
        if (cycleAnimator != null && !cycleAnimator.isRunning())
            cycleAnimator.start();
    }

    private boolean isCancelAnimatedCycle = false;

    /**
     * 取消按钮呼吸效果
     */
    private void cancelBreathCycle() {
        if (cycleAnimator != null && cycleAnimator.isRunning()) {
            isCancelAnimatedCycle = true;
        }
    }

    /**
     * 立即取消按钮呼吸效果
     */
    private void cancelButtonViewBreathAnimatedCycleImmediately() {
        if (cycleAnimator != null && cycleAnimator.isRunning()) {
            cycleAnimator.cancel();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mController != null) {
            if (visibility == VISIBLE) {
                mController.setState(new StartState());
            } else {
                mController.setState(new StopState());
            }
            mController.animateView();
        }
    }

    //转换dp为px
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());

    }

    //转换sp为px
    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    class StartState implements State {

        @Override
        public void animateView() {
            startColorAnimation();
            startBreathCycle();
        }

        @Override
        public void publishListener(OnStateChangeListener listener) {
            if (listener != null) listener.onStart();
        }
    }

    class StopState implements State {

        @Override
        public void animateView() {
            stopColorAnimation();
            cancelBreathCycle();
        }

        @Override
        public void publishListener(OnStateChangeListener listener) {
            if (listener != null) listener.onStop();
        }
    }


    class Controller {
        private State state;
        private int type = TYPE_STOP;

        public Controller() {
        }

        public void setState(State state) {
            this.state = state;
        }

        public void animateView() {
            state.animateView();
        }

        public void publishListener(OnStateChangeListener listener) {
            state.publishListener(listener);
        }

        public int getType() {
            return type;
        }

        public void switchType() {
            type = (type == TYPE_STOP) ? TYPE_START : TYPE_STOP;
        }
    }
}
