package com.xbo1.flutter_smart_cropper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import me.pqpo.smartcropperlib.SmartCropper;

/** FlutterSmartCropperPlugin */
public class FlutterSmartCropperPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {
  private static final int REQUEST_CODE_GRANT_PERMISSIONS = 2001;
  private static final String ANDROID_OPTIONS = "options";
  private final MethodChannel channel;
  private final Activity activity;
  private final Context context;
  private final BinaryMessenger messenger;
  private Result pendingResult;
  private MethodCall methodCall;

  private FlutterSmartCropperPlugin(Activity activity, Context context, MethodChannel channel, BinaryMessenger messenger) {
    this.activity = activity;
    this.context = context;
    this.channel = channel;
    this.messenger = messenger;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_smart_cropper");
    FlutterSmartCropperPlugin instance = new FlutterSmartCropperPlugin(registrar.activity(), registrar.context(), channel, registrar.messenger());
    registrar.addActivityResultListener(instance);
    registrar.addRequestPermissionsResultListener(instance);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {

    if (!setPendingMethodCallAndResult(call, result)) {
      finishWithAlreadyActiveError(result);
      return;
    }

    if ("cropImage".equals(call.method)) {
//      startCropImage();
      finishWithSuccess();
    }
    else if ("detectImageRect".equals(call.method)) {
      String file = call.argument("file");
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(file, options);
      int outHeight = options.outHeight;
      int outWidth = options.outWidth;
      options.inJustDecodeBounds = false;
      options.inSampleSize = calculateSampleSize(options);
      Bitmap bmp = BitmapFactory.decodeFile(file, options);
      Point[] pts = SmartCropper.scan(bmp);
      HashMap<String, Object> ret = new HashMap<>();
      ret.put("tl", pt2Json(pts[0]));
      ret.put("tr", pt2Json(pts[1]));
      ret.put("br", pt2Json(pts[2]));
      ret.put("bl", pt2Json(pts[3]));
      ret.put("width", outWidth);
      ret.put("height", outHeight);
      pendingResult.success(ret);
      clearMethodCallAndResult();
    }
    else {
      pendingResult.notImplemented();
      clearMethodCallAndResult();
    }
  }
  private HashMap<String, Integer> pt2Json(Point pt) {
    HashMap<String, Integer> ret = new HashMap<>();
    ret.put("x", pt.x);
    ret.put("y", pt.y);
    return ret;
  }

//  private void startCropImage() {
//    if (ContextCompat.checkSelfPermission(this.activity,
//            Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
//            Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(this.activity,
//              new String[]{
//                      Manifest.permission.READ_EXTERNAL_STORAGE,
//                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                      Manifest.permission.CAMERA
//              },
//              REQUEST_CODE_GRANT_PERMISSIONS);
//    } else {
//      cropImage();
//    }
//  }

  private void cropImage() {
    HashMap<String, String> options = this.methodCall.argument(ANDROID_OPTIONS);
    String file = "";
    if (this.methodCall.hasArgument("uri")) {
      String strUri = this.methodCall.argument("uri");
      Uri uri = Uri.parse(strUri);
      file = FileDirectory.getPath(this.context, uri);
    }
    else if (this.methodCall.hasArgument("file")) {
      file = this.methodCall.argument("file");
    }
    String channel = this.methodCall.argument("channel");
    if (channel == null || channel.isEmpty()) {
      finishWithError("param error", "miss channel param");
      return;
    }
    if (file == null || file.isEmpty()) {
//      finishWithError("param error", "must has uri or file param");
//      return;
      file = "";
    }

    //不再支持
//    CropActivity.messenger = messenger;
//    activity.startActivityForResult(CropActivity.getJumpIntent(context, channel, file), 101);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    return false;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_CODE_GRANT_PERMISSIONS && permissions.length == 3) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED
              && grantResults[1] == PackageManager.PERMISSION_GRANTED
              && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
        cropImage();
      } else {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ||
//                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
//                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
//          finishWithError("PERMISSION_DENIED", "Read, write or camera permission was not granted");
//        } else{
//          finishWithError("PERMISSION_PERMANENTLY_DENIED", "Please enable access to the storage and the camera.");
//        }
//        return false;
      }
      return true;
    }
    finishWithError("PERMISSION_DENIED", "Read, write or camera permission was not granted");
    return false;
  }

  private void finishWithAlreadyActiveError(MethodChannel.Result result) {
    if (result != null)
      result.error("already_active", "cropper is already active", null);
  }
  private void finishWithError(String errorCode, String errorMessage) {
    if (pendingResult != null)
      pendingResult.error(errorCode, errorMessage, null);
    clearMethodCallAndResult();
  }
  private void finishWithSuccess() {
    if (pendingResult != null)
      pendingResult.success(true);
    clearMethodCallAndResult();
  }
  private void clearMethodCallAndResult() {
    methodCall = null;
    pendingResult = null;
  }

  private boolean setPendingMethodCallAndResult(
          MethodCall methodCall, MethodChannel.Result result) {
    if (pendingResult != null) {
      return false;
    }

    this.methodCall = methodCall;
    pendingResult = result;
    return true;
  }

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


