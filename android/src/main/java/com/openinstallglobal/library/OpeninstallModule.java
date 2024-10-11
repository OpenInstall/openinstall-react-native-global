package com.openinstallglobal.library;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import io.openinstall.api.OpData;
import io.openinstall.api.OpError;
import io.openinstall.api.OpenInstall;
import io.openinstall.api.ResultCallBack;

public class OpeninstallModule extends ReactContextBaseJavaModule {

    private static final String TAG = "OpenInstallModule";

    public static final String EVENT = "OpeninstallWakeupCallBack";
    private final ReactContext context;
    private Intent wakeupIntent = null;
    private WritableMap wakeupDataHolder = null;
    private boolean registerWakeup = false;
    private boolean initialized = false;

    public OpeninstallModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        OpenInstall.initialize(context);
        reactContext.addActivityEventListener(new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

            }

            @Override
            public void onNewIntent(Intent intent) {
                Log.d(TAG, "onNewIntent");
                getWakeUp(intent, null);
            }
        });
    }

    @Override
    public String getName() {
        return "OpeninstallModule";
    }

    private boolean hasTrue(ReadableMap map, String key) {
        if (map.hasKey(key)) {
            if (map.isNull(key)) return false;
            return map.getBoolean(key);
        }
        return false;
    }

    @ReactMethod
    public void config(ReadableMap readableMap) {
//
//        if (hasTrue(readableMap, "adEnabled")) {
//            builder.adEnabled(true);
//        }
//        if (readableMap.hasKey("oaid")) {
//            builder.oaid(readableMap.getString("oaid"));
//        }
//        if (readableMap.hasKey("gaid")) {
//            builder.gaid(readableMap.getString("gaid"));
//        }
//        if (hasTrue(readableMap, "imeiDisabled")) {
//            builder.imeiDisabled();
//        }
//        if (readableMap.hasKey("imei")) {
//            builder.imei(readableMap.getString("imei"));
//        }
//        if (hasTrue(readableMap, "macDisabled")) {
//            builder.macDisabled();
//        }
//        if (readableMap.hasKey("macAddress")) {
//            builder.macAddress(readableMap.getString("macAddress"));
//        }
//        if (readableMap.hasKey("androidId")) {
//            builder.androidId(readableMap.getString("androidId"));
//        }
//        if (readableMap.hasKey("serialNumber")) {
//            builder.serialNumber(readableMap.getString("serialNumber"));
//        }
//        if (hasTrue(readableMap, "simulatorDisabled")) {
//            builder.simulatorDisabled();
//        }
//        if (hasTrue(readableMap, "storageDisabled")) {
//            builder.storageDisabled();
//        }

    }

    @ReactMethod
    public void init(ReadableMap readableMap) {
        // config
        if (hasTrue(readableMap, "disableFetchAndroidId")) {
            OpenInstall.getInstance().disableFetchAndroidId();
        }
        if (readableMap.hasKey("androidId")) {
            OpenInstall.getInstance().setAndroidId(readableMap.getString("androidId"));
        }
        if (hasTrue(readableMap, "disableFetchClipData")) {
            OpenInstall.getInstance().disableFetchClipData();
        }
        if (readableMap.hasKey("clipData")) {
            String clipDataStr = readableMap.getString("clipData");
            ClipData clipData = ClipData.newPlainText("text", clipDataStr);
            OpenInstall.getInstance().setClipData(clipData);
        }
        if (hasTrue(readableMap, "disableCheckSimulator")) {
            OpenInstall.getInstance().disableCheckSimulator();
        }
        if (readableMap.hasKey("isSimulator")) {
            OpenInstall.getInstance().setSimulator(readableMap.getBoolean("isSimulator"));
        }
        if (readableMap.hasKey("bizID")) {
            OpenInstall.getInstance().setBizID(readableMap.getString("bizID"));
        }

        if (context.hasCurrentActivity()) {
            OpenInstall.getInstance().start(context.getCurrentActivity());
            initialized();
        } else {
            Log.w(TAG, "no activity, please call init method later");
        }
    }

    private void initialized() {
        initialized = true;
        if (wakeupIntent != null) {
            OpenInstall.getInstance().handleDeepLink(wakeupIntent, new ResultCallBack<OpData>() {
                @Override
                public void onResult(OpData opData) {
                    wakeupIntent = null;
                    Log.d(TAG, "getWakeUp : wakeupData = " + opData.toString());
                    WritableMap params = putData2Map(opData);
                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EVENT, params);
                }

                @Override
                public void onError(OpError opError) {
//                    wakeupIntent = null;
//                    WritableMap params = putError2Map(opError);
//                    getReactApplicationContext()
//                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                            .emit(EVENT, params);
                }
            });
        }
    }

    @ReactMethod
    public void getWakeUp(final Callback successBack) {
        registerWakeup = true;
        if (wakeupDataHolder != null) {
            // 调用getWakeUp注册前就处理过拉起参数了(onNewIntent)
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(EVENT, wakeupDataHolder);
            wakeupDataHolder = null;
        } else {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                Intent intent = currentActivity.getIntent();
                getWakeUp(intent, successBack);
            }
        }
    }

    // 可能在用户调用初始化之前调用
    private void getWakeUp(Intent intent, final Callback callback) {
        if (initialized) {
            OpenInstall.getInstance().handleDeepLink(intent, new ResultCallBack<OpData>() {
                @Override
                public void onResult(OpData opData) {
                    WritableMap params = putData2Map(opData);
                    if (registerWakeup) {
                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(EVENT, params);
                    } else {
                        wakeupDataHolder = params;
                    }
                }

                @Override
                public void onError(OpError opError) {

                }
            });
        } else {
            wakeupIntent = intent;
        }
    }

    @ReactMethod
    public void getInstall(Integer time, final Callback callback) {
        OpenInstall.getInstance().getInstallParam(time, new ResultCallBack<OpData>() {
            @Override
            public void onResult(OpData opData) {
                WritableMap params = putData2Map(opData);
                callback.invoke(params);
            }

            @Override
            public void onError(OpError opError) {
                WritableMap params = putError2Map(opError);
                callback.invoke(params);
            }

        });
    }


    @ReactMethod
    public void reportRegister() {
        OpenInstall.getInstance().register();
    }

//    @ReactMethod
//    public void reportEffectPoint(String pointId, Integer pointValue) {
//        if (!TextUtils.isEmpty(pointId) && pointValue >= 0) {
//            OpenInstall.reportEffectPoint(pointId, pointValue);
//        }else {
//            Log.w(TAG, "reportEffectPoint 调用失败：pointId 不能为空，pointValue 必须大于0");
//        }
//    }

    @ReactMethod
    public void reportEffectPoint(String pointId, Integer pointValue, ReadableMap readableMap) {
        if (!TextUtils.isEmpty(pointId) && pointValue >= 0) {
            HashMap<String, String> extraMap = null;
            if (readableMap != null) {
                extraMap = new HashMap<>();
                HashMap<String, Object> map = readableMap.toHashMap();
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    String name = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) continue;
                    if (value instanceof String) {
                        extraMap.put(name, (String) value);
                    } else {
                        extraMap.put(name, value.toString());
                    }
                }
            }
            OpenInstall.getInstance().saveEvent(pointId, pointValue, extraMap);
        } else {
            Log.w(TAG, "reportEffectPoint 调用失败：pointId 不能为空，pointValue 必须大于0");
        }
    }

    @ReactMethod
    public void reportShare(String shareCode, String sharePlatform, final Callback callback) {
        if (TextUtils.isEmpty(shareCode) || TextUtils.isEmpty(sharePlatform)) {
            Log.w(TAG, "reportShare 调用失败：shareCode 和 sharePlatform 不能为空");
            WritableMap params = Arguments.createMap();
            params.putBoolean("shouldRetry", false);
            params.putString("message", "shareCode 和 sharePlatform 不能为空");
            callback.invoke(params);
        } else {
            OpenInstall.getInstance().reportShare(shareCode, sharePlatform, new ResultCallBack<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    WritableMap params = Arguments.createMap();
                    params.putBoolean("shouldRetry", false);
                    callback.invoke(params);
                }

                @Override
                public void onError(OpError opError) {
                    WritableMap params = putError2Map(opError);
                    callback.invoke(params);
                }
            });
        }
    }

    private WritableMap putData2Map(OpData appData) {
        WritableMap params = Arguments.createMap();
        params.putBoolean("shouldRetry", false);
        if (appData != null) {
            params.putString("channel", appData.getChannelCode());
            params.putString("data", appData.getBindData());
        }
        return params;
    }

    private WritableMap putError2Map(OpError error) {
        WritableMap params = Arguments.createMap();
        params.putBoolean("shouldRetry", error != null && error.shouldRetry());
        if (error != null) {
            params.putString("message", error.getErrorMsg());
        }
        return params;
    }

}
