//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build.VERSION;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.provider.Settings.SettingNotFoundException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CoordinateConverter.CoordType;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;

import com.gizwits.amap.bean.AddressItem;
import com.gizwits.amap.utils.ColorUtil;
import com.gizwits.amap.utils.GPSUtil;
import com.gizwits.amap.utils.UIUtils;
import com.gizwits.amap.utils.GPSPresenter;
import com.gizwits.amap.utils.GPSInterface;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gizwitsgeo.R;
import com.gizwitsgeo.R.id;
import com.gizwitsgeo.R.layout;
import com.gizwitsgeo.R.mipmap;


import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public class AMapActivity extends Activity implements OnCameraMoveListener, OnMapReadyCallback, OnCameraIdleListener, AMapLocationListener, OnGeocodeSearchListener, InfoWindowAdapter, com.google.android.gms.maps.GoogleMap.InfoWindowAdapter, OnClickListener, GPSInterface {
  private static final int PERMISSION_CODE = 100;
  private static final String TAG = "AmapActivity";
  public static final String ADDRESS_ITEM = "addressItem";
  public static final String CITY = "city";
  public static final String THEME_COLOR = "themeColor";
  public static final String NAVI_BG = "navi_bg";
  public static final String TITLE_COLOR = "titleColor";
  public static final String TITLE = "title";
  public static final String RIGHT_TITLE = "right_title";
  private static final String URL = "url";
  private static final String TOKEN = "token";
  private static final String APP_KEY = "appKey";
  private static final String TYPE = "type";
  private static final String VERSION = "version";
  private static final String ACTION = "action";
  private static final String REGION = "region";
  private static final String ADDRESS = "address";
  public static final String THEME_INFO = "themeInfo";
  private static final int RESULT_CANCEL = 1;
  private static final int RESULT_NO_LOCATION_PERMISSION = 2;
  private static final int RESULT_NO_ALWAYS_ALLOW_LOCATION = 3;
  private static final int RESULT_INCORRECT_PARM = 4;
  private static final int RESULT_LIMITED_LOCATION = 5;
  private static final int RESULT_LOCATION_RESOLUTION_FAILURE = 6;
  private static final int REQUEST_SEARCH_ADDRESS = 10000;
  public static final String KEY_IS_AMAP = "isAmap";
  private boolean isModifyAddress;
  public static final String RESULT = "result";
  private MapView mapView;
  private AMap aMap;
  private RelativeLayout rl_top;
  private ImageView iv_left;
  private LinearLayout ll_location;
  private TextView tv_title;
  private TextView tv_right;
  private String action;
  private TextView tv_search;
  private ImageView iv_search;
  private String result;
  private double latitude;
  private double longitude;
  private RelativeLayout rl_map;
  private GoogleMap googlemap;
  private com.google.android.gms.maps.MapView mGoogleMapView;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private Location mLastKnownLocation;
  private float zoom;
  private boolean mIsAmapDisplay;
  private boolean isSuportGooglePlay;
  private AMapLocationClient aMapLocationClient;
  private GeocodeSearch geocodeSearch;
  private String themeColor = "#000000";
  private MarkerOptions markerOptions;
  private String gpsNetworkNotEnabledText = "GPS Network not enabled";
  private String cancelText = "Cancel";
  private String openLocationSettingsText = "Open Location Settings";
  private String permissionNotEnabledContent = "Location Permission not enabled";
  private String openLocationPermissionText = "Open Location Permission";
  private boolean hasPermission = false;
  View infoWindow;
  private GPSPresenter gps_presenter;
  MyLocationStyle myLocationStyle;
  AlertDialog gpsSwitchBuilder;
  AlertDialog locationPermission;

  private Handler handler = new Handler() {
    public void handleMessage(Message message) {
      AMapActivity.this.mapView.setVisibility(View.GONE);
      if (AMapActivity.this.mapView != null) {
        AMapActivity.this.mapView.onDestroy();
      }

    }
  };
  private TextView tv_address;

  public AMapActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      onBackPressed();
      return;
    }
    this.initView();
    this.checkGooglePlayServices();
    this.mapView.onCreate(savedInstanceState);
    this.aMap = this.mapView.getMap();
    aMap.setMyLocationEnabled(true);
    myLocationStyle = new MyLocationStyle();
    myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
    myLocationStyle.showMyLocation(false);
    myLocationStyle.interval(2000);
    aMap.setMyLocationStyle(myLocationStyle);
    this.checkPermisssion(this);
    try {
      this.initMap();
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.initEvent();
    this.initParams(true);
    this.mIsAmapDisplay = true;
    gps_presenter = new GPSPresenter( this , this ) ;

  }

  private void initView() {
    this.setContentView(R.layout.activity_amap);
    this.rl_top = (RelativeLayout) this.findViewById(R.id.rl_top);
    this.iv_left = (ImageView) this.findViewById(R.id.iv_left);
    this.tv_title = (TextView) this.findViewById(R.id.tv_title);
    this.tv_right = (TextView) this.findViewById(R.id.tv_right);
    this.ll_location = (LinearLayout) this.findViewById(R.id.ll_location);
    this.rl_map = (RelativeLayout) this.findViewById(R.id.rl_map);
    this.tv_search = (TextView) this.findViewById(R.id.tv_search);
    this.iv_search = (ImageView) this.findViewById(R.id.iv_search);
    this.mapView = new MapView(this);
    this.mapView.setLayoutParams(new LayoutParams(-1, -1));
    this.rl_map.addView(this.mapView);
  }

  private void initParams(boolean showLocationAlert) {
    Intent intent = this.getIntent();
    this.action = intent.getStringExtra("action");
    String title = intent.getStringExtra("title");
    if (title != null && !"".equals(title)) {
      this.tv_title.setText(title);
    }

    String rightTitle = intent.getStringExtra("right_title");
    if (rightTitle != null && !"".equals(rightTitle)) {
      this.tv_right.setText(rightTitle);
    }

    String strTitleColor = intent.getStringExtra("titleColor");
    if (strTitleColor != null && !"".equals(strTitleColor)) {
      int titleColor = ColorUtil.parseStrColor(strTitleColor);
      this.tv_title.setTextColor(titleColor);
    }

    String themeInfoStr = intent.getStringExtra("themeInfo");
    Log.e("AmapActivity", "themeInfoStr:" + themeInfoStr);
    if (themeInfoStr != null && !"".equals(themeInfoStr)) {
      try {
        JSONObject jb = new JSONObject(themeInfoStr);
        if (jb.has("themeColor")) {
          this.themeColor = jb.getString("themeColor");
          ColorUtil.changeDrawableColor(this.themeColor, this.iv_left);
          ColorUtil.changeDrawableColor(this.themeColor, this.iv_search);
          this.tv_right.setTextColor(ColorUtil.parseStrColor(this.themeColor));
        }

        if (jb.has("navi_bg")) {
          this.rl_top.setBackgroundColor(ColorUtil.parseStrColor(jb.getString("navi_bg")));
        }

        if (jb.has("searchTips")) {
          this.tv_search.setText(jb.getString("searchTips"));
        }

        if (jb.has("titleColor")) {
          this.tv_title.setTextColor(ColorUtil.parseStrColor(jb.getString("titleColor")));
        }

        if (jb.has("title")) {
          this.tv_title.setText(jb.getString("title"));
        }

        if (jb.has("right_title")) {
          this.tv_right.setText(jb.getString("right_title"));
        }

        if (jb.has("gpsNetworkNotEnabledText")) {
          this.gpsNetworkNotEnabledText = jb.getString("gpsNetworkNotEnabledText");
        }

        if (jb.has("openLocationSettingsText")) {
          this.openLocationSettingsText = jb.getString("openLocationSettingsText");
        }

        if (jb.has("permissionNotEnabledContent")) {
          this.permissionNotEnabledContent = jb.getString("permissionNotEnabledContent");
        }

        if (jb.has("openLocationPermissionText")) {
          this.openLocationPermissionText = jb.getString("openLocationPermissionText");
        }

        if (jb.has("cancelText")) {
          this.cancelText = jb.getString("cancelText");
        }
      } catch (JSONException var7) {
        var7.printStackTrace();
      }
    }

    this.latitude = intent.getDoubleExtra("latitude", 0.0D);
    this.longitude = intent.getDoubleExtra("longitude", 0.0D);
    if (this.latitude != 0.0D && this.longitude != 0.0D && this.action.equals("pickAddress")) {
      this.isModifyAddress = true;
      this.transformFromWGSToGCJ(this.latitude, this.longitude);
      if (!GPSUtil.isInArea(this.latitude, this.longitude) && this.mIsAmapDisplay && this.isSuportGooglePlay) {
        this.changeToGoogleMapView();
      }

      this.addPinView(this.latitude, this.longitude);
    } else {
      this.isModifyAddress = false;
      this.getCurrentLocation(showLocationAlert);
    }
  }

  public boolean checkPermisssion(final Context context) {
    // 记录当前的权限状态
    hasPermission = hasLocationPermission(context);
    if (hasPermission) {
      if (locationPermission != null) {
        locationPermission.dismiss();
      }
      return true;
    } else {
      ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, 100);
      return false;
    }
  }

  public boolean checkLocationEnabled(final Context context, boolean showLocationAlert) {
    int locationMode = 0;
    String locationProviders;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
      try {
        locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

      } catch (SettingNotFoundException e) {
        e.printStackTrace();
        return false;
      }

      if (locationMode != Settings.Secure.LOCATION_MODE_OFF) {
        return true;
      } else if (showLocationAlert) {
        if (gpsSwitchBuilder !=null) {
          gpsSwitchBuilder.dismiss();
          gpsSwitchBuilder = null;
        }
        gpsSwitchBuilder = new AlertDialog.Builder(context)
          .setMessage(this.gpsNetworkNotEnabledText)
          .setPositiveButton(this.openLocationSettingsText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
              context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
          })
          .setNegativeButton(this.cancelText, null)
          .show();
      }
      return false;
    }else{
      locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      return !TextUtils.isEmpty(locationProviders);
    }
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100) {
      if (grantResults.length > 0) {
        if (grantResults[0] == 0) {
          this.initParams(false);
        } else {
          if (locationPermission != null) {
            locationPermission.dismiss();
            locationPermission = null;
          }
          locationPermission = new AlertDialog.Builder(AMapActivity.this)
            .setMessage(this.permissionNotEnabledContent)
            .setPositiveButton(this.openLocationPermissionText, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                AMapActivity.this.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + AMapActivity.this.getPackageName())));
              }
            })
            .setNegativeButton(this.cancelText, null)
            .show();
        }
      } else {
        JSONObject json = new JSONObject();

        try {
          json.put("resultCode", 2);
        } catch (JSONException var6) {
          var6.printStackTrace();
        }

        this.result = json.toString();
      }
    }

  }

  private void initMap() throws Exception {
    Log.e("AmapActivity", "initMap");
  //  ServiceSettings.updatePrivacyShow(this, true, true);
  //  ServiceSettings.updatePrivacyAgree(this,true);
    AMapLocationClient.updatePrivacyAgree(this.getApplicationContext(), true);
    AMapLocationClient.updatePrivacyShow(this.getApplicationContext(), true, true);

    this.mIsAmapDisplay = true;
    this.geocodeSearch = new GeocodeSearch(this);

    this.geocodeSearch.setOnGeocodeSearchListener(this);
    this.aMapLocationClient = new AMapLocationClient(this);
    this.aMapLocationClient.setLocationListener(this);
    AMapLocationClientOption clientOption = new AMapLocationClientOption();
    clientOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
    clientOption.setOnceLocation(true);
    clientOption.setLocationCacheEnable(false);
    this.aMapLocationClient.setLocationOption(clientOption);
    this.aMap.getUiSettings().setZoomControlsEnabled(false);
    this.isModifyAddress = true;
    this.aMap.setOnMapLoadedListener(new OnMapLoadedListener() {
      public void onMapLoaded() {
        Log.e("AmapActivity", "onMapLoaded");
        AMapActivity.this.aMap.moveCamera(CameraUpdateFactory.zoomTo(18.0F));
        if (AMapActivity.this.isModifyAddress) {
          AMapActivity.this.aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(AMapActivity.this.latitude, AMapActivity.this.longitude)));
        }

      }
    });
    this.markerOptions = new MarkerOptions();
    this.aMap.setInfoWindowAdapter(this);
    this.aMap.setOnMapClickListener(new OnMapClickListener() {
      public void onMapClick(LatLng latLng) {
        Log.e("AmapActivity", "onMapClick");
        AMapActivity.this.addPinView(latLng.latitude, latLng.longitude);
      }
    });
    this.aMap.setOnCameraChangeListener(new OnCameraChangeListener() {
      public void onCameraChange(CameraPosition cameraPosition) {
      }

      public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.e("AmapActivity", "onCameraChangeFinish:" + cameraPosition.toString());
        AMapActivity.this.zoom = cameraPosition.zoom;
        if (!GPSUtil.isInArea(cameraPosition.target.latitude, cameraPosition.target.longitude) && AMapActivity.this.mIsAmapDisplay && AMapActivity.this.isSuportGooglePlay) {
          AMapActivity.this.changeToGoogleMapView();
        }

      }
    });
    this.aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
      @Override
      public void onMyLocationChange(Location location) {

      }
    });
//        this.aMapLocationClient.setLocationOption(new AMapLocationClientOption().setOnceLocation(true));
    this.aMapLocationClient.setLocationListener(new AMapLocationListener() {
      @Override
      public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
          double mLocationLatitude = aMapLocation.getLatitude();
          double mLocationLongitude = aMapLocation.getLongitude();
          if (mLocationLatitude > 0 && mLocationLongitude > 0) {
            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(mLocationLatitude, mLocationLongitude), 17);
            aMap.moveCamera(cu);
            addPinView(mLocationLatitude, mLocationLongitude);
          } else {
            Log.e("AmapActivity", "定位失败:"+aMapLocation.getErrorCode());
          }
        }
//                aMapLocationClient.stopLocation();
      }
    });
  }

  protected void onResume() {
    super.onResume();

    // 如果一开始是true，变成了false 需要关闭activity
    boolean currentPermission = hasLocationPermission(AMapActivity.this);
    if (currentPermission) {
      if (locationPermission != null) {
        locationPermission.dismiss();
        getCurrentLocation(true);
      }
    }

    if (this.mapView != null) {
      this.mapView.onResume();
    }

    if (this.mGoogleMapView != null) {
      try {
        this.mGoogleMapView.onResume();
      } catch (Exception var2) {
        ;
      }
    }

  }

  protected void onPause() {
    super.onPause();
    if (this.mapView != null) {
      this.mapView.onPause();
    }

    if (this.mGoogleMapView != null) {
      try {
        this.mGoogleMapView.onPause();
      } catch (Exception var2) {
        ;
      }
    }

  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    this.mapView.onSaveInstanceState(outState);
  }

  private boolean hasLocationPermission(final Context context) {
    boolean value = PermissionChecker.checkPermission(AMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Process.myPid(), Process.myUid(), getPackageName()) == PermissionChecker.PERMISSION_GRANTED;
    return value;
  }

  private void getCurrentLocationWithNotSetAddress() {
    if (this.latitude != 0.0D && this.longitude != 0.0D && this.action.equals("pickAddress")) {
      // 设置了家庭地址，不需要定位
    } else {
      this.getCurrentLocation(true);
    }
  }

  @Override
  public void gpsSwitchState(boolean gpsOpen) {
    if ( gpsOpen ){
      if (gpsSwitchBuilder != null) {
        gpsSwitchBuilder.dismiss();
        getCurrentLocation(true);
      } else {
        getCurrentLocationWithNotSetAddress();
      }
    }
  }

  protected void onDestroy() {
    super.onDestroy();
    if (this.mapView != null) {
      this.mapView.onDestroy();
    }
    if (this.aMapLocationClient != null) {
      this.aMapLocationClient.onDestroy();
    }
    if ( gps_presenter != null ){
      gps_presenter.onDestroy();
    }

    if (this.mGoogleMapView != null) {
      try {
        this.mGoogleMapView.onDestroy();
      } catch (Exception var2) {
        ;
      }
    }

  }

  public void onBackPressed() {
    JSONObject json = new JSONObject();

    try {
      json.put("resultCode", 1);
    } catch (JSONException var6) {
      var6.printStackTrace();
    }
    this.result = json.toString();
    Intent intent = new Intent();
    intent.putExtra("result", this.result);
    this.setResult(6, intent);
    this.finish();
  }

  private void initEvent() {
    this.ll_location.setOnClickListener(this);
    this.iv_left.setOnClickListener(this);
    this.tv_right.setOnClickListener(this);
    this.findViewById(id.ll_search).setOnClickListener(this);
  }

  private void getCurrentLocation(boolean showLocationAlert) {
    System.out.println("Amap Test this.mIsAmapDisplay" + this.mIsAmapDisplay);

    if (this.mIsAmapDisplay) {
      if (this.aMap.getMyLocation() != null) {
        double mLocationLatitude = this.aMap.getMyLocation().getLatitude();
        double mLocationLongitude = this.aMap.getMyLocation().getLongitude();
        Log.e("AmapActivity", "getCurrentLocation Amap." + " " + mLocationLatitude + " " + mLocationLongitude);
        if (mLocationLatitude != 0 && mLocationLatitude != 0) {
          CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(mLocationLatitude, mLocationLongitude), 17);
          aMap.moveCamera(cu);
          addPinView(mLocationLatitude, mLocationLongitude);
          return;
        }
      }
      System.out.println("Amap Test getMyLocation is None");
      if (this.checkLocationEnabled(this, showLocationAlert)) {
        if (this.checkPermisssion(this)) {
          this.aMapLocationClient.startLocation();
        }
      }
    } else {
      Log.e("AmapActivity", "getCurrentLocation google.");

      try {
        Task locationResult = this.fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener() {
          public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
              AMapActivity.this.mLastKnownLocation = (Location) task.getResult();
              AMapActivity.this.googlemap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(AMapActivity.this.mLastKnownLocation.getLatitude(), AMapActivity.this.mLastKnownLocation.getLongitude())));
              AMapActivity.this.addPinView(AMapActivity.this.mLastKnownLocation.getLatitude(), AMapActivity.this.mLastKnownLocation.getLongitude());
            } else {
              Log.e("AmapActivity", "Current location is null. Using defaults.");
              Log.e("AmapActivity", "Exception: %s", task.getException());
            }

          }
        });
      } catch (SecurityException var2) {
        Log.e("Exception: %s", var2.getMessage());
      }
    }

  }

  private void addPinView(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    if (this.mIsAmapDisplay) {
      this.aMap.clear();
      this.markerOptions.position(new LatLng(latitude, longitude));
      this.markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), mipmap.home_icon_)));
      this.markerOptions.anchor(0.5F, 0.5F);
      this.markerOptions.title("  ");
      Marker marker = this.aMap.addMarker(this.markerOptions);
      marker.showInfoWindow();
      LatLonPoint latLngPoint = new LatLonPoint(latitude, longitude);
      RegeocodeQuery query = new RegeocodeQuery(latLngPoint, 200.0F, "autonavi");
      this.geocodeSearch.getFromLocationAsyn(query);
    } else {
      Geocoder geocoder = new Geocoder(this, Locale.getDefault());

      try {
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        this.googlemap.clear();
        com.google.android.gms.maps.model.Marker marker = this.googlemap.addMarker((new com.google.android.gms.maps.model.MarkerOptions()).position(new com.google.android.gms.maps.model.LatLng(latitude, longitude)).icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(), mipmap.home_icon_))).anchor(0.5F, 0.5F).title(((Address) addresses.get(0)).getAddressLine(0)));
        marker.showInfoWindow();
      } catch (Exception var8) {
        var8.printStackTrace();
      }
    }

  }

  public void onPointerCaptureChanged(boolean hasCapture) {
  }

  private void confirm() {
    Log.e("AmapActivity", "result:" + this.result);
    Intent intent = new Intent();
    intent.putExtra("result", this.result);
    this.setResult(-1, intent);
    this.finish();
  }

  private void transformFromGCJToWGS(double lat, double lon) {
    double[] latlon = GPSUtil.gcj02_To_Gps84(lat, lon);
    JSONObject json = new JSONObject();

    try {
      json.put("latitude", latlon[0]);
      json.put("longitude", latlon[1]);
    } catch (JSONException var8) {
      var8.printStackTrace();
    }

    this.result = json.toString();
  }

  private void transformFromWGSToGCJ(double lat, double lon) {
    LatLng latLng = new LatLng(lat, lon);
    CoordinateConverter converter = new CoordinateConverter(this.getApplicationContext());
    converter.from(CoordType.GPS);
    converter.coord(latLng);
    LatLng desLatLng = converter.convert();
    this.latitude = desLatLng.latitude;
    this.longitude = desLatLng.longitude;
  }

  private void changeToGoogleMapView() {
    if (this.checkGooglePlayServices()) {
      this.zoom = this.mapView.getMap().getCameraPosition().zoom;
      this.latitude = this.mapView.getMap().getCameraPosition().target.latitude;
      this.longitude = this.mapView.getMap().getCameraPosition().target.longitude;
      this.mIsAmapDisplay = false;
      this.mGoogleMapView = new com.google.android.gms.maps.MapView(this, (new GoogleMapOptions()).camera(new com.google.android.gms.maps.model.CameraPosition(new com.google.android.gms.maps.model.LatLng(this.latitude, this.longitude), this.zoom, 0.0F, 0.0F)));
      this.mGoogleMapView.setLayoutParams(new LayoutParams(-1, -1));
      this.fusedLocationProviderClient = new FusedLocationProviderClient(this);
      this.mGoogleMapView.onCreate((Bundle) null);
      this.mGoogleMapView.onResume();
      this.rl_map.addView(this.mGoogleMapView);
      this.mGoogleMapView.getMapAsync(this);
      this.handler.sendEmptyMessageDelayed(0, 500L);
    }
  }

  private boolean checkGooglePlayServices() {
    int result = MapsInitializer.initialize(this);
    switch (result) {
      case 0:
        this.isSuportGooglePlay = true;
        return true;
      case 1:
        this.isSuportGooglePlay = false;
        Log.e("AmapActivity", "SERVICE_MISSING");
        break;
      case 2:
        this.isSuportGooglePlay = true;
        Toast.makeText(this.getApplicationContext(), "SERVICE_VERSION_UPDATE_REQUIRED", 0).show();
        GooglePlayServicesUtil.getErrorDialog(2, this, Toast.LENGTH_SHORT).show();
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      default:
        break;
      case 9:
        this.isSuportGooglePlay = false;
        Log.e("AmapActivity", "SERVICE_INVALID");
    }

    return false;
  }

  private void changeToAmapView() throws Exception {
    if (this.googlemap != null) {
      this.zoom = this.googlemap.getCameraPosition().zoom;
      this.latitude = this.googlemap.getCameraPosition().target.latitude;
      this.longitude = this.googlemap.getCameraPosition().target.longitude;
    }

    this.mapView = new MapView(this);
    this.mapView.setLayoutParams(new LayoutParams(-1, -1));
    this.mapView = new MapView(this, (new AMapOptions()).camera(new CameraPosition(new LatLng(this.latitude, this.longitude), this.zoom, 0.0F, 0.0F)));
    this.mapView.onCreate((Bundle) null);
    this.mapView.onResume();
    this.rl_map.addView(this.mapView);
    this.mGoogleMapView.animate().alpha(0.0F).setDuration(500L).setListener(new AnimatorListenerAdapter() {
      public void onAnimationEnd(Animator animation) {
        AMapActivity.this.mGoogleMapView.setVisibility(View.GONE);
        AMapActivity.this.rl_map.removeView(AMapActivity.this.mGoogleMapView);
        if (AMapActivity.this.mGoogleMapView != null) {
          AMapActivity.this.mGoogleMapView.onDestroy();
        }

      }
    });
    this.aMap = this.mapView.getMap();
    this.initMap();
    this.mIsAmapDisplay = true;
  }

  public void onCameraMove() {
  }

  @SuppressLint("MissingPermission")
  public void onMapReady(GoogleMap googleMap) {
    this.googlemap = googleMap;
    if (this.googlemap != null) {
      this.googlemap.setOnCameraMoveListener(this);
      this.googlemap.setOnCameraIdleListener(this);
      this.googlemap.setMyLocationEnabled(false);
      this.googlemap.setInfoWindowAdapter(this);
      this.googlemap.getUiSettings().setMyLocationButtonEnabled(false);
      this.googlemap.setOnMapClickListener(new com.google.android.gms.maps.GoogleMap.OnMapClickListener() {
        public void onMapClick(com.google.android.gms.maps.model.LatLng latLng) {
          AMapActivity.this.addPinView(latLng.latitude, latLng.longitude);
        }
      });
    }

  }

  public void onCameraIdle() {
    com.google.android.gms.maps.model.CameraPosition cameraPosition = this.googlemap.getCameraPosition();
    this.longitude = cameraPosition.target.longitude;
    this.latitude = cameraPosition.target.latitude;
    this.zoom = cameraPosition.zoom;
    if (GPSUtil.isInArea(this.latitude, this.longitude) && !this.mIsAmapDisplay) {
      try {
        this.changeToAmapView();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  public void onLocationChanged(AMapLocation aMapLocation) {
    if (aMapLocation.getErrorCode() == 0) {
      try {
        this.checkLocation(aMapLocation);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      Intent intent;
      if (aMapLocation.getErrorCode() == 7) {
        intent = new Intent();
        intent.putExtra("result", "8");
        this.setResult(5, intent);
        this.finish();
      } else if (aMapLocation.getErrorCode() == 12) {
        intent = new Intent();
        intent.putExtra("result", "5");
        this.setResult(5, intent);
        this.finish();
      }
    }

  }

  private void checkLocation(AMapLocation location) throws Exception {
    if (this.isModifyAddress) {
      this.isModifyAddress = false;
    } else {
      if (GPSUtil.isInArea(location.getLatitude(), location.getLongitude())) {
        if (!this.mIsAmapDisplay) {
          this.changeToAmapView();
        }
      } else if (this.mIsAmapDisplay && this.isSuportGooglePlay) {
        this.changeToGoogleMapView();
      }

      if (this.mIsAmapDisplay) {
        this.aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        this.addPinView(location.getLatitude(), location.getLongitude());
      } else {
        this.googlemap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude())));
      }
    }

  }

  public void onClick(View v) {
    int i = v.getId();
    if (i == id.ll_location) {
      this.getCurrentLocation(true);
    } else if (i == id.iv_left) {
      this.onBackPressed();
    } else if (i == id.tv_right) {
      if (this.action != null && this.action.equals("pickAddress")) {
        this.transformFromGCJToWGS(this.latitude, this.longitude);
      }

      this.confirm();
    } else if (i == id.ll_search) {
      Intent intent = new Intent(this, SearchAddressActivity.class);
      intent.putExtras(this.getIntent().getExtras());
      intent.putExtra("isAmap", this.mIsAmapDisplay);
      this.startActivityForResult(intent, 10000);
    }

  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == -1 && requestCode == 10000) {
      if (data != null) {
        AddressItem addressItem = (AddressItem) data.getSerializableExtra("addressItem");
        if (addressItem != null) {
          this.tv_search.setText(addressItem.getName());
          if (this.mIsAmapDisplay) {
            this.aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(addressItem.getLatitude(), addressItem.getLongitude())));
          } else {
            this.googlemap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(new com.google.android.gms.maps.model.LatLng(addressItem.getLatitude(), addressItem.getLongitude())));
          }

          this.addPinView(addressItem.getLatitude(), addressItem.getLongitude());
        }
      } else {
        if (this.action != null && this.action.equals("pickAddress")) {
          this.transformFromGCJToWGS(this.latitude, this.longitude);
        }

        this.confirm();
      }
    }

  }

  public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
    Log.e("AmapActivity", "onRegeocodeSearched:" + rCode);
    if (rCode == 1000) {
      RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
      if (this.tv_address != null) {
        this.tv_address.setText(regeocodeAddress.getFormatAddress());
      }

      if (this.markerOptions != null) {
        this.markerOptions.title(regeocodeAddress.getFormatAddress());
      }
    }

  }

  public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
  }

  public View getInfoWindow(Marker marker) {
    if (infoWindow == null) {
      infoWindow = this.getLayoutInflater().inflate(layout.customer_info_window, (ViewGroup) null);
    }
    LayoutParams params = new LayoutParams(-2, UIUtils.dip2px(this, 45.0F));
    infoWindow.setLayoutParams(params);
    this.render(marker, infoWindow);
    return infoWindow;
  }

  public View getInfoContents(Marker marker) {
    return null;
  }

  public void render(Marker marker, View view) {
    Log.e("AmapActivity", "render " + marker.getTitle());
    this.tv_address = (TextView) view.findViewById(id.tv_pop);
//    String title = marker.getTitle();
//    if (title != null||!title.equals("")) {
//      this.tv_address.setText(title);
//    }

  }

  public void render(com.google.android.gms.maps.model.Marker marker, View view) {
    this.tv_address = (TextView) view.findViewById(id.tv_pop);
    String title = marker.getTitle();
    if (title != null) {
      this.tv_address.setText(title);
    }

  }

  public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
    if (infoWindow == null) {
      infoWindow = this.getLayoutInflater().inflate(layout.customer_info_window, (ViewGroup) null);
    }
    LayoutParams params = new LayoutParams(-2, UIUtils.dip2px(this, 45.0F));
    infoWindow.setLayoutParams(params);
    this.render(marker, infoWindow);
    return infoWindow;
  }

  public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
    return null;
  }
}
