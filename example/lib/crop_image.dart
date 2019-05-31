import 'package:flutter/material.dart';
import 'package:flutter_smart_cropper/flutter_smart_cropper.dart';
import 'dart:io';
import 'dart:typed_data';
import 'cropper_painter.dart';
import 'package:image/image.dart' as PS;

class CropImage extends StatefulWidget {
  final RectPoint rectPoint;
  final String file;
  CropImage(this.file, this.rectPoint);
  @override
  _CropImageState createState() => _CropImageState();
}

class _CropImageState extends State<CropImage> {
  final GlobalKey _repaintKey = new GlobalKey();
  RectPoint rcPoint;
  Image image;
  @override
  void initState() {
    super.initState();
    image = Image.file(File(widget.file));
//    Future.delayed(Duration(milliseconds: 500), ()=> resizeRect());
  }
  PS.Point offset2Point(Offset offset) {
    var newOffset = offset.scale(widget.rectPoint.width/rcPoint.width, widget.rectPoint.height/rcPoint.height);
    num x = newOffset.dx;
    num y = newOffset.dy;
    return PS.Point(x, y);
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          brightness: Brightness.light,
          elevation: 0,
          backgroundColor: Colors.white,
          leading: IconButton(
              icon: Icon(
                Icons.arrow_back_ios,
                color: Color(0xFF1A1A1A),
              ),
              onPressed: () {
                Navigator.pop(context);
              }),
          actions: <Widget>[
            FlatButton(
              child:  Text(
                "确认",
                style: TextStyle(color: Colors.blue, fontSize: 16, fontWeight: FontWeight.normal),
              ),
              onPressed: () {
                PS.Image image = PS.decodeImage(File(widget.file).readAsBytesSync());
                RectPoint rp = rcPoint;
                var newImage = PS.copyRectify(image, topLeft: offset2Point(rp.tl),
                    topRight: offset2Point(rp.tr), bottomLeft: offset2Point(rp.bl),
                    bottomRight: offset2Point(rp.br));
                Navigator.pop(context, Uint8List.fromList(PS.encodeJpg(newImage)));
              },
            )
          ],
        ),
        body: Container(
          child: Padding(
            padding: EdgeInsets.only(left: 13, right: 13),
            child: RepaintBoundary(
              key: _repaintKey,
              child: Stack(
                alignment: Alignment.center,
                children: <Widget>[
                  image,
                  Positioned(
                    child: _buildCanvas(),
                    top: 0.0,
                    bottom: 0.0,
                    left: 0.0,
                    right: 0.0,
                  ),
                ],
              ),
            ),
          ),
        ));
  }

  bool hasResized = false;
  void resizeRect(StateSetter state) {
    if (hasResized) {
      return;
    }
    RenderBox referenceBox =_repaintKey.currentContext.findRenderObject();
//    RenderBox referenceBox = context.findRenderObject();
    if (referenceBox == null || !referenceBox.hasSize) {
      return;
    }
    Size dest = referenceBox.size;
    if (dest.height == 0 || dest.width == 0) {
      return;
    }
    hasResized = true;
    if (widget.rectPoint == null) {
      rcPoint = RectPoint();
      rcPoint.tl = Offset(0, 0);
      rcPoint.tr = Offset(dest.width, 0);
      rcPoint.br = Offset(dest.width, dest.height);
      rcPoint.bl = Offset(0, dest.height);
      rcPoint.width = dest.width.ceil();
      rcPoint.height = dest.height.ceil();
      return;
    }

    Size src = Size(widget.rectPoint.width.toDouble(), widget.rectPoint.height.toDouble());
    rcPoint = RectPoint();
    rcPoint.tl = this.resizePoint(widget.rectPoint.tl, dest, src);
    rcPoint.tr = this.resizePoint(widget.rectPoint.tr, dest, src);
    rcPoint.br = this.resizePoint(widget.rectPoint.br, dest, src);
    rcPoint.bl = this.resizePoint(widget.rectPoint.bl, dest, src);
    rcPoint.width = dest.width.ceil();
    rcPoint.height = dest.height.ceil();
    state(() {

    });
  }
  Offset resizePoint(Offset pt, Size dest, Size src) {
    return pt.scale(dest.width/src.width, dest.height/src.height);
//    double x = pt.dx*dest.width/src.width;
//    double y = pt.dy*dest.height/src.height;
//    return Offset(x, y);
  }

  Widget _buildCanvas() {
    return StatefulBuilder(builder: (context, state) {
      resizeRect(state);
      return CustomPaint(
        painter: CropperPainter(
          rcPoint: rcPoint,
//          strokeColor: selectedColor,
//          strokeWidth: strokeWidth,
        ),
        child: GestureDetector(
          onPanStart: (details) {
            // before painting, set color & strokeWidth.
//            resizeRect(context);
          },
          onPanUpdate: (details) {
            if (rcPoint == null) {
              return;
            }
            RenderBox referenceBox = context.findRenderObject();
            Offset localPosition = referenceBox.globalToLocal(details.globalPosition);

            state(() {
//              points[curFrame].points.add(localPosition);
            rcPoint.updateOffset(localPosition);
            });
          },
          onPanEnd: (details) {
            // preparing for next line painting.

          },
        ),
      );
    });
  }
}
