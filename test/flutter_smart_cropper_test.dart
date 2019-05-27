import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_smart_cropper/flutter_smart_cropper.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_smart_cropper');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterSmartCropper.platformVersion, '42');
  });
}
