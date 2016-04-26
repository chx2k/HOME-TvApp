package com.sony.sel.tvapp.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.sony.sel.tvapp.R;
import com.sony.sel.tvapp.util.SettingsHelper;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sony.sel.tvapp.util.DlnaObjects.VideoBroadcast;

/**
 * Cell for displaying channel info.
 */
public class ChannelCell extends BaseListCell<VideoBroadcast> {

  private VideoBroadcast channel;

  @Bind(R.id.channelIcon)
  ImageView icon;
  @Bind(R.id.channelName)
  TextView title;
  @Bind(R.id.channelNetwork)
  TextView details;
  @Bind(R.id.favorite)
  ImageView favorite;

  public ChannelCell(Context context) {
    super(context);
  }

  public ChannelCell(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ChannelCell(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ChannelCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void bind(final VideoBroadcast channel) {

    ButterKnife.bind(this);

    this.channel = channel;

    // icon
    if (channel.getIcon() != null) {
      // use channel icon
      icon.setVisibility(View.VISIBLE);
      icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
      int padding = getResources().getDimensionPixelSize(R.dimen.channelThumbPadding);
      icon.setPadding(padding, padding, padding, padding);
      Picasso.with(getContext()).load(Uri.parse(channel.getIcon())).into(icon);
    } else {
      // no icon available
      icon.setVisibility(View.GONE);
    }

    // call sign
    title.setText(channel.getCallSign());

    // number
    details.setText(channel.getChannelNumber());

    if (SettingsHelper.getHelper(getContext()).getFavoriteChannels().contains(channel.getChannelId())) {
      favorite.setVisibility(View.VISIBLE);
    } else {
      favorite.setVisibility(View.GONE);
    }

    setupFocus(null, 1.1f);

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        SettingsHelper.getHelper(getContext()).setCurrentChannel(channel);
      }
    });
    setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showChannelPopup(v, channel);
        return true;
      }
    });
  }

  private void showChannelPopup(View v, final VideoBroadcast channel) {
    final SettingsHelper settingsHelper = SettingsHelper.getHelper(getContext());
    PopupMenu menu = new PopupMenu(getContext(), v);
    menu.inflate(R.menu.channel_popup_menu);
    if (settingsHelper.getFavoriteChannels().contains(channel.getChannelId())) {
      menu.getMenu().removeItem(R.id.addToFavoriteChannels);
    } else {
      menu.getMenu().removeItem(R.id.removeFromFavoriteChannels);
    }
    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.addToFavoriteChannels:
            settingsHelper.addFavoriteChannel(channel.getChannelId());
            // re-bind to update display
            bind(channel);
            return true;
          case R.id.removeFromFavoriteChannels:
            settingsHelper.removeFavoriteChannel(channel.getChannelId());
            // re-bind to update display
            bind(channel);
            return true;
        }
        return false;
      }
    });
    menu.show();
  }

  @Override
  public VideoBroadcast getData() {
    return channel;
  }
}
