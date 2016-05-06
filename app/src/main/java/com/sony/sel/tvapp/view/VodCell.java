package com.sony.sel.tvapp.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sony.sel.tvapp.R;
import com.sony.sel.tvapp.util.SettingsHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sony.sel.tvapp.util.DlnaObjects.DlnaObject;
import static com.sony.sel.tvapp.util.DlnaObjects.VideoItem;

/**
 * Cell for Displaying VOD items
 */
public class VodCell extends BaseListCell<DlnaObject> {

  @Bind(R.id.icon)
  ImageView icon;
  @Bind(R.id.title)
  TextView title;

  private DlnaObject data;
  private SettingsHelper settingsHelper;

  public VodCell(Context context) {
    super(context);
  }

  public VodCell(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VodCell(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public VodCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
    setupFocus(null,1.02f);
    settingsHelper = SettingsHelper.getHelper(getContext());
  }

  @Override
  public void bind(DlnaObject data) {
    this.data = data;
    this.title.setText(data.getTitle());
    icon.setImageDrawable(null);
    String iconUrl = data.getIcon();
    if (iconUrl != null && iconUrl.length() > 0) {
      Picasso.with(getContext()).load(Uri.parse(iconUrl)).into(icon);
    }
  }

  @Override
  public DlnaObject getData() {
    return data;
  }
}
