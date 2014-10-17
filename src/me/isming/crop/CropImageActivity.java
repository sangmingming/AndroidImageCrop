package me.isming.crop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import me.isming.crop.util.CLog;
import me.isming.crop.view.CropImageLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CropImageActivity extends MonitoredActivity {

    private CropImageLayout mImageView;
    private Uri mSaveUri;
    private int mMaxWidth;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mImageView = (CropImageLayout) findViewById(R.id.clip);
        setupFromIntent();
    }


    private void setupFromIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mMaxWidth = extras.getInt(Crop.Extra.MAX_WIDTH);
            mSaveUri = extras.getParcelable(MediaStore.EXTRA_OUTPUT);
        }

        Uri data = getIntent().getData();
        if (data == null) {
            finish();
        }
        InputStream is = null;
        try {
            int sampleSize = calculateBitmapSampleSize(data);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            is = getContentResolver().openInputStream(data);
            Bitmap b = BitmapFactory.decodeStream(is, null, options);
            mImageView.setImageBitmap(b);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            CropUtil.closeSilently(is);
        }
    }


    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtil.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }

        CLog.e("SampleSize" + sampleSize);
        return sampleSize;
    }

    private int getMaxImageSize() {
        int textureLimit = getScreenHeight();
        if (textureLimit == 0) {
            return CropImageLayout.MAX_WIDTH;
        } else {
            return Math.min(textureLimit, CropImageLayout.MAX_WIDTH);
        }
    }

    private int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clip:
                saveOutput();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void saveOutput() {
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = getContentResolver().openOutputStream(mSaveUri);
                Bitmap b = mImageView.clip();

                if (outputStream != null && b != null) {
                    if (mMaxWidth >0 && b.getWidth() > mMaxWidth) {
                        b = Bitmap.createScaledBitmap(b, mMaxWidth, mMaxWidth, true);
                    }
                    b.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
            } catch (IOException e) {
                CLog.e("Cannot open file: " + mSaveUri);
            } finally {
                CropUtil.closeSilently(outputStream);
            }


        }
    }


}
