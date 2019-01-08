//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

public class ColorUtil {
  public ColorUtil() {
  }

  public static void changeDrawableColor(String strColor, ImageView imageView) {
    int color = parseStrColor(strColor);
    Drawable drawable = imageView.getDrawable();
    if (drawable != null) {
      imageView.setImageDrawable(tintDrawable(drawable, ColorStateList.valueOf(color)));
    }
  }

  public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
    Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
    DrawableCompat.setTintList(wrappedDrawable, colors);
    return wrappedDrawable;
  }

  public static int parseStrColor(String color) {
    if (color != null && !"".equals(color)) {
      try {
        return color.contains("#") ? Color.parseColor(color) : Color.parseColor("#" + color);
      } catch (Exception var2) {
        return Color.parseColor("#4e8dec");
      }
    } else {
      return Color.parseColor("#4e8dec");
    }
  }
}
