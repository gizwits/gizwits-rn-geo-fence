//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

public class BackView extends View {
  private Paint paint;
  private int width;
  private int height;

  public BackView(Context context) {
    super(context);
  }

  public BackView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @RequiresApi(
    api = 21
  )
  public BackView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    this.width = right - left;
    this.height = bottom - top;
    this.init();
  }

  private void init() {
    if (this.paint == null) {
      this.paint = new Paint();
      this.paint.setAntiAlias(true);
      this.paint.setStrokeWidth(5.0F);
    }

  }

  public void setColor(int color) {
    if (this.paint == null) {
      this.paint = new Paint();
      this.paint.setAntiAlias(true);
      this.paint.setStrokeWidth(5.0F);
    }

    this.paint.setColor(color);
    this.postInvalidate();
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawLine((float)(this.width / 2), (float)(this.height / 3), (float)(this.width / 4), (float)(this.height / 2), this.paint);
    canvas.drawLine((float)(this.width / 4), (float)(this.height / 2), (float)(this.width / 2), (float)(this.height - this.height / 3), this.paint);
  }
}
