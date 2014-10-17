package me.isming.crop;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

/**
 * Created by sam on 14-10-17.
 */
public class DemoActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Crop(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/pic/jjjj.jpg")))
                .output(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/pic/first.jpg")))
                .withWidth(640)
                .start(this);
    }
}