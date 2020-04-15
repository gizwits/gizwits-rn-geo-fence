//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gizwits.amap;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.gizwitsgeo.R;


public class DropDownListView extends ListView implements OnScrollListener {
  protected boolean isDropDownStyle = true;
  protected boolean isOnBottomStyle = true;
  protected boolean isAutoLoadOnBottom = false;
  private String headerDefaultText;
  private String headerPullText;
  private String headerReleaseText;
  private String headerLoadingText;
  private String footerDefaultText;
  private String footerLoadingText;
  private String footerNoMoreText;
  private Context context;
  private RelativeLayout headerLayout;
  private ImageView headerImage;
  private ProgressBar headerProgressBar;
  private TextView headerText;
  private TextView headerSecondText;
  private RelativeLayout footerLayout;
  private ProgressBar footerProgressBar;
  private Button footerButton;
  private TextView footerTextView;
  private OnDropDownListener onDropDownListener;
  protected OnScrollListener onScrollListener;
  private OnTouchListener onTouchListener;
  private float headerPaddingTopRate = 1.5F;
  private int headerReleaseMinDistance;
  protected boolean hasMore = true;
  private boolean isShowFooterProgressBar = true;
  private boolean isShowFooterWhenNoMore = false;
  protected int currentScrollState;
  protected int currentHeaderStatus;
  protected boolean hasReachedTop = false;
  private RotateAnimation flipAnimation;
  private RotateAnimation reverseFlipAnimation;
  private int headerOriginalHeight;
  private int headerOriginalTopPadding;
  private float actionDownPointY = -1.0F;
  private boolean isOnBottomLoading = false;
  private OnClickListener onBottomListener;
  public static final int HEADER_STATUS_CLICK_TO_LOAD = 1;
  public static final int HEADER_STATUS_DROP_DOWN_TO_LOAD = 2;
  public static final int HEADER_STATUS_RELEASE_TO_LOAD = 3;
  public static final int HEADER_STATUS_LOADING = 4;

  public DropDownListView(Context context) {
    super(context);
    this.init(context);
  }

  public DropDownListView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.getAttrs(context, attrs);
    this.init(context);
  }

  public DropDownListView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this.getAttrs(context, attrs);
    this.init(context);
  }

  private void init(Context context) {
    this.context = context;
    this.initDropDownStyle();
    this.initOnBottomStyle();
    super.setOnScrollListener(this);
  }

  private void initDropDownStyle() {
    if (this.headerLayout != null) {
      if (this.isDropDownStyle) {
        this.addHeaderView(this.headerLayout);
      } else {
        this.removeHeaderView(this.headerLayout);
      }

    } else if (this.isDropDownStyle) {
      this.headerReleaseMinDistance = this.context.getResources().getDimensionPixelSize(R.dimen.drop_down_list_header_release_min_distance);
      this.flipAnimation = new RotateAnimation(0.0F, 180.0F, 1, 0.5F, 1, 0.5F);
      this.flipAnimation.setInterpolator(new LinearInterpolator());
      this.flipAnimation.setDuration(250L);
      this.flipAnimation.setFillAfter(true);
      this.reverseFlipAnimation = new RotateAnimation(-180.0F, 0.0F, 1, 0.5F, 1, 0.5F);
      this.reverseFlipAnimation.setInterpolator(new LinearInterpolator());
      this.reverseFlipAnimation.setDuration(250L);
      this.reverseFlipAnimation.setFillAfter(true);
      this.headerDefaultText = this.context.getString(R.string.drop_down_list_header_default_text);
      this.headerPullText = this.context.getString(R.string.drop_down_list_header_pull_text);
      this.headerReleaseText = this.context.getString(R.string.drop_down_list_header_release_text);
      this.headerLoadingText = this.context.getString(R.string.drop_down_list_header_loading_text);
      LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.headerLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_list_header, this, false);
      this.headerText = (TextView)this.headerLayout.findViewById(R.id.drop_down_list_header_default_text);
      this.headerImage = (ImageView)this.headerLayout.findViewById(R.id.drop_down_list_header_image);
      this.headerProgressBar = (ProgressBar)this.headerLayout.findViewById(R.id.drop_down_list_header_progress_bar);
      this.headerSecondText = (TextView)this.headerLayout.findViewById(R.id.drop_down_list_header_second_text);
      this.headerLayout.setClickable(true);
      this.headerLayout.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          DropDownListView.this.onDropDown();
        }
      });
      this.headerText.setText(this.headerDefaultText);
      this.addHeaderView(this.headerLayout);
      this.measureHeaderLayout(this.headerLayout);
      this.headerOriginalHeight = this.headerLayout.getMeasuredHeight();
      this.headerOriginalTopPadding = this.headerLayout.getPaddingTop();
      this.currentHeaderStatus = 1;
    }
  }

  public void addSecordHeader(int height) {
    View view = new LinearLayout(this.context);
    view.setPadding(0, height, 0, 0);
    this.addHeaderView(view);
  }

  public void addSecordHeader(View view) {
    this.addHeaderView(view);
  }

  private void initOnBottomStyle() {
    if (this.footerLayout != null) {
      if (this.isOnBottomStyle) {
        this.addFooterView(this.footerLayout);
      } else {
        this.removeFooterView(this.footerLayout);
      }

    } else if (this.isOnBottomStyle) {
      this.footerDefaultText = this.context.getString(R.string.drop_down_list_footer_default_text);
      this.footerLoadingText = this.context.getString(R.string.drop_down_list_footer_loading_text);
      this.footerNoMoreText = this.context.getString(R.string.drop_down_list_footer_no_more_text);
      LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.footerLayout = (RelativeLayout)inflater.inflate(R.layout.drop_down_list_footer, this, false);
      this.footerButton = (Button)this.footerLayout.findViewById(R.id.drop_down_list_footer_button);
      this.footerButton.setDrawingCacheBackgroundColor(0);
      this.footerButton.setEnabled(true);
      this.footerTextView = (TextView)this.footerLayout.findViewById(R.id.drop_down_list_footer_text);
      this.footerProgressBar = (ProgressBar)this.footerLayout.findViewById(R.id.drop_down_list_footer_progress_bar);
      this.addFooterView(this.footerLayout);
    }
  }

  public boolean isDropDownStyle() {
    return this.isDropDownStyle;
  }

  public void setDropDownStyle(boolean isDropDownStyle) {
    if (this.isDropDownStyle != isDropDownStyle) {
      this.isDropDownStyle = isDropDownStyle;
      this.initDropDownStyle();
    }

  }

  public void setDropDownStyle(boolean isDropDownStyle, boolean secondHeader, int height) {
    if (this.isDropDownStyle != isDropDownStyle) {
      this.isDropDownStyle = isDropDownStyle;
      this.initDropDownStyle();
    }

    if (secondHeader) {
      this.removeHeaderView(this.headerLayout);
      this.addSecordHeader(height);
      this.initDropDownStyle();
    }

  }

  public void setDropDownStyle(boolean isDropDownStyle, boolean secondHeader, View view) {
    if (this.isDropDownStyle != isDropDownStyle) {
      this.isDropDownStyle = isDropDownStyle;
      this.initDropDownStyle();
    }

    if (secondHeader) {
      this.removeHeaderView(this.headerLayout);
      this.addSecordHeader(view);
      this.initDropDownStyle();
    }

  }

  public boolean isOnBottomStyle() {
    return this.isOnBottomStyle;
  }

  public void setOnBottomStyle(boolean isOnBottomStyle) {
    if (this.isOnBottomStyle != isOnBottomStyle) {
      this.isOnBottomStyle = isOnBottomStyle;
      this.initOnBottomStyle();
    }

  }

  public boolean isAutoLoadOnBottom() {
    return this.isAutoLoadOnBottom;
  }

  public void setAutoLoadOnBottom(boolean isAutoLoadOnBottom) {
    this.isAutoLoadOnBottom = isAutoLoadOnBottom;
  }

  public boolean isShowFooterProgressBar() {
    return this.isShowFooterProgressBar;
  }

  public void setShowFooterProgressBar(boolean isShowFooterProgressBar) {
    this.isShowFooterProgressBar = isShowFooterProgressBar;
  }

  public boolean isShowFooterWhenNoMore() {
    return this.isShowFooterWhenNoMore;
  }

  public void setShowFooterWhenNoMore(boolean isShowFooterWhenNoMore) {
    this.isShowFooterWhenNoMore = isShowFooterWhenNoMore;
  }

  public Button getFooterButton() {
    return this.footerButton;
  }

  public void setAdapter(ListAdapter adapter) {
    super.setAdapter(adapter);
    if (this.isDropDownStyle) {
      this.setSecondPositionVisible();
    }

  }

  public void setOnScrollListener(OnScrollListener listener) {
    this.onScrollListener = listener;
  }

  public void setOnTouchListener(OnTouchListener onTouchListener) {
    this.onTouchListener = onTouchListener;
  }

  public void setOnDropDownListener(OnDropDownListener onDropDownListener) {
    this.onDropDownListener = onDropDownListener;
  }

  public void setOnBottomListener(OnClickListener onBottomListener) {
    this.onBottomListener = onBottomListener;
    this.footerButton.setOnClickListener(onBottomListener);
  }

  public boolean onTouchEvent(MotionEvent event) {
    if (this.onTouchListener != null && this.onTouchListener.onTouch(this, event)) {
      return true;
    } else if (!this.isDropDownStyle) {
      return super.onTouchEvent(event);
    } else {
      this.hasReachedTop = false;
      switch(event.getAction()) {
        case 0:
          this.actionDownPointY = event.getY();
          break;
        case 1:
          this.actionDownPointY = -1.0F;
          if (!this.isVerticalScrollBarEnabled()) {
            this.setVerticalScrollBarEnabled(true);
          }

          if (this.getFirstVisiblePosition() == 0 && this.currentHeaderStatus != 4) {
            switch(this.currentHeaderStatus) {
              case 1:
              default:
                return super.onTouchEvent(event);
              case 2:
                this.setHeaderStatusClickToLoad();
                this.setSecondPositionVisible();
                return super.onTouchEvent(event);
              case 3:
                this.onDropDown();
            }
          }
          break;
        case 2:
          if (this.actionDownPointY == -1.0F) {
            this.actionDownPointY = event.getY();
          }

          if (this.currentScrollState == 1 && this.currentHeaderStatus != 4) {
            if (this.getFirstVisiblePosition() == 0) {
              this.headerImage.setVisibility(View.VISIBLE);
              int pointBottom = this.headerOriginalHeight + this.headerReleaseMinDistance;
              if (this.headerLayout.getBottom() >= pointBottom) {
                this.setHeaderStatusReleaseToLoad();
              } else if (this.headerLayout.getBottom() < pointBottom) {
                this.setHeaderStatusDropDownToLoad();
              }
            } else {
              this.setHeaderStatusClickToLoad();
            }
          }

          this.adjustHeaderPadding(event);
      }

      return super.onTouchEvent(event);
    }
  }

  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    if (this.isDropDownStyle && (this.currentScrollState != 1 || this.currentHeaderStatus == 4)) {
      if (this.currentScrollState == 2 && firstVisibleItem == 0 && this.currentHeaderStatus != 4) {
        this.setSecondPositionVisible();
        this.hasReachedTop = true;
      } else if (this.currentScrollState == 2 && this.hasReachedTop) {
        this.setSecondPositionVisible();
      }
    }

    if (this.isOnBottomStyle && this.isAutoLoadOnBottom && this.hasMore && totalItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
      this.onBottom();
    }

    if (this.onScrollListener != null) {
      this.onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }

  }

  public void onScrollStateChanged(AbsListView view, int scrollState) {
    if (this.isDropDownStyle) {
      this.currentScrollState = scrollState;
      if (this.currentScrollState == 0) {
        this.hasReachedTop = false;
      }
    }

    if (this.onScrollListener != null) {
      this.onScrollListener.onScrollStateChanged(view, scrollState);
    }

  }

  public void onDropDownBegin() {
    if (this.isDropDownStyle) {
      this.setHeaderStatusLoading();
    }

  }

  public void onDropDown() {
    if (this.currentHeaderStatus != 4 && this.isDropDownStyle && this.onDropDownListener != null) {
      this.onDropDownBegin();
      this.onDropDownListener.onDropDown();
    }

  }

  public void onDropDownComplete(CharSequence secondText) {
    if (this.isDropDownStyle) {
      this.setHeaderSecondText(secondText);
      this.onDropDownComplete();
    }

  }

  public void setHeaderSecondText(CharSequence secondText) {
    if (this.isDropDownStyle) {
      if (secondText == null) {
        this.headerSecondText.setVisibility(GONE);
      } else {
        this.headerSecondText.setVisibility(VISIBLE);
        this.headerSecondText.setText(secondText);
      }
    }

  }

  public void onDropDownComplete() {
    if (this.isDropDownStyle) {
      this.setHeaderStatusClickToLoad();
      if (this.headerLayout.getBottom() > 0) {
        this.invalidateViews();
        this.setSecondPositionVisible();
      }
    }

  }

  public void onBottomBegin() {
    if (this.isOnBottomStyle) {
      if (this.isShowFooterProgressBar) {
        this.footerProgressBar.setVisibility(VISIBLE);
      }

      this.footerTextView.setText(this.footerLoadingText);
      this.footerButton.setEnabled(false);
    }

  }

  public void onBottom() {
    if (this.isOnBottomStyle && !this.isOnBottomLoading) {
      this.isOnBottomLoading = true;
      this.onBottomBegin();
      if (this.onBottomListener != null) {
        this.onBottomListener.onClick(this.footerButton);
      }
    }

  }

  public void onBottomComplete() {
    if (this.isOnBottomStyle) {
      if (this.isShowFooterProgressBar) {
        this.footerProgressBar.setVisibility(GONE);
      }

      if (!this.hasMore) {
        this.footerTextView.setText(this.footerNoMoreText);
        this.footerButton.setEnabled(false);
        if (!this.isShowFooterWhenNoMore) {
          this.removeFooterView(this.footerLayout);
        }
      } else {
        this.footerTextView.setText(this.footerDefaultText);
        this.footerButton.setEnabled(true);
      }

      this.isOnBottomLoading = false;
    }

  }

  public void setSecondPositionVisible() {
    if (this.getAdapter() != null && this.getAdapter().getCount() > 0 && this.getFirstVisiblePosition() == 0) {
      this.headerLayout.setPadding(this.headerLayout.getPaddingLeft(), this.headerOriginalTopPadding - this.headerOriginalHeight, this.headerLayout.getPaddingRight(), this.headerLayout.getPaddingBottom());
    }

  }

  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  public boolean isHasMore() {
    return this.hasMore;
  }

  public RelativeLayout getHeaderLayout() {
    return this.headerLayout;
  }

  public RelativeLayout getFooterLayout() {
    return this.footerLayout;
  }

  public float getHeaderPaddingTopRate() {
    return this.headerPaddingTopRate;
  }

  public void setHeaderPaddingTopRate(float headerPaddingTopRate) {
    this.headerPaddingTopRate = headerPaddingTopRate;
  }

  public int getHeaderReleaseMinDistance() {
    return this.headerReleaseMinDistance;
  }

  public void setHeaderReleaseMinDistance(int headerReleaseMinDistance) {
    this.headerReleaseMinDistance = headerReleaseMinDistance;
  }

  public String getHeaderDefaultText() {
    return this.headerDefaultText;
  }

  public void setHeaderDefaultText(int textID) {
    this.setHeaderDefaultText(this.context.getString(textID));
  }

  public void setHeaderDefaultText(String headerDefaultText) {
    this.headerDefaultText = headerDefaultText;
    if (this.headerText != null && this.currentHeaderStatus == 1) {
      this.headerText.setText(headerDefaultText);
    }

  }

  public String getHeaderPullText() {
    return this.headerPullText;
  }

  public void setHeaderPullText(int textID) {
    this.setHeaderPullText(this.context.getString(textID));
  }

  public void setHeaderPullText(String headerPullText) {
    this.headerPullText = headerPullText;
  }

  public String getHeaderReleaseText() {
    return this.headerReleaseText;
  }

  public void setHeaderReleaseText(int textID) {
    this.setHeaderReleaseText(this.context.getString(textID));
  }

  public void setHeaderReleaseText(String headerReleaseText) {
    this.headerReleaseText = headerReleaseText;
  }

  public String getHeaderLoadingText() {
    return this.headerLoadingText;
  }

  public void setHeaderLoadingText(int textID) {
    this.setHeaderLoadingText(this.context.getString(textID));
  }

  public void setHeaderLoadingText(String headerLoadingText) {
    this.headerLoadingText = headerLoadingText;
  }

  public String getFooterDefaultText() {
    return this.footerDefaultText;
  }

  public void setFooterDefaultText(int textID) {
    this.setFooterDefaultText(this.context.getString(textID));
  }

  public void setFooterDefaultText(String footerDefaultText) {
    this.footerDefaultText = footerDefaultText;
    if (this.footerButton != null && this.footerButton.isEnabled()) {
      this.footerTextView.setText(footerDefaultText);
    }

  }

  public String getFooterLoadingText() {
    return this.footerLoadingText;
  }

  public void setFooterLoadingText(int textID) {
    this.setFooterLoadingText(this.context.getString(textID));
  }

  public void setFooterLoadingText(String footerLoadingText) {
    this.footerLoadingText = footerLoadingText;
  }

  public String getFooterNoMoreText() {
    return this.footerNoMoreText;
  }

  public void setFooterNoMoreText(int textID) {
    this.setFooterNoMoreText(this.context.getString(textID));
  }

  public void setFooterNoMoreText(String footerNoMoreText) {
    this.footerNoMoreText = footerNoMoreText;
  }

  public void setHeaderStatusClickToLoad() {
    if (this.currentHeaderStatus != 1) {
      this.resetHeaderPadding();
      this.headerImage.clearAnimation();
      this.headerImage.setVisibility(View.GONE);
      this.headerProgressBar.setVisibility(GONE);
      this.headerText.setText(this.headerDefaultText);
      this.currentHeaderStatus = 1;
    }

  }

  public void setHeaderStatusDropDownToLoad() {
    if (this.currentHeaderStatus != 2) {
      this.headerImage.setVisibility(View.VISIBLE);
      if (this.currentHeaderStatus != 1) {
        this.headerImage.clearAnimation();
        this.headerImage.startAnimation(this.reverseFlipAnimation);
      }

      this.headerProgressBar.setVisibility(GONE);
      this.headerText.setText(this.headerPullText);
      if (this.isVerticalFadingEdgeEnabled()) {
        this.setVerticalScrollBarEnabled(false);
      }

      this.currentHeaderStatus = 2;
    }

  }

  public void setHeaderStatusReleaseToLoad() {
    if (this.currentHeaderStatus != 3) {
      this.headerImage.setVisibility(View.VISIBLE);
      this.headerImage.clearAnimation();
      this.headerImage.startAnimation(this.flipAnimation);
      this.headerProgressBar.setVisibility(GONE);
      this.headerText.setText(this.headerReleaseText);
      this.currentHeaderStatus = 3;
    }

  }

  public void setHeaderStatusLoading() {
    if (this.currentHeaderStatus != 4) {
      this.resetHeaderPadding();
      this.headerImage.setVisibility(GONE);
      this.headerImage.clearAnimation();
      this.headerProgressBar.setVisibility(VISIBLE);
      this.headerText.setText(this.headerLoadingText);
      this.currentHeaderStatus = 4;
      this.setSelection(0);
    }

  }

  private void adjustHeaderPadding(MotionEvent ev) {
    int pointerCount = ev.getHistorySize();
    if (this.isVerticalFadingEdgeEnabled()) {
      this.setVerticalScrollBarEnabled(false);
    }

    for(int i = 0; i < pointerCount; ++i) {
      if (this.currentHeaderStatus == 2 || this.currentHeaderStatus == 3) {
        this.headerLayout.setPadding(this.headerLayout.getPaddingLeft(), (int)((ev.getHistoricalY(i) - this.actionDownPointY - (float)this.headerOriginalHeight) / this.headerPaddingTopRate), this.headerLayout.getPaddingRight(), this.headerLayout.getPaddingBottom());
      }
    }

  }

  private void resetHeaderPadding() {
    this.headerLayout.setPadding(this.headerLayout.getPaddingLeft(), this.headerOriginalTopPadding, this.headerLayout.getPaddingRight(), this.headerLayout.getPaddingBottom());
  }

  private void measureHeaderLayout(View child) {
    ViewGroup.LayoutParams p = child.getLayoutParams();
    if (p == null) {
      p = new LayoutParams(-1, -2);
    }

    int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
    int lpHeight = p.height;
    int childHeightSpec;
    if (lpHeight > 0) {
      childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
    } else {
      childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    }

    child.measure(childWidthSpec, childHeightSpec);
  }

  private void getAttrs(Context context, AttributeSet attrs) {
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.drop_down_list_attr);
    this.isDropDownStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isDropDownStyle, false);
    this.isOnBottomStyle = ta.getBoolean(R.styleable.drop_down_list_attr_isOnBottomStyle, false);
    this.isAutoLoadOnBottom = ta.getBoolean(R.styleable.drop_down_list_attr_isAutoLoadOnBottom, false);
    ta.recycle();
  }

  public void setHeaderPaddingTop(int paddingTop) {
    if (this.headerLayout != null) {
      this.headerLayout.setPadding(this.headerLayout.getPaddingLeft(), paddingTop, this.headerLayout.getPaddingRight(), this.headerLayout.getPaddingBottom());
      this.measureHeaderLayout(this.headerLayout);
      this.headerOriginalHeight = this.headerLayout.getMeasuredHeight();
      this.headerOriginalTopPadding = this.headerLayout.getPaddingTop();
    }
  }

  public interface OnDropDownListener {
    void onDropDown();
  }
}
