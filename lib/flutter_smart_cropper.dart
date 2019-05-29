import 'dart:async';

import 'package:flutter/services.dart';

class FlutterSmartCropper {
  static const MethodChannel _channel =
      const MethodChannel('flutter_smart_cropper');

  static Future<String> get platformVersion async {
//    final String version = await _channel.invokeMethod('getPlatformVersion');
    return "0.0.1";
  }

  static Future<ByteData> cropImage({String uri, String file}) async {

//    if ((uri == null || uri.isEmpty) && (file == null || file.isEmpty)) {
//      throw new ArgumentError('uri or file must set one at least');
//    }

    Completer completer = new Completer<ByteData>();
    String channel = 'flutter_smart_cropper/image/crop';
    BinaryMessages.setMessageHandler(channel, (ByteData message) {
      completer.complete(message);
      BinaryMessages.setMessageHandler(channel, null);
    });

    var params = {
      "channel": channel
    };
    if (file != null) {
      params['file'] = file;
    }
    if (uri != null) {
      params['uri'] = uri;
    }
    await _channel.invokeMethod("cropImage", params);
    return completer.future;
  }
}
