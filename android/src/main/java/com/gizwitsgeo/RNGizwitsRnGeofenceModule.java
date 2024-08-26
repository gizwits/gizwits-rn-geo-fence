package com.gizwitsgeo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RNGizwitsRnGeofenceModule extends ReactContextBaseJavaModule implements ActivityEventListener,LifecycleEventListener {
    private String themeInfo;
    public  int REQUEST_CODE = 123;
    ReactApplicationContext reactContext;

    private Callback getCurrentLocationCallback;
    private Callback getAddressInfoCallback;
    private Callback pickAddressCallback;


    private static final int AUTHORIZATION_STATUS_NOT_DETERMINED = 0;
    private static final int AUTHORIZATION_STATUS_DENIED = 2;
    private static final int AUTHORIZATION_STATUS_AUTHORIZED_ALWAYS = 3;

    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGTITUDE = "longitude";

    public RNGizwitsRnGeofenceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNGizwitsRnGeofence";
    }


     @ReactMethod
    public void setThemeInfo(ReadableMap args,Callback callback) {
        JSONObject obj = readable2JsonObject(args);
        if (obj != null) {
            themeInfo = obj.toString();
        }
//        sendResultEvent(callback,null,null);
    }

    @ReactMethod
    public void authorizationStatus(Callback callback) {
        
    }

    @ReactMethod
    public void getCurrentLocation(Callback callback) throws Exception {
        
    }


    @ReactMethod
    public void getAddressInfo(ReadableMap readableMap, Callback callback) throws Exception {
        
    }

    @ReactMethod
    public void pickAddress(ReadableMap readableMap, Callback callback) {
       
    }


    public JSONObject readable2JsonObject(ReadableMap readableMap) {
        try {
            JSONObject jsonObject = new JSONObject();
            ReadableMapKeySetIterator readableMapKeySetIterator = readableMap.keySetIterator();
            while (readableMapKeySetIterator.hasNextKey()) {
                String key = readableMapKeySetIterator.nextKey();
                if (readableMap.getType(key) == ReadableType.Number) {
//                    try {
//                        jsonObject.put(key, readableMap.getInt(key));
//                    } catch (Exception e) {
                    jsonObject.put(key, readableMap.getDouble(key));
//                    }
                } else if (readableMap.getType(key) == ReadableType.Map) {
                    jsonObject.put(key, readable2JsonObject(readableMap.getMap(key)));
                } else if (readableMap.getType(key) == ReadableType.String) {
                    jsonObject.put(key, readableMap.getString(key));
                } else if (readableMap.getType(key) == ReadableType.Boolean) {
                    jsonObject.put(key, readableMap.getBoolean(key));
                } else if (readableMap.getType(key) == ReadableType.Array) {
                    jsonObject.put(key, readable2jsonArray(readableMap.getArray(key)));
                } else {
                    jsonObject.put(key, null);
                }
            }
            return jsonObject;
        } catch (JSONException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public JSONArray readable2jsonArray(ReadableArray readableArray) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < readableArray.size(); i++) {
                if (readableArray.getType(i) == ReadableType.Number) {
//                    try {
//                        jsonArray.put(i, readableArray.getInt(i));
//                    } catch (Exception e) {
                    jsonArray.put(i, readableArray.getDouble(i));
//                    }
                } else if (readableArray.getType(i) == ReadableType.Map) {
                    jsonArray.put(i, readable2JsonObject(readableArray.getMap(i)));
                } else if (readableArray.getType(i) == ReadableType.String) {
                    jsonArray.put(i, readableArray.getString(i));
                } else if (readableArray.getType(i) == ReadableType.Boolean) {
                    jsonArray.put(i, readableArray.getBoolean(i));
                } else if (readableArray.getType(i) == ReadableType.Array) {
                    jsonArray.put(i, readable2jsonArray(readableArray.getArray(i)));
                } else {
                    jsonArray.put(i, null);
                }
            }

            return jsonArray;
        } catch (JSONException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private void sendResultEvent(Callback callbackContext, JSONObject dataDict, JSONObject errDict) {
        if (callbackContext == null) {
            return;
        }
        try {
            if (dataDict != null) {
                WritableMap successMap = jsonObject2WriteableMap(dataDict);
                callbackContext.invoke(null, successMap);
            } else {
                WritableMap errorMap = jsonObject2WriteableMap(errDict);
                callbackContext.invoke(errorMap, null);
            }
        } catch (Exception e) {

        }

    }


    public WritableMap jsonObject2WriteableMap(JSONObject jsonObject) {
        try {
            WritableMap writableMap = Arguments.createMap();
            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Object object = jsonObject.get(key);
                if (object instanceof String) {
                    writableMap.putString(key, jsonObject.getString(key));
                } else if (object instanceof Boolean) {
                    writableMap.putBoolean(key, jsonObject.getBoolean(key));
                } else if (object instanceof Integer) {
                    writableMap.putInt(key, jsonObject.getInt(key));
                } else if (object instanceof Double) {
                    writableMap.putDouble(key, jsonObject.getDouble(key));
                } else if (object instanceof JSONObject) {
                    writableMap.putMap(key, jsonObject2WriteableMap(jsonObject.getJSONObject(key)));
                } else if (object instanceof JSONArray) {
                    writableMap.putArray(key, jsonArray2WriteableArray(jsonObject.getJSONArray(key)));
                } else {
                    writableMap.putNull(key);
                }
            }
            return writableMap;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public WritableArray jsonArray2WriteableArray(JSONArray jsonArray) {
        try {
            WritableArray writableArray = Arguments.createArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                Object object = jsonArray.get(i);
                if (object instanceof String) {
                    writableArray.pushString(jsonArray.getString(i));
                } else if (object instanceof Boolean) {
                    writableArray.pushBoolean(jsonArray.getBoolean(i));
                } else if (object instanceof Integer) {
                    writableArray.pushInt(jsonArray.getInt(i));
                } else if (object instanceof Double) {
                    writableArray.pushDouble(jsonArray.getDouble(i));
                } else if (object instanceof JSONObject) {
                    writableArray.pushMap(jsonObject2WriteableMap(jsonArray.getJSONObject(i)));
                } else if (object instanceof JSONArray) {
                    writableArray.pushArray(jsonArray2WriteableArray(jsonArray.getJSONArray(i)));
                } else {
                    writableArray.pushNull();
                }
            }

            return writableArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GCJ-02 转换为 WGS-84
     */
    @ReactMethod
    private void transformFromGCJToWGS(ReadableMap readableMap,Callback callback) {
        
    }

    @ReactMethod
    private void transformFromWGSToGCJ(ReadableMap readableMap,Callback callback) {
        
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
//        if (pickAddressCallback == null) {
//            return;
//        }
//        if (requestCode == REQUEST_CODE) {
            JSONObject jsonObject = new JSONObject();
            try {
                if (resultCode == Activity.RESULT_OK && intent != null) {

                  String code = intent.getStringExtra("result");
                    jsonObject = new JSONObject(code);
                    sendResultEvent(pickAddressCallback, jsonObject, null);
                } else if (intent != null) {
                    String code = intent.getStringExtra("result");
                    jsonObject.put("code",code);
                    sendResultEvent(pickAddressCallback,null,jsonObject);
                } else {
                    jsonObject.put("code",9);
                    // goolge play 服务不可用
                    sendResultEvent(pickAddressCallback,null,jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.reactContext.removeActivityEventListener(this);

//        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {

    }
}
