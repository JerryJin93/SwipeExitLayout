package com.jerryjin.lib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Author: Jerry
 * Generated at: 2019/3/15 12:02
 * GitHub: https://github.com/JerryJin93
 * Blog:
 * WeChat: enGrave93
 * Version: 0.0.1
 * Description:
 */
@SuppressWarnings("FieldCanBeLocal")
public class SwipeExitLayout extends FrameLayout {

    private static final String TAG = SwipeExitLayout.class.getSimpleName();
    private static final float THRESHOLD_SCALE_FLOOR = 0.4f;
    private static int screenHeight;
    public boolean allowExit = true;
    private boolean lockAndLoad;
    private Activity mContext;
    private View decor;
    private View originDecorChild;
    private Drawable originDecorBackground;
    private float downX, downY;
    private float lastX, lastY;
    private View mContent;
    private View onlyChild;
    private Scroller mScroller;
    private int mTouchSlop;
    private VelocityTracker tracker;
    private boolean timeToFinish;
    private boolean lock;
    private Rect originArea;

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

    private void init(Context context) {
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        tracker = VelocityTracker.obtain();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
    }

    public void attachTo(Activity activity) {
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

        originDecorChild = decorChild;
        originDecorBackground = decorChild.getBackground();

        decorChild.setBackgroundResource(background);

        decorView.removeView(decorChild);
        setContentView(decorChild);
        addView(onlyChild);
        decorView.addView(this);
        mContent = this;
    }


    private void setContentView(View decorChild) {
        onlyChild = decorChild;
    }

    private void startEvacuation(MotionEvent event) {
        // 跟随手指移动offset进行位移
        Log.e(TAG, "getX: " + event.getX() + " downX: " + downX + " getY: " + event.getY() + " downY: " + downY);
        Log.e(TAG, "x: " + (int) -(event.getX() - downX) + " y: " + (int) -(event.getY() - downY));
        // - -> in + -> out
        scrollTo((int) -(event.getX() - downX), (int) -(event.getY() - downY));
        float scale = (screenHeight - event.getY()) / screenHeight;
        Log.e(TAG, "scale: " + scale);
        // down↓ scale↓ -> event.getY()↑ scale↓

        float finalScale =
                scale > 0 && scale >= THRESHOLD_SCALE_FLOOR ? scale : THRESHOLD_SCALE_FLOOR;
        setBackgroundColor(0x33000000);
        onlyChild.setScaleX(finalScale);
        onlyChild.setScaleY(finalScale);

        Log.e(TAG, "l, t, r, b: " + getLeft() + ", " + getTop() + ", " + getRight() + ", " + getBottom());

        timeToFinish = lastY > screenHeight * 2f / 3;
        // postInvalidate();
    }

    private void requestRestoreToFirst() {
        Log.e(TAG, "restoring");
        mScroller.startScroll(mContent.getScrollX(), mContent.getScrollY(),
                -mContent.getScrollX(), -mContent.getScrollY());
        onlyChild.setScaleX(1);
        onlyChild.setScaleY(1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        tracker.clear();
        tracker.addMovement(ev);
        tracker.computeCurrentVelocity(1000);
        lockAndLoad = tracker.getYVelocity() > 0;
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
                if (!lock) {
                    originArea = new Rect(getLeft(), getTop(), getRight(), getBottom());
                    lock = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = event.getX();
                lastY = event.getY();
                if (lockAndLoad) {
                    startEvacuation(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (timeToFinish) {
                    mContext.finish();
                    mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    requestRestoreToFirst();
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

    private Rect getRect(MotionEvent event, float scale) {
        Rect rect = new Rect();
        return rect;
    }

    private boolean validateGesture(MotionEvent event) {
        return event.getRawX() - downX >= mTouchSlop || event.getRawY() - downY >= mTouchSlop;
    }

    // TODO: 2019/3/18  跟手優化
}
