package com.xbo1.flutter_smart_cropper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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
      startCropImage();
      finishWithSuccess();
    } else {
      pendingResult.notImplemented();
      clearMethodCallAndResult();
    }
  }

  private void startCropImage() {
    if (ContextCompat.checkSelfPermission(this.activity,
            Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.activity,
            Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this.activity,
              new String[]{
                      Manifest.permission.READ_EXTERNAL_STORAGE,
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      Manifest.permission.CAMERA
              },
              REQUEST_CODE_GRANT_PERMISSIONS);
    } else {
      cropImage();
    }
  }

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

    CropActivity.messenger = messenger;
    activity.startActivityForResult(CropActivity.getJumpIntent(context, channel, file), 101);
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
          finishWithError("PERMISSION_DENIED", "Read, write or camera permission was not granted");
        } else{
          finishWithError("PERMISSION_PERMANENTLY_DENIED", "Please enable access to the storage and the camera.");
        }
        return false;
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
}
