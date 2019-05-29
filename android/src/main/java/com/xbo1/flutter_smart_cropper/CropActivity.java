package com.xbo1.flutter_smart_cropper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.flutter.plugin.common.BinaryMessenger;
import me.pqpo.smartcropperlib.view.CropImageView;

public class CropActivity extends AppCompatActivity {

    private static final String EXTRA_CHANNEL = "extra_channel";
    private static final String EXTRA_FILE = "extra_file";
    private static final int REQUEST_CODE_TAKE_PHOTO = 100;
    private static final int REQUEST_CODE_SELECT_ALBUM = 200;

    //Options
    private int colorActionBar = 0xFF006B36;
    private int colorActionBarTitle = 0xFFFFFFFF;
    private int colorStatusBar = 0xFF006B36;
    private boolean isStatusBarLight = false;
    private String actionBarTitle = "裁剪图片";

    public static BinaryMessenger messenger;
    String channel;

    CropImageView ivCrop;

    boolean mFromAlbum = true;
//    File mCroppedFile;
    String extraFile = "";
    File tempFile;

    public static Intent getJumpIntent(Context context, String channel, String file) {
        Intent intent = new Intent(context, CropActivity.class);
        intent.putExtra(EXTRA_CHANNEL, channel);
        intent.putExtra(EXTRA_FILE, file);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ivCrop = (CropImageView) findViewById(R.id.iv_crop);
        initToolBar();

        tempFile = new File(getExternalFilesDir("img"), "temp.jpg");
//        mCroppedFile = (File) getIntent().getSerializableExtra(EXTRA_CROPPED_FILE);
//        if (mCroppedFile == null) {
//            setResult(RESULT_CANCELED);
//            finish();
//            return;
//        }
        channel = getIntent().getStringExtra(EXTRA_CHANNEL);
        extraFile = getIntent().getStringExtra(EXTRA_FILE);
        if (extraFile.isEmpty()) {
            selectPhoto();
        }
        else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(extraFile, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateSampleSize(options);
            Bitmap bmp = BitmapFactory.decodeFile(extraFile, options);
            if (bmp != null) {
                ivCrop.setImageToCrop(bmp);
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(Activity activity, int colorStatusBar) {
        if (colorStatusBar == Integer.MAX_VALUE) return;
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colorStatusBar);
    }
    private void initToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_picker_bar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(colorActionBar);
        toolbar.setTitleTextColor(colorActionBarTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColor(this, colorStatusBar);
        }
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
//            if (fishton.drawableHomeAsUpIndicator != null)
//                getSupportActionBar().setHomeAsUpIndicator(fishton.drawableHomeAsUpIndicator);
        }

        if (isStatusBarLight
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(actionBarTitle);
        }

    }

    private void selectPhoto() {
        if (mFromAlbum) {
            Intent selectIntent = new Intent(Intent.ACTION_PICK);
            selectIntent.setType("image/*");
            if (selectIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(selectIntent, REQUEST_CODE_SELECT_ALBUM);
            }
        } else {
            Intent startCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(this, "com.bob.smartcrop.fileProvider", tempFile);
            } else {
                uri = Uri.fromFile(tempFile);
            }
            startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            if (startCameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(startCameraIntent, REQUEST_CODE_TAKE_PHOTO);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Bitmap selectedBitmap = null;
        if (requestCode == REQUEST_CODE_TAKE_PHOTO && tempFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tempFile.getPath(), options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateSampleSize(options);
            selectedBitmap = BitmapFactory.decodeFile(tempFile.getPath(), options);
        } else if (requestCode == REQUEST_CODE_SELECT_ALBUM && data != null && data.getData() != null) {
            ContentResolver cr = getContentResolver();
            Uri bmpUri = data.getData();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateSampleSize(options);
                selectedBitmap = BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (selectedBitmap != null) {
            ivCrop.setImageToCrop(selectedBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smart_cropper, menu);
//        MenuItem item = menu.findItem(R.id.action_ok);
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            if (ivCrop.canRightCrop()) {
                Bitmap crop = ivCrop.crop();
                if (crop != null) {
//                    saveImage(crop, mCroppedFile);
                    try {
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        crop.compress(Bitmap.CompressFormat.JPEG, 85, bitmapStream);
                        byte[] byteArray = bitmapStream.toByteArray();
                        crop.recycle();
                        bitmapStream.close();
                        final ByteBuffer buffer;
                        if (byteArray != null) {
                            buffer = ByteBuffer.allocateDirect(byteArray.length);
                            buffer.put(byteArray);
                            if (messenger != null) {
                                messenger.send(channel, buffer);
                            }
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
            } else {
                Toast.makeText(CropActivity.this, "裁剪失败", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

//    private void saveImage(Bitmap bitmap, File saveFile) {
//        try {
//            FileOutputStream fos = new FileOutputStream(saveFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private int calculateSampleSize(BitmapFactory.Options options) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int sampleSize = 1;
        int destHeight = 1000;
        int destWidth = 1000;
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }
}
