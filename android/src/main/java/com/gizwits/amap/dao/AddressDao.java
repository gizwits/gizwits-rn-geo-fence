//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import com.gizwits.amap.bean.AddressItem;
import java.util.ArrayList;
import java.util.List;

public class AddressDao extends SQLiteOpenHelper {
  private static final String DB_NAME = "gizwits_map";
  private static final int VERSION = 1;
  private static SQLiteDatabase db;

  public AddressDao(Context context, String name, CursorFactory factory, int version) {
    super(context, name, factory, version);
  }

  public AddressDao(Context context) {
    super(context, "gizwits_map", (CursorFactory)null, 1);
  }

  public void openDb() {
    db = this.getWritableDatabase();
  }

  public void closeDb() {
    if (db != null) {
      db.close();
    }

  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table if not exists addressItemTable (name text primary key, address text, latitude text, longitude text)");
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists addressItemTable");
    this.onCreate(db);
  }

  public void insertOrReplaceAddressItem(AddressItem addressItem) {
    ContentValues values = new ContentValues();
    values.put("name", addressItem.getName());
    values.put("address", addressItem.getAddress());
    values.put("latitude", String.valueOf(addressItem.getLatitude()));
    values.put("longitude", String.valueOf(addressItem.getLongitude()));
    db.replace("addressItemTable", (String)null, values);
  }

  public List<AddressItem> getAllAddressItems() {
    List<AddressItem> addressItemList = new ArrayList();
    Cursor cursor = this.rawQuery();
    if (cursor != null && cursor.getCount() > 0) {
      while(cursor.moveToNext()) {
        AddressItem addressItem = new AddressItem();
        addressItem.setName(cursor.getString(cursor.getColumnIndex("name")));
        addressItem.setAddress(cursor.getString(cursor.getColumnIndex("address")));
        addressItem.setLatitude(Double.valueOf(cursor.getString(cursor.getColumnIndex("latitude"))));
        addressItem.setLongitude(Double.valueOf(cursor.getString(cursor.getColumnIndex("longitude"))));
        addressItemList.add(addressItem);
      }

      cursor.close();
    }

    return addressItemList;
  }

  private Cursor rawQuery() {
    Cursor c = db.rawQuery("select * from addressItemTable", (String[])null);
    return c;
  }
}
