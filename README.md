# flutter_smart_cropper

A Flutter plugin use to crop largest rectangle object in picture.

用于裁剪出图像中的最大的矩形，例如身份证，名片，文档的识别

- 矩形识别iOS端使用Photos.framework中的边缘识别器，
- Android使用的是pqpo的[SmartCropper](https://github.com/pqpo/SmartCropper),底层用OpenCV实现

需要配合image和image_picker包使用

更多使用场景参考pqpo的[SmartCropper](https://github.com/pqpo/SmartCropper)

## 使用
flutter版本要求: 1.5

在pubspec.yaml添加依赖

    flutter_smart_cropper: ^0.1.2
在执行"flutter packages get"后，您可以查看包中的example，了解如何使用它。

检测最大矩形代码
```
    var imageFile = await ImagePicker.pickImage(source: ImageSource.gallery);
    String file = imageFile.path;
    RectPoint rectPoint = await FlutterSmartCropper.detectImageRect(file);
```