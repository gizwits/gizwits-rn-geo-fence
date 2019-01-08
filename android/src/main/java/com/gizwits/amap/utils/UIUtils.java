//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap.utils;

import android.content.Context;

public class UIUtils {
  public UIUtils() {
  }

  public static int dip2px(Context context, float dpValue) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int)(dpValue * scale + 0.5F);
  }

  public static int px2dip(Context context, float pxValue) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int)(pxValue / scale + 0.5F);
  }
}
