# flutter_smart_cropper_example

## 使用
flutter版本要求: 1.2

在pubspec.yaml添加依赖

    flutter_smart_cropper: ^0.1.1

检测最大矩形代码
```
    var imageFile = await ImagePicker.pickImage(source: ImageSource.gallery);
    String file = imageFile.path;
    RectPoint rectPoint = await FlutterSmartCropper.detectImageRect(file);
```

裁剪矩形图片
```
//该Image是image包的类，不是flutter自带的Image
//RectPoint rp; rp是裁剪后的大小
Image image = decodeImage(File(file).readAsBytesSync());
var newImage = copyRectify(image, topLeft: offset2Point(rp.tl),
    topRight: offset2Point(rp.tr), bottomLeft: offset2Point(rp.bl),
    bottomRight: offset2Point(rp.br));
```