import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:flutter/services.dart';
class RectPoint {
//  RectPoint(Offset tl, Offset tr, Offset bl, Offset br) {
//    this.tl = tl;
//    this.tr = tr;
//    this.bl = bl;
//    this.br = br;
//  }
  Offset tl;
  Offset tr;
  Offset bl;
  Offset br;
  int width;
  int height;

  void updateOffset(Offset pt) {
    double dtl = distanceSquared(pt, tl);
    double dtr = distanceSquared(pt, tr);
    double dbl = distanceSquared(pt, bl);
    double dbr = distanceSquared(pt, br);
    double min = isMin(dtl, dtr, dbl, dbr);
    if (dtl == min) {
      tl = pt;
    }
    if (dtr == min) {
      tr = pt;
    }
    if (dbl == min) {
      bl = pt;
    }
    if (dbr == min) {
      br = pt;
    }
  }
  double distanceSquared(Offset pt1, Offset pt2){
    return (pt1.dx-pt2.dx)*(pt1.dx-pt2.dx) + (pt1.dy-pt2.dy)*(pt1.dy-pt2.dy);
  }
  double isMin(dtl, dtr, dbl, dbr) {
    double ret = dtl;
    if (ret > dtr) {
      ret = dtr;
    }
    if (ret > dbl) {
      ret = dbl;
    }
    if (ret > dbr) {
      ret = dbr;
    }
    return ret;
  }

  @override
  String toString() {
    return "tl:$tl,tr:$tr,bl:$bl,br:$br,width:$width,height:$height";
  }
}

class FlutterSmartCropper {
  static const MethodChannel _channel =
      const MethodChannel('flutter_smart_cropper');

  static Future<RectPoint> detectImageRect(String file) async {
    if (file == null || file.isEmpty) {
      return null;
    }
    var params = {'file':file};
    Map ret = await _channel.invokeMethod("detectImageRect", params);
    if (ret.isNotEmpty) {
      RectPoint pt = json2RectPoint(ret);
      return pt;
    }
    return null;
  }


  static Future<String> get platformVersion async {
//    final String version = await _channel.invokeMethod('getPlatformVersion');
    return "0.0.1";
  }

  //not support anymore
//  static Future<ByteBuffer> cropImageNative({String file}) async {
//    if (!Platform.isAndroid) {
//      throw new UnsupportedError('only support android');
//    }
//
////    if ((uri == null || uri.isEmpty) && (file == null || file.isEmpty)) {
////      throw new ArgumentError('uri or file must set one at least');
////    }
//    Completer completer = new Completer<ByteData>();
//    String channel = 'flutter_smart_cropper/image/crop';
//    BinaryMessages.setMessageHandler(channel, (ByteData message) {
//      completer.complete(message.buffer);
//      BinaryMessages.setMessageHandler(channel, null);
//    });
//
//    var params = {
//      "channel": channel
//    };
//    if (file != null) {
//      params['file'] = file;
//    }
//    await _channel.invokeMethod("cropImage", params);
//    return completer.future;
//  }

  static Offset json2Offset(json) {
    num x = json['x'];
    num y = json['y'];
    return Offset(x.toDouble(), y.toDouble());
  }

  static RectPoint json2RectPoint(Map ret) {
    RectPoint pt = RectPoint();
    pt.tl = json2Offset(ret['tl']);
    pt.tr = json2Offset(ret['tr']);
    pt.bl = json2Offset(ret['bl']);
    pt.br = json2Offset(ret['br']);
    //颠倒上下
    if (Platform.isIOS) {
      Offset tl = pt.tl;
      Offset tr = pt.tr;
      Offset bl = pt.bl;
      Offset br = pt.br;
      pt.tl = Offset(bl.dx, tl.dy);
      pt.tr = Offset(br.dx, tr.dy);
      pt.bl = Offset(tl.dx, bl.dy);
      pt.br = Offset(tr.dx, br.dy);
    }
    else {

    }
    pt.width = ret["width"];
    pt.height = ret["height"];
    return pt;
  }
}
