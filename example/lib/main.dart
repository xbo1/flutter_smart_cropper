import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_smart_cropper/flutter_smart_cropper.dart';
import 'package:image_picker/image_picker.dart';
import 'crop_image.dart';

void main() => runApp(Home());

class Home extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: MyApp()
    );
  }
}


class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterSmartCropper.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'pushed times:',
            ),
            bytes!=null ? Image.memory(bytes):Icon(Icons.error),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: toCropImage,
        tooltip: 'Choose Image',
        child: Icon(Icons.add),
      ), //
    );
  }
  Uint8List bytes;
  toCropImage() async {
//     ByteBuffer data = await FlutterSmartCropper.cropImage();
//     bytes = Uint8List.view(data);
//     setState(() {
//
//     });
    var imageFile = await ImagePicker.pickImage(source: ImageSource.gallery);
    String file = imageFile.path;
    RectPoint rectPoint = await FlutterSmartCropper.detectImageRect(file);

    var bts = await Navigator.push(context, MaterialPageRoute(builder: (BuildContext context)=>CropImage(file, rectPoint)));
    if (bts != null) {
      setState(() {
        bytes = bts;
      });
    }
  }



}
