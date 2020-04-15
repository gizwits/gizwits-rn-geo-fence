//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RefreshListView extends DropDownListView {
  private boolean isRefreshing;
  private boolean isLoading;
  private OnDropDownListener oddl;
  private OnClickListener obl;
  private boolean canlayoutChildren = true;

  public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this.init();
  }

  public RefreshListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.init();
  }

  public RefreshListView(Context context) {
    super(context);
    this.init();
  }

  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return super.onInterceptTouchEvent(ev);
  }

  private void init() {
    this.setDropDownStyle(true);
    this.setOnBottomStyle(true);
    this.setOnBottomStyle(false);
    this.setOnDropDownListener(new OnDropDownListener() {
      public void onDropDown() {
        RefreshListView.this.isRefreshing = true;
        if (RefreshListView.this.oddl != null) {
          RefreshListView.this.oddl.onDropDown();
        }

      }
    });
    this.setOnBottomListener(new OnClickListener() {
      public void onClick(View arg0) {
        RefreshListView.this.isLoading = true;
        if (RefreshListView.this.obl != null) {
          RefreshListView.this.obl.onClick(arg0);
        }

      }
    });
  }

  public void showRefreshing(boolean scrollTop) {
    this.isRefreshing = true;
    this.setHeaderStatusLoading();
    if (scrollTop) {
      this.smoothScrollToPosition(0);
    }

  }

  public void showRefreshFail() {
    this.isRefreshing = false;
    this.setHeaderStatusLoading();
    this.setHeaderStatusClickToLoad();
  }

  public void prepareLoad() {
    this.isLoading = false;
    this.onBottomComplete();
    this.setOnBottomStyle(true);
    this.setAutoLoadOnBottom(true);
    this.setHasMore(true);
    this.onBottomBegin();
  }

  public void showLoading() {
    this.isLoading = true;
    this.onBottomComplete();
    this.setOnBottomStyle(true);
    this.setAutoLoadOnBottom(false);
    this.setHasMore(true);
    this.onBottomBegin();
  }

  public void showNoMore() {
    this.isLoading = false;
    this.setOnBottomStyle(true);
    this.setAutoLoadOnBottom(false);
    this.setShowFooterWhenNoMore(true);
    this.setHasMore(false);
    this.onBottomComplete();
  }

  public void showLoadFail() {
    this.isLoading = false;
    this.setOnBottomStyle(true);
    this.setAutoLoadOnBottom(false);
    this.setHasMore(true);
    this.onBottomComplete();
  }

  public void completeRefresh() {
    this.isRefreshing = false;
    this.onDropDownComplete();
  }

  public void completeLoad() {
    this.isLoading = false;
    this.onBottomComplete();
  }

  public void setLoadable(boolean loadable) {
    this.setOnBottomStyle(loadable);
  }

  public void setRefreshable(boolean refreshable) {
    this.setDropDownStyle(refreshable);
  }

  public void setOnRefreshListener(OnDropDownListener l) {
    this.oddl = l;
  }

  public void setOnLoadListener(OnClickListener l) {
    this.obl = l;
  }

  public boolean isRefreshing() {
    return this.isRefreshing;
  }

  public void setRefreshing(boolean isRefreshing) {
    this.isRefreshing = isRefreshing;
  }

  public boolean isLoading() {
    return this.isLoading;
  }

  public void setLoading(boolean isLoading) {
    this.isLoading = isLoading;
  }

  protected void layoutChildren() {
    if (this.canlayoutChildren) {
      super.layoutChildren();
    }

  }

  public void setCanLayoutChildren(boolean b) {
    this.canlayoutChildren = b;
  }

  public boolean isCanLayoutChildren() {
    return this.canlayoutChildren;
  }
}
