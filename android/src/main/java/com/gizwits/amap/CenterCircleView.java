//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import android.util.AttributeSet;
import android.view.View;
import com.gizwits.amap.utils.ColorUtil;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class CenterCircleView extends View {
  private Paint paint;
  private Paint whitePaint;
  private int cX;
  private int cY;
  private Context mContext;
  private int width;
  private int height;

  public CenterCircleView(Context context) {
    super(context);
  }

  public CenterCircleView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CenterCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @RequiresApi(
    api = 21
  )
  public CenterCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    }

    if (this.whitePaint == null) {
      this.whitePaint = new Paint();
      this.whitePaint.setColor(ColorUtil.parseStrColor("#ffffff"));
    }

    this.cX = this.width - 2 >> 1;
    this.cY = this.height / 2 + (this.height - this.width) / 2;
  }

  public void setColor(int color) {
    if (this.paint == null) {
      this.paint = new Paint();
    }

    this.paint.setColor(color);
    this.postInvalidate();
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    this.paint.setStrokeWidth(3.0F);
    this.paint.setAntiAlias(true);
    this.paint.setStyle(Style.FILL_AND_STROKE);
    canvas.drawCircle((float)this.cX, (float)this.cY, (float)(this.width / 2 - 5), this.whitePaint);
    canvas.drawCircle((float)this.cX, (float)this.cY, (float)(this.width / 4), this.paint);
  }
}
