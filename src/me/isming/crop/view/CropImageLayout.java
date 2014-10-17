package me.isming.crop.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import me.isming.crop.R;

/**
 * Created by sam on 14-10-17.
 */
public class CropImageLayout extends RelativeLayout {

    private CropZoomableImageView mZoomImageView;
    private CropImageBorderView mClipImageView;

    public final static int MAX_WIDTH = 2048;

    private int mHorizontalPadding = 20;

    public CropImageLayout(Context context) {
        this(context, null);
    }

    public CropImageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mZoomImageView = new CropZoomableImageView(context);
        mClipImageView = new CropImageBorderView(context);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mZoomImageView, params);
        addView(mClipImageView, params);

        mZoomImageView.setImageResource(R.drawable.test);
    }

    public Bitmap clip() {
        return mZoomImageView.clip();
    }

    public void setImageBitmap(Bitmap bitmap) {
        mZoomImageView.setImageBitmap(bitmap);
        mZoomImageView.reLayout();
        mZoomImageView.invalidate();
    }

    public void setImagePath(String filePath) {
        Bitmap b = BitmapFactory.decodeFile(filePath);
        setImageBitmap(b);
    }
}
