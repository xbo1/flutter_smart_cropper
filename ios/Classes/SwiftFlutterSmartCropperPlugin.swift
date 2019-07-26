import Flutter
import UIKit
import Photos

public class SwiftFlutterSmartCropperPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_smart_cropper", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterSmartCropperPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "detectImageRect":
            let arguments = call.arguments as? Dictionary<String, AnyObject>
            let identifier:String? = arguments?["identifier"] as? String
            let file:String? = arguments?["file"] as? String
            if (file != nil) {
                //let imgData = try! Data.init(contentsOf: URL.init(string: file!)!)
                let image = UIImage.init(contentsOfFile: file!)//UIImage(data: imgData)
                self.detectImageRect(image: image, result:result)
            }
            else if (identifier != nil) {
                let manager = PHImageManager.default()
                let options = PHImageRequestOptions()
                
                options.deliveryMode = PHImageRequestOptionsDeliveryMode.highQualityFormat
                options.resizeMode = PHImageRequestOptionsResizeMode.exact
                options.isSynchronous = false
                options.isNetworkAccessAllowed = true
                
                let assets: PHFetchResult = PHAsset.fetchAssets(withLocalIdentifiers: [identifier!], options: nil)
                if (assets.count > 0) {
                    let asset: PHAsset = assets[0];
                    let ID: PHImageRequestID = manager.requestImage(
                        for: asset,
                        targetSize: PHImageManagerMaximumSize,
                        contentMode: PHImageContentMode.aspectFill,
                        options: options,
                        resultHandler: {
                            (image: UIImage?, info) in
                            self.detectImageRect(image: image, result:result)
                    })
                    
                    if(PHInvalidImageRequestID != ID) {
                        result([])
                    }
                }
            }else {
                result([])
            }
            break
        default:
            result(FlutterMethodNotImplemented)
        }
        result("iOS " + UIDevice.current.systemVersion)
    }
    
    private func detectImageRect(image:UIImage?,result: @escaping FlutterResult) {
        if let img:UIImage = image {
            let width = image?.cgImage?.width ?? 0
            let height = image?.cgImage?.height ?? 0
            if (width == 0 || height == 0) {
                result([])
                return
            }
            var ciImg:CIImage = CIImage.init(cgImage: img.cgImage!)
            ciImg = CIFilter.init(name: "CIColorControls", parameters: ["inputContrast":1.1,kCIInputImageKey:ciImg])?.outputImage ?? ciImg
            // 用高精度边缘识别器 识别特征
            let features:Array<CIRectangleFeature> = self.getDetector().features(in: ciImg) as! Array<CIRectangleFeature>
            let biggestRect = self.biggestRectangleInRectangles(rectangles: features)
            if biggestRect != nil {
                result(["tl":self.pt2Json(pt: (biggestRect?.bottomLeft)!), "tr":self.pt2Json(pt: (biggestRect?.bottomRight)!), "bl":self.pt2Json(pt: (biggestRect?.topLeft)!), "br":self.pt2Json(pt: (biggestRect?.topRight)!), "width":width, "height":height])
                return
            }
            else {
                result(["tl":["x":0,"y":0], "tr":["x":width,"y":0], "bl":["x":0,"y":height], "br":["x":width,"y":height], "width":width, "height":height])
                return;
            }
        }
        result([])
    }
    private func pt2Json(pt: CGPoint)-> Dictionary<String,CGFloat> {
        return ["x":pt.x, "y":pt.y]
    }
    
    var detector:CIDetector? = nil
    private func getDetector() -> CIDetector {
        if (detector == nil) {
            detector = CIDetector.init(ofType: CIDetectorTypeRectangle, context: nil, options: [CIDetectorAccuracy : CIDetectorAccuracyHigh])
        }
        return detector!
    }
    private func biggestRectangleInRectangles(rectangles:Array<CIRectangleFeature>) -> CIRectangleFeature?
    {
        if (rectangles.count == 0) {
            return nil
        }
        
        var halfPerimiterValue:Float = 0.0;
        
        var biggestRectangle = rectangles.first
        
        for rect in rectangles
        {
            let p1 = rect.topLeft;
            let p2 = rect.topRight;
            let width = hypotf(Float(p1.x - p2.x), Float(p1.y - p2.y));
            
            let p3 = rect.topLeft;
            let p4 = rect.bottomLeft;
            let height = hypotf(Float(p3.x - p4.x), Float(p3.y - p4.y));
            
            let currentHalfPerimiterValue = height + width;
            
            if (halfPerimiterValue < currentHalfPerimiterValue)
            {
                halfPerimiterValue = currentHalfPerimiterValue;
                biggestRectangle = rect;
            }
        }
        
        return biggestRectangle;
    }
}
