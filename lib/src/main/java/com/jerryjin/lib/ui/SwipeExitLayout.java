package com.jerryjin.lib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.jerryjin.lib.R;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Author: Jerry
 * Generated at: 2019/3/15 12:02
 * GitHub: https://github.com/JerryJin93
 * Blog:
 * WeChat: enGrave93
 * Version: 1.0.1
 * Description: Swipe down to exit.
 */
public class SwipeExitLayout extends FrameLayout {

    private static final String TAG = SwipeExitLayout.class.getSimpleName();
    private static final float THRESHOLD_SCALE_FLOOR = 0.4f;
    private static final float DEFAULT_FINISH_OFFSET_THRESHOLD = 200;
    private static int screenHeight;

    private Activity mContext;
    private View mContent;
    private View onlyChild;
    private View decor;
    private Drawable originDecorBackground;

    private boolean lockAndLoad;
    private boolean allowExit = true;
    private boolean onStartLock;
    private float mStartEvacuationOffset;

    private int mBackgroundColor;
    private float downX, downY;
    private float lastX, lastY;

    private Scroller mScroller;
    private int mTouchSlop;
    private VelocityTracker mTracker;
    private OnExitListener mOnExitListener;

    public SwipeExitLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeExitLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeExitLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initialization.
     *
     * @param context The context of current layout.
     */
    private void init(Context context) {
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTracker = VelocityTracker.obtain();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
    }

    /**
     * Attach to a specific Activity.
     *
     * @param activity The specified Activity.
     */
    public final void attachTo(Activity activity) {
        mContext = activity;
        // set the background of ActionBar to window background.
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        int background = ta.getResourceId(0, 0);
        ta.recycle();

        // Get decor view.
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        decor = decorView;

        // Get action bar and set its background to current window's background.
        int childCount = decorView.getChildCount();
        Log.e(TAG, "has " + childCount + (childCount > 1 ? " children" : " child") + ".");
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);

        originDecorBackground = decorChild.getBackground();

        decorChild.setBackgroundResource(background);

        decorView.removeView(decorChild);
        setContentView(decorChild);
        addView(onlyChild);
        decorView.addView(this);
        mContent = this;
    }

    /**
     * Detach from the current Activity.
     */
    public final void detach() {
        ((ViewGroup) decor).removeViewAt(0);
        removeViewAt(0);
        onlyChild.setBackground(originDecorBackground);
        ((ViewGroup) decor).addView(onlyChild);
    }


    /**
     * Set the child of the attached Activity's decor view the field.
     *
     * @param decorChild The child of the attached Activity's decor view.
     */
    private void setContentView(View decorChild) {
        onlyChild = decorChild;
    }

    /**
     * Start the exit animation.
     */
    private void startEvacuation() {
        // 跟随手指移动offset进行位移
        Log.e(TAG, "getX: " + lastX + " downX: " + downX + " getY: " + lastY + " downY: " + downY);
        Log.e(TAG, "x: " + (int) -(lastX - downX) + " y: " + (int) -(lastY - downY));
        // - -> in + -> out
        scrollTo((int) -(lastX - downX), (int) -(lastY - downY));

        float scale = (screenHeight - lastY) / screenHeight;
        Log.e(TAG, "scale: " + scale);
        // down↓ scale↓ -> event.getY()↑ scale↓

        float finalScale = scale >= THRESHOLD_SCALE_FLOOR ? scale : THRESHOLD_SCALE_FLOOR;

        computeAlpha(scale);
        setBackgroundColor(mBackgroundColor);
        if (mOnExitListener != null) {
            mOnExitListener.onExit(mBackgroundColor);
        }

        //setBackgroundColor(0x33000000);
        onlyChild.setScaleX(finalScale);
        onlyChild.setScaleY(finalScale);

        Log.e(TAG, "l, t, r, b: " + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom());

        mStartEvacuationOffset = mStartEvacuationOffset == 0 ? DEFAULT_FINISH_OFFSET_THRESHOLD : mStartEvacuationOffset;
        // postInvalidate();
    }

    /**
     * Restore to origin.
     */
    private void requestRestoreToFirst() {
        Log.e(TAG, "restoring");
        mScroller.startScroll(mContent.getScrollX(), mContent.getScrollY(),
                -mContent.getScrollX(), -mContent.getScrollY());
        onlyChild.setScaleX(1);
        onlyChild.setScaleY(1);
        // temporary solution for restoring position.
        scrollTo(0, 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mTracker.clear();
        mTracker.addMovement(ev);
        mTracker.computeCurrentVelocity(1000);
        lockAndLoad = mTracker.getYVelocity() > 0;
        Log.e(TAG, "yVelocity: " + mTracker.getYVelocity() + " pixels per second.");
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (lockAndLoad && validateGesture(ev) && allowExit) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = event.getX();
                lastY = event.getY();
                if (lockAndLoad) {
                    startEvacuation();
                    if (mOnExitListener != null && !onStartLock) {
                        mOnExitListener.onStart();
                        onStartLock = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean timeToFinish = event.getY() - downY > mStartEvacuationOffset;
                if (timeToFinish) {
                    if (mOnExitListener != null) {
                        mOnExitListener.onPreFinish();
                    }
                    // TODO: 2019/3/25 Animation
                    // setBackgroundColor(Color.TRANSPARENT);
                    mContext.finish();
                    mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    requestRestoreToFirst();
                    if (mOnExitListener != null) {
                        mOnExitListener.onRestore();
                        onStartLock = false;
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mContent.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * To determine the gesture of current user has met the requirement of executing the exit animation.
     *
     * @param event The current MotionEvent object.
     * @return True if the gesture is correct, false otherwise.
     */
    private boolean validateGesture(MotionEvent event) {
        return event.getRawX() - downX >= mTouchSlop || event.getRawY() - downY >= mTouchSlop;
    }

    // TODO: 2019/3/18  跟手優化

    /**
     * Register a callback to be invoked when starts exit animation.
     *
     * @param listener The callback that will run.
     */
    public void setOnExitListener(OnExitListener listener) {
        mOnExitListener = listener;
    }

    /**
     * Reset the background color to pure black.
     */
    private void resetBackgroundColor() {
        mBackgroundColor = 0x00000000;
    }

    /**
     * Update the alpha value.
     *
     * @param alpha The newest alpha value.
     */
    private void computeAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        resetBackgroundColor();
        mBackgroundColor |= (int) ((alpha >= 0 ? alpha : 0) * 255.0f + 0.5f) << 24;
    }

    /**
     * Get the alpha value of the background color.
     *
     * @return The current alpha value.
     */
    public int getBackgroundAlpha() {
        return mBackgroundColor & 0xff000000;
    }

    /**
     * Get the distance between the y value of finger-down point and the finger-up point.
     *
     * @return The distance that developer set.
     */
    public float getStartEvacuationOffset() {
        return mStartEvacuationOffset;
    }

    /**
     * Tell me when to start to evaluate.
     *
     * @param offsetFromDownY The distance between the y value of finger-down point and the finger-up point.
     */
    public void setStartEvacuationOffset(float offsetFromDownY) {
        mStartEvacuationOffset = offsetFromDownY;
    }

    /**
     * Set the flag that whether intercept the touch event or not.
     *
     * @param allowExit True if not to intercept, false otherwise.
     */
    public void allowExit(boolean allowExit) {
        this.allowExit = allowExit;
    }

    /**
     * Get to know whether this layout will intercept touch event or not.
     *
     * @return True if not to intercept, false otherwise.
     */
    public boolean isExitPermitted() {
        return allowExit;
    }

    /**
     * An exposed interface for doing something either after the beginning of the exit animation
     * or before finishing the attached Activity.
     */
    public interface OnExitListener {
        /**
         * Callback that will be invoked after the beginning of the exit animation.
         */
        void onStart();

        /**
         * Callback that will be invoked during the exit animation.
         *
         * @param backgroundColor The background color of root view of the attached Activity.
         */
        void onExit(int backgroundColor);

        /**
         * Callback that will be invoked before finishing the attached Activity.
         */
        void onPreFinish();

        /**
         * Callback that will be invoked when restore to the primitive status.
         */
        void onRestore();
    }

    /**
     * Implementation for OnExitListener.
     */
    public static abstract class OnExitListenerImpl implements OnExitListener {
        @Override
        public void onStart() {

        }

        @Override
        public void onExit(int backgroundColor) {

        }

        @Override
        public void onPreFinish() {

        }

        @Override
        public void onRestore() {

        }
    }
}
