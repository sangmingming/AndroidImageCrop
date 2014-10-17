package me.isming.crop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageView;
import me.isming.crop.util.CLog;

/**
 * Created by sam on 14-10-16.
 */
public class CropZoomableImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener{

    private float SCALE_MAX = 4.0f;
    private float SCALE_MID = 2.0f;

    /**
     * 初始缩放比例。如果图片宽度或高度大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;

    private final float[] matrixValues = new float[9]; //用于存放矩阵的9个值

    private boolean once = true;

    private ScaleGestureDetector mScaleGestureDetector = null; //缩放手势的检测

    private final Matrix mScaleMatrix = new Matrix();

    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    private int mTouchSlop;
    private boolean isCheckTopAndBottom = true;
    private boolean isCheckLeftAndRight = true;

    private int mHorizontalPadding = 0;
    private int mVerticalPadding;

    public CropZoomableImageView(Context context) {
        this(context, null);
    }

    public CropZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropZoomableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        // 获得可以认为是滚动的距离
        mTouchSlop = configuration.getScaledTouchSlop();
        //mTouchSlop = 4;

        mHorizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mHorizontalPadding, getResources().getDisplayMetrics());

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale == true) {
                    return true;
                }
                float x = e.getX();
                float y = e.getY();
                if (getScale() < SCALE_MID) {
                    CropZoomableImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MID, x, y), 16);
                    isAutoScale = true;
                } else if (getScale() >= SCALE_MID && getScale() <= SCALE_MAX) {
                    CropZoomableImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MAX, x, y), 16);
                    isAutoScale = true;
                } else {
                    CropZoomableImageView.this.postDelayed(new AutoScaleRunnable(initScale, x, y), 16);
                    isAutoScale = true;
                }
                return true;
            }
        });


    }

    public void reLayout() {
        once = true;
    }

    @Override
    public void onGlobalLayout() {
        if (once) {
            Drawable d = getDrawable();
            if (d == null) {
                return;
            }
            CLog.e("drawable.intrinsicWidth:" + d.getIntrinsicWidth() +
                    ",drawable.intrinsicHeight:" + d.getIntrinsicHeight());
            int width = getWidth();
            int height = getHeight();

            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            float scale = 1.0f;

            scale = Math.max(width * 1.0f / dw, width * 1.0f /dh);

            initScale = scale;
            SCALE_MAX = initScale * 4;
            SCALE_MID = initScale *2;
            mVerticalPadding = (getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;

            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) /2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mScaleMatrix);
            once = false;

        }
    }

    /**
     * 裁剪图片，返回裁剪后的bitmap对象
     * @return
     */
    public Bitmap clip() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, mHorizontalPadding, mVerticalPadding, getWidth() - 2 * mHorizontalPadding,
                getHeight() - 2 * mVerticalPadding);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null) {
            return true;
        }

        if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)) {
            if (scaleFactor * scale  < initScale) {
                scaleFactor = initScale / scale;
            }

            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale;
            }

            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private float mLastX, mLastY;
    private int mLastPointerCount;
    private boolean mIsCanDrag;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        mScaleGestureDetector.onTouchEvent(event);
        float x = 0, y = 0;

        final int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);

        }

        x = x / pointerCount;
        y = y / pointerCount;

        if (pointerCount != mLastPointerCount) {
            mIsCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!mIsCanDrag) {
                    CLog.i("action move" + pointerCount + "show can drag");
                    mIsCanDrag = isCanDrag(dx, dy);
                }



                if (mIsCanDrag) {
                    CLog.i("can drag action move");
                    RectF rectF = getMatrixRectF();
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        //如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() < getWidth() - 2 * mHorizontalPadding) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        if (rectF.height() < getHeight() - 2 * mVerticalPadding) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastY = y;
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;

        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        //如果宽或搞大于屏幕则控制范围
        if (rect.width() >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - rect.right - mHorizontalPadding;
            }

        }

        if (rect.height() >= height - 2 * mVerticalPadding) {
            if (rect.top > 0) {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - rect.bottom - mVerticalPadding;
            }
        }

        //如果宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }

        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);


    }

    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();
        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();

        if (rect.top > mVerticalPadding && isCheckTopAndBottom) {
            deltaY = -rect.top + mVerticalPadding;
        }
        if (rect.bottom < viewHeight - mVerticalPadding && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom - mVerticalPadding;
        }
        if (rect.left > mHorizontalPadding && isCheckLeftAndRight) {
            deltaX = -rect.left + mHorizontalPadding;
        }
        if (rect.right < viewWidth - mHorizontalPadding && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right - mHorizontalPadding;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    private boolean isCanDrag(float dx, float dy) {
        CLog.i("x:" + Math.sqrt((dx * dx) + (dy * dy)) + "y:" + mTouchSlop);
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }

    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        private float x;
        private float y;

        public AutoScaleRunnable(float targetScale, float x, float y) {
            mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();

            if(((tmpScale > 1f) && (currentScale < mTargetScale)) || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                CropZoomableImageView.this.postDelayed(this, 16);
            } else {
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }
}
