package com.reactlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.telecom.Call;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.gizwits.amap.AMapActivity;
import com.gizwits.amap.utils.GPSUtil;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class RNGizwitsRnGeofenceModule extends ReactContextBaseJavaModule implements ActivityEventListener, AMapLocationListener, GeocodeSearch.OnGeocodeSearchListener {
    private String themeInfo;
    public static final int REQUEST_CODE = 0x0ba7c0de;
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
        this.reactContext.addActivityEventListener(this);

    }

    @Override
    public String getName() {
        return "RNGizwitsRnGeofenceModule";
    }


    @ReactMethod
    public void setThemeInfo(ReadableMap args) {
        JSONObject obj = readable2JsonObject(args);
        if (obj != null) {
            themeInfo = obj.toString();
        }
    }

    @ReactMethod
    public void authorizationStatus(Callback callback) {
        int i = ContextCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION);
        JSONObject jsonObject = new JSONObject();
        try {
            if (i != PackageManager.PERMISSION_GRANTED) {
                jsonObject.put("status", AUTHORIZATION_STATUS_AUTHORIZED_ALWAYS);
                sendResultEvent(callback, jsonObject, null);
            } else {
                jsonObject.put("status", AUTHORIZATION_STATUS_NOT_DETERMINED);
                sendResultEvent(callback, jsonObject, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void getCurrentLocation(Callback callback) {
        getCurrentLocationCallback = callback;
        AMapLocationClient aMapLocationClient = new AMapLocationClient(reactContext);
        aMapLocationClient.setLocationListener(this);
        AMapLocationClientOption clientOption = new AMapLocationClientOption();
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        clientOption.setOnceLocation(true);
        clientOption.setLocationCacheEnable(false);
        aMapLocationClient.setLocationOption(clientOption);
        aMapLocationClient.startLocation();
    }


    @ReactMethod
    public void getAddressInfo(ReadableMap readableMap, Callback callback) {
        JSONObject args = readable2JsonObject(readableMap);
        getAddressInfoCallback = callback;
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        if (GPSUtil.isInArea(lat, lon)) {
            getGPSAddressInfoFromAMap(lat, lon);
        } else {
            getGPSAdrresInfoFromGoogleMap(lat, lon);
        }
    }

    private void getGPSAddressInfoFromAMap(double lat, double lon) {
        GeocodeSearch geocodeSearch = new GeocodeSearch(reactContext);
        geocodeSearch.setOnGeocodeSearchListener(this);
        LatLonPoint latLngPoint = new LatLonPoint(lat, lon);
        RegeocodeQuery query = new RegeocodeQuery(latLngPoint, 200, GeocodeSearch.GPS);
        geocodeSearch.getFromLocationAsyn(query);
    }

    private void getGPSAdrresInfoFromGoogleMap(double lat, double lon) {
        Geocoder geocoder = new Geocoder(reactContext, Locale.getDefault());
        JSONObject json = new JSONObject();
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && addresses.size() > 0) {
                Address addr = addresses.get(0);
                try {
                    JSONArray js = new JSONArray();
                    json.put("FormattedAddressLines", js.put(addresses.get(0).getAddressLine(0)));
                    json.put("Street", "");
                    json.put("Thoroughfare", "");
                    json.put("Name", addr.getFeatureName());
                    json.put("City", addr.getLocality());
                    json.put("Country", addr.getCountryName());
                    json.put("State", addr.getAdminArea());
                    json.put("SubLocality", addr.getSubLocality());
                    json.put("CountryCode", addr.getCountryCode());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("地址解析", json.toString());
                sendResultEvent(getAddressInfoCallback, json, null);
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            json.put("code", 6);
            sendResultEvent(getAddressInfoCallback, null, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void pickAddress(ReadableMap readableMap, Callback callback) {
        Intent mapIntent = new Intent(reactContext, AMapActivity.class);
        pickAddressCallback = callback;
        // add config as intent extras
        JSONObject args = readable2JsonObject(readableMap);
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        mapIntent.putExtra(KEY_LATITUDE, lat);
        mapIntent.putExtra(KEY_LONGTITUDE, lon);
        mapIntent.putExtra("action", "pickAddress");
        mapIntent.putExtra("themeInfo", themeInfo);

        // avoid calling other phonegap apps
        mapIntent.setPackage(reactContext.getPackageName());
        reactContext.startActivityForResult(mapIntent, REQUEST_CODE, null);

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
        JSONObject args = readable2JsonObject(readableMap);
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        double[] latlon = GPSUtil.gcj02_To_Gps84(lat, lon);
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_LATITUDE, latlon[0]);
            json.put(KEY_LONGTITUDE, latlon[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("jason", "transformFromGCJToWGS :" + json.toString());
        sendResultEvent(callback, json, null);
    }

    @ReactMethod
    private void transformFromWGSToGCJ(ReadableMap readableMap,Callback callback) {
        JSONObject args = readable2JsonObject(readableMap);
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        LatLng latLng = new LatLng(lat, lon);
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(latLng);
        LatLng desLatLng = converter.convert();
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_LATITUDE, desLatLng.latitude);
            json.put(KEY_LONGTITUDE, desLatLng.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("jason", "transformFromWGSToGCJ :"+json.toString());
        sendResultEvent(callback,json,null);
    }

    /**
     * GCJ-02 转换为 BD-09
     */
    private void transformFromGCJToBaidu(ReadableMap readableMap,Callback callback) {
        JSONObject args = readable2JsonObject(readableMap);
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        double[] latlon = GPSUtil.gcj02_To_Bd09(lat, lon);
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_LATITUDE, latlon[0]);
            json.put(KEY_LONGTITUDE, latlon[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendResultEvent(callback,json,null);
    }

    /**
     * BD-09 转换为 GCJ-02
     */
    private void transformFromBaiduToGCJ(ReadableMap readableMap,Callback callback) {
        JSONObject args = readable2JsonObject(readableMap);
        double lat = 0;
        double lon = 0;
        try {
            lat = args.getDouble(KEY_LATITUDE);
            lon = args.getDouble(KEY_LONGTITUDE);
        } catch (JSONException e) {
            Log.i("CordovaLog", e.getLocalizedMessage());
        }
        LatLng latLng = new LatLng(lat, lon);
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.BAIDU);
        converter.coord(latLng);
        LatLng desLatLng = converter.convert();
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_LATITUDE, desLatLng.latitude);
            json.put(KEY_LONGTITUDE, desLatLng.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendResultEvent(callback,json,null);
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (getCurrentLocationCallback == null) {
            return;
        }
        double[] latlon = GPSUtil.gcj02_To_Gps84(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_LATITUDE, latlon[0]);
            json.put(KEY_LONGTITUDE, latlon[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("jason", "transformFromGCJToWGS :" + json.toString());
        sendResultEvent(getCurrentLocationCallback, json, null);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        if (getCurrentLocationCallback == null) {
            return;
        }
        try {
            JSONObject json = new JSONObject();

            if (rCode == 1000) {
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();

                JSONArray js = new JSONArray();
                json.put("FormattedAddressLines", js.put(regeocodeAddress.getFormatAddress()));
//				json.put("Street", regeocodeAddress.getRoads().get(0).getName());
//				json.put("Thoroughfare", regeocodeAddress.getRoads().get(0).getName());
                json.put("Street", "");
                json.put("Thoroughfare", "");
                json.put("Name", regeocodeAddress.getBuilding());
                json.put("City", regeocodeAddress.getCity());
                json.put("Country", regeocodeAddress.getCountry());
                json.put("State", regeocodeAddress.getProvince());
                json.put("SubLocality", regeocodeAddress.getDistrict());
                json.put("CountryCode", "");

                Log.e("jason", "address:" + json.toString());
                sendResultEvent(getAddressInfoCallback, json, null);
            } else if (rCode == 1008) {
                json.put("code", 8);
                sendResultEvent(getAddressInfoCallback, null, json);
            } else if (rCode == 1002) {
                //key 不正确或过期
                json.put("code", 7);
                sendResultEvent(getAddressInfoCallback, null, json);
            } else {
                json.put("code", 6);
                sendResultEvent(getAddressInfoCallback, null, json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (pickAddressCallback == null) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            JSONObject jsonObject = null;
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
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
