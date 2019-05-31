import 'package:flutter/material.dart';

import 'package:flutter_smart_cropper/flutter_smart_cropper.dart';

class CropperPainter extends CustomPainter {
  final RectPoint rcPoint;
  Paint painter;
//  final double strokeWidth;
//  final Color strokeColor;
  CropperPainter({
    @required this.rcPoint,
//    @required this.strokeColor,
//    @required this.strokeWidth,
  }) {
    painter = Paint()
      ..color = Colors.red//strokeColor
      ..strokeWidth = 2//strokeWidth
      ..strokeCap = StrokeCap.round;
  }
  @override
  void paint(Canvas canvas, Size size) {
    if (rcPoint == null) {
      return;
    }
    canvas.drawLine(rcPoint.tl, rcPoint.tr, painter);
    canvas.drawLine(rcPoint.tr, rcPoint.br, painter);
    canvas.drawLine(rcPoint.br, rcPoint.bl, painter);
    canvas.drawLine(rcPoint.bl, rcPoint.tl, painter);
  }


  @override
  bool shouldRepaint(CropperPainter other) => true;
}
