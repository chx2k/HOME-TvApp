package com.sony.sel.tvapp.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sony.sel.tvapp.R;
import com.sony.sel.tvapp.util.DlnaObjects.VideoProgram;
import com.sony.sel.tvapp.util.EventBus;
import com.sony.sel.tvapp.util.EventBus.RecordingsChangedEvent;
import com.sony.sel.tvapp.util.SettingsHelper;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * View
 */
public class SearchResultCell extends BaseListCell<VideoProgram> {

  private VideoProgram program;
  private SettingsHelper settingsHelper;

  @Bind(android.R.id.text1)
  TextView title;
  @Bind(android.R.id.text2)
  TextView details;
  @Bind(R.id.icon)
  ImageView icon;
  @Bind(R.id.recordProgram)
  ImageView recordProgram;
  @Bind(R.id.recordSeries)
  ImageView recordSeries;

  public SearchResultCell(Context context) {
    super(context);
  }

  public SearchResultCell(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SearchResultCell(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public SearchResultCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
    setupFocus(null, 1.03f);
    settingsHelper = SettingsHelper.getHelper(getContext());
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    EventBus.getInstance().register(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    EventBus.getInstance().unregister(this);
  }

  @Override
  public void bind(VideoProgram data) {
    program = data;

    title.setText(data.getTitle());

    String time = new SimpleDateFormat("M/d/yy h:mm a").format(data.getScheduledStartTime());
    details.setText(time);

    if (settingsHelper.getSeriesToRecord().contains(program.getTitle())) {
      recordSeries.setVisibility(View.VISIBLE);
      recordProgram.setVisibility(View.GONE);
    } else if (settingsHelper.getProgramsToRecord().contains(program.getId())) {
      recordSeries.setVisibility(View.GONE);
      recordProgram.setVisibility(View.VISIBLE);
    } else {
      recordSeries.setVisibility(View.GONE);
      recordProgram.setVisibility(View.GONE);
    }

    if (program.getIcon() != null && program.getIcon().length() > 0) {
      Picasso.with(getContext()).load(Uri.parse(program.getIcon())).into(icon);
    } else {
      icon.setImageDrawable(null);
    }

  }

  @Override
  public VideoProgram getData() {
    return program;
  }

  @Subscribe
  public void onRecordingsChanged(RecordingsChangedEvent event) {
    // rebind to refresh display
    bind(program);
  }

}
