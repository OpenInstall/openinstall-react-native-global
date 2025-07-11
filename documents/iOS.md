### iOS 手动集成方式

- 在React Native < 0.60版本下， `react-native link` 之后，打开 iOS 工程  
- 在React Native >= 0.60版本下，通过pod安装插件后，打开iOS工程  

也可不执行 `react-native link` 或cocoapod安装，通过手动拖拽openinstall-react-native-global插件 `RCTOpenInstall.xcodeproj` 到xcode工程中，这部分可参考[官方文档](https://reactnative.cn/docs/linking-libraries-ios/)或[facebook英文文档](https://facebook.github.io/react-native/docs/linking-libraries-ios)

#### 1 相关配置

##### （1）初始化配置
在 `Info.plist` 文件中配置 appKey 键值对，如下：
``` xml
<key>com.openinstall.APP_KEY</key>
<string>从openinstall官网后台获取应用的appkey</string>
```
##### （2）universal links配置（iOS9以后推荐使用）

对于iOS，为确保能正常跳转，AppID必须开启Associated Domains功能，请到[苹果开发者网站](https://developer.apple.com)，选择Certificate, Identifiers & Profiles，选择相应的AppID，开启Associated Domains。注意：当AppID重新编辑过之后，需要更新相应的mobileprovision证书。如果已经开启过Associated Domains功能，进行下面操作：

- 在左侧导航器中点击您的项目
- 选择 `Capabilities` 标签
- 打开 `Associated Domains` 开关
- 添加 openinstall 官网后台中应用对应的关联域名（openinstall应用控制台->iOS集成->iOS应用配置->关联域名(Associated Domains)）

##### （3）scheme配置
- `scheme` 的值请在openinstall控制台获取（openinstall应用控制台->iOS集成->iOS应用配置）
在 `Info.plist` 文件中，在 `CFBundleURLTypes` 数组中添加应用对应的 `scheme`，或者在工程“TARGETS-Info-URL Types”里快速添加  
（scheme的值详细获取位置：openinstall应用控制台->iOS集成->iOS应用配置）  

``` xml
 <key>CFBundleURLTypes</key>
 <array>
  <dict>
    <key>CFBundleTypeRole</key>
    <string>Editor</string>
    <key>CFBundleURLName</key>
    <string>openinstall</string>
    <key>CFBundleURLSchemes</key>
    <array>
      <string>"从openinstall官网后台获取应用的scheme"</string>
    </array>
  </dict>
 </array>
```

##### 注意：

- 在 iOS 工程中如果找不到头文件可能要在 TARGETS-> BUILD SETTINGS -> Search Paths -> Header Search Paths 添加如下如路径：
````
$(SRCROOT)/../node_modules/openinstall-react-native-global/ios/RCTOpenInstall
````

#### 2 相关代码

（1）AppDelegate.h 中添加如下代码，导入头文件
```
#import <openinstall-react-native-global/RCTOpenInstall.h>
```

（2）初始化sdk的代码  
**为配合隐私政策合规方案，即App首次安装需要用户手动同意后，才可初始化SDK**  
初始化代码由用户主动调用方式，在react native端入口js文件中（例如App.js文件）调用init方法**  


（3）scheme相关代码  
AppDelegate.mm 里面添加如下代码：  
```
//iOS9以上，会优先走这个方法
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options{
 //openURL1
  AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
  RCTBridge *bridge = delegate.bridge;
  RCTOpenInstall *module = [bridge moduleForClass:[RCTOpenInstall class]];
  [module handLinkURL:url];
  
  //其它代码...
 return YES;
}

（4）universal link相关代码  
AppDelegate.m 里面添加如下代码：  
```
- (BOOL)application:(UIApplication *)application continueUserActivity:(nonnull NSUserActivity *)userActivity restorationHandler:(nonnull void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler{
  //univeral link
  //RCTBridge *bridge = [[RCTBridge alloc]initWithDelegate:self launchOptions:nil];
  AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
  RCTBridge *bridge = delegate.bridge;
  RCTOpenInstall *module = [bridge moduleForClass:[RCTOpenInstall class]];
  [module continueUserActivity:userActivity];
  
  //其它代码...
  return YES;
 }
```
