//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.gizwits.amap.bean.AddressItem;
import com.gizwitsgeo.R;

import java.util.List;

public class SearchAddressAdapter extends EasyAdapter<AddressItem> {
  public SearchAddressAdapter(Context context) {
    super(context);
  }

  public SearchAddressAdapter(Context context, List<AddressItem> items) {
    super(context, items);
  }

  protected EasyAdapter<AddressItem>.ViewHolder newHolder() {
    return new mViewHolder();
  }

  private class mViewHolder extends EasyAdapter<AddressItem>.ViewHolder {
    private TextView tv_name;
    private TextView tv_address;
    AddressItem addressItem;

    private mViewHolder() {
      super(SearchAddressAdapter.this);
    }

    protected View init(LayoutInflater layoutInflater) {
      View v = layoutInflater.inflate(R.layout.layout_search_adress_item, (ViewGroup)null);
      this.tv_name = (TextView)v.findViewById(R.id.tv_name);
      this.tv_address = (TextView)v.findViewById(R.id.tv_address);
      return v;
    }

    protected void update() {
      this.addressItem = (AddressItem)SearchAddressAdapter.this.get(this.position);
      if (this.addressItem != null) {
        this.tv_name.setText(this.addressItem.getName());
        this.tv_address.setText(this.addressItem.getAddress());
      }
    }
  }
}
