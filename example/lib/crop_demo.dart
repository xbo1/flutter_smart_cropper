import 'dart:io';
import 'dart:ui';
import 'dart:typed_data';
import 'package:image/image.dart';
import 'package:image_picker/image_picker.dart';
import 'package:flutter_smart_cropper/flutter_smart_cropper.dart'

;
class CropDemo {

  //only for demo
  Future<ByteBuffer> cropImage({String file}) async{
    if (file == null) {
      var imageFile = await ImagePicker.pickImage(source: ImageSource.gallery);
      file = imageFile.path;
    }
    RectPoint rp = await FlutterSmartCropper.detectImageRect(file);
    Image image = decodeImage(File(file).readAsBytesSync());
    if (rp != null) {
      Image newImage = copyRectify(image, topLeft: offset2Point(rp.tl),
          topRight: offset2Point(rp.tr), bottomLeft: offset2Point(rp.bl),
          bottomRight: offset2Point(rp.br));
      return Uint8List.fromList(encodeJpg(newImage)).buffer;
    }

    return null;
  }
  static Point offset2Point(Offset offset) {
    num x = offset.dx;
    num y = offset.dy;
    return Point(x, y);
  }
}