#import "FlutterSmartCropperPlugin.h"
#import <flutter_smart_cropper/flutter_smart_cropper-Swift.h>

@implementation FlutterSmartCropperPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterSmartCropperPlugin registerWithRegistrar:registrar];
}
@end
