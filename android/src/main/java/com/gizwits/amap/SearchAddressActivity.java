//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.reactlibrary.R.id;
import com.reactlibrary.R.layout;
import com.gizwits.amap.adapter.SearchAddressAdapter;
import com.gizwits.amap.bean.AddressItem;
import com.gizwits.amap.dao.AddressDao;
import com.gizwits.amap.utils.ColorUtil;
import com.google.android.gms.location.places.GeoDataClient;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchAddressActivity extends Activity implements OnPoiSearchListener, OnClickListener {
  private static final String TAG = "SearchAddressActivity";
  private static final int PAGE_SIZE = 20;
  private ImageView iv_left;
  private TextView tv_title;
  private TextView tv_right;
  private EditText et_search;
  private ImageView iv_search;
  private RefreshListView address_list;
  private TextView tv_none_search;
  private RelativeLayout rl_top;
  private SearchAddressAdapter adapter;
  private boolean mIsAmapDisplay;
  GeoDataClient geoDataClient;

  public SearchAddressActivity() {
  }

  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(layout.activity_search_address);
    this.mIsAmapDisplay = this.getIntent().getBooleanExtra("isAmap", true);
    this.initView();
    this.initEvent();
    this.initParams();
    this.loadAddressSearchRecodes();
  }

  private void initView() {
    this.rl_top = (RelativeLayout)this.findViewById(id.rl_top);
    this.iv_left = (ImageView)this.findViewById(id.iv_left);
    this.tv_title = (TextView)this.findViewById(id.tv_title);
    this.tv_right = (TextView)this.findViewById(id.tv_right);
    this.et_search = (EditText)this.findViewById(id.et_search);
    this.iv_search = (ImageView)this.findViewById(id.iv_search);
    this.address_list = (RefreshListView)this.findViewById(id.address_list);
    this.tv_none_search = (TextView)this.findViewById(id.tv_none_search);
    this.adapter = new SearchAddressAdapter(this, new ArrayList());
    this.address_list.setAdapter(this.adapter);
    this.address_list.setRefreshable(false);
  }

  private void initEvent() {
    this.tv_right.setOnClickListener(this);
    this.iv_left.setOnClickListener(this);
    this.iv_search.setOnClickListener(this);
    this.address_list.setOnBottomListener(new OnClickListener() {
      public void onClick(View v) {
        SearchAddressActivity.this.poiSearch(SearchAddressActivity.this.adapter.getCount() / 20 + 1, SearchAddressActivity.this.et_search.getText().toString());
      }
    });
    this.et_search.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(SearchAddressActivity.this.et_search.getText())) {
          SearchAddressActivity.this.adapter.clear();
          SearchAddressActivity.this.loadAddressSearchRecodes();
        } else {
          SearchAddressActivity.this.adapter.clear();
          SearchAddressActivity.this.address_list.showNoMore();
          SearchAddressActivity.this.poiSearch(1, SearchAddressActivity.this.et_search.getText().toString());
        }

      }

      public void afterTextChanged(Editable s) {
      }
    });
    this.address_list.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AddressDao addressDao = new AddressDao(SearchAddressActivity.this);
        addressDao.openDb();
        addressDao.insertOrReplaceAddressItem((AddressItem)SearchAddressActivity.this.adapter.get(position));
        addressDao.closeDb();
        Intent intent = new Intent();
        intent.putExtra("addressItem", (Serializable)SearchAddressActivity.this.adapter.get(position));
        SearchAddressActivity.this.setResult(-1, intent);
        SearchAddressActivity.this.finish();
      }
    });
  }

  private void initParams() {
    Intent intent = this.getIntent();
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
    if (themeInfoStr != null && !"".equals(themeInfoStr)) {
      try {
        JSONObject jb = new JSONObject(themeInfoStr);
        if (jb.has("themeColor")) {
          String themeColor = jb.getString("themeColor");
          ColorUtil.changeDrawableColor(themeColor, this.iv_left);
          ColorUtil.changeDrawableColor(themeColor, this.iv_search);
          this.tv_right.setTextColor(ColorUtil.parseStrColor(themeColor));
        }

        if (jb.has("navi_bg")) {
          this.rl_top.setBackgroundColor(ColorUtil.parseStrColor(jb.getString("navi_bg")));
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
      } catch (JSONException var8) {
        var8.printStackTrace();
      }
    }

  }

  private void loadAddressSearchRecodes() {
    AddressDao addressDao = new AddressDao(this);
    addressDao.openDb();
    List<AddressItem> addressItemList = addressDao.getAllAddressItems();
    this.adapter.add(addressItemList);
    this.address_list.showNoMore();
    addressDao.closeDb();
    if (this.adapter.getCount() < 1) {
      this.address_list.setVisibility(View.GONE);
      this.tv_none_search.setVisibility(View.VISIBLE);
    } else {
      this.address_list.setVisibility(View.VISIBLE);
      this.tv_none_search.setVisibility(View.GONE);
    }

  }

  private void poiSearch(int page, String str) {
    if (this.mIsAmapDisplay) {
      Query mPoiSearchQuery = new Query(str, "", "");
      mPoiSearchQuery.setPageSize(20);
      mPoiSearchQuery.setPageNum(page);
      PoiSearch poiSearch = new PoiSearch(this, mPoiSearchQuery);
      poiSearch.setOnPoiSearchListener(this);
      poiSearch.searchPOIAsyn();
    } else {
      Geocoder geocoder = new Geocoder(this, Locale.CHINA);

      try {
        List<AddressItem> addressItems = new ArrayList();
        List<Address> addresses = geocoder.getFromLocationName(str, 200);
        Log.e("SearchAddressActivity", "addresses.size:" + addresses.size());
        Iterator var6 = addresses.iterator();

        while(var6.hasNext()) {
          Address address = (Address)var6.next();
          Log.e("SearchAddressActivity", "address:" + address.toString());
          AddressItem addressItem = new AddressItem();
          addressItem.setCity(address.getLocality());
          addressItem.setName(address.getFeatureName());
          addressItem.setLatitude(address.getLatitude());
          addressItem.setLongitude(address.getLongitude());
          addressItem.setAddress(address.getAddressLine(0));
          addressItems.add(addressItem);
        }

        this.adapter.add(addressItems);
        if (this.adapter.getCount() < 1) {
          this.address_list.setVisibility(View.GONE);
          this.tv_none_search.setVisibility(View.VISIBLE);
        } else {
          this.address_list.setVisibility(View.VISIBLE);
          this.tv_none_search.setVisibility(View.GONE);
          this.address_list.showNoMore();
        }
      } catch (IOException var9) {
        var9.printStackTrace();
      }
    }

  }

  public void onPoiSearched(PoiResult result, int rCode) {
    if (rCode == 1000 && result != null) {
      List<PoiItem> poiItems = result.getPois();
      List<AddressItem> addressItems = new ArrayList();
      Iterator var5 = poiItems.iterator();

      while(var5.hasNext()) {
        PoiItem poiItem = (PoiItem)var5.next();
        AddressItem addressItem = new AddressItem();
        addressItem.setCity(poiItem.getCityName());
        addressItem.setName(poiItem.getTitle());
        addressItem.setLatitude(poiItem.getLatLonPoint().getLatitude());
        addressItem.setLongitude(poiItem.getLatLonPoint().getLongitude());
        addressItem.setAddress(poiItem.getSnippet());
        addressItems.add(addressItem);
      }

      this.adapter.add(addressItems);
      if (this.adapter.getCount() < 1) {
        this.address_list.setVisibility(View.GONE);
        this.tv_none_search.setVisibility(View.VISIBLE);
      } else {
        this.address_list.setVisibility(View.VISIBLE);
        this.tv_none_search.setVisibility(View.GONE);
        if (addressItems.size() < 20) {
          this.address_list.showNoMore();
        } else {
          this.address_list.prepareLoad();
        }
      }
    }

  }

  public void onPoiItemSearched(PoiItem poiItem, int i) {
  }

  public void onClick(View v) {
    if (v.getId() == id.iv_left) {
      this.onBackPressed();
    } else if (v.getId() == id.iv_search) {
      this.adapter.clear();
      this.poiSearch(1, this.et_search.getText().toString());
    } else if (v.getId() == id.tv_right) {
      this.setResult(-1);
      this.finish();
    }

  }
}
