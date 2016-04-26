package com.sony.sel.tvapp.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.sony.sel.tvapp.R;
import com.sony.sel.tvapp.fragment.BaseFragment;
import com.sony.sel.tvapp.fragment.ChannelInfoFragment;
import com.sony.sel.tvapp.fragment.NavigationFragment;
import com.sony.sel.tvapp.fragment.VideoFragment;
import com.sony.sel.tvapp.ui.NavigationItem;
import com.sony.sel.tvapp.util.EventBus;
import com.sony.sel.tvapp.util.SettingsHelper;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;

/**
 * Main Activity
 */
public class MainActivity extends BaseActivity {

  public static final String TAG = MainActivity.class.getSimpleName();

  private VideoFragment videoFragment;
  private ChannelInfoFragment channelInfoFragment;
  private BaseFragment currentFragment;
  private NavigationFragment navigationFragment;

  public static final long HIDE_UI_TIMEOUT = 5000;
  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    @Override
    public void run() {
      hideUi();
    }
  };

  @Override
  protected void onPause() {
    super.onPause();
    hideUi();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String urn = SettingsHelper.getHelper(this).getEpgServer();
    if (urn == null) {
      // go to server selection
      startActivity(new Intent(this, SelectServerActivity.class));
      finish();
      return;
    }

    setContentView(R.layout.main_activity);
    ButterKnife.bind(this);

    initFragments();

  }

  @Override
  public void onVisibleBehindCanceled() {
    super.onVisibleBehindCanceled();
    videoFragment.stop();
  }

  /**
   * Initialize fragments.
   * <p/>
   * Note that this also can be called when Fragments have automatically been restored by Android.
   * In this case we need to attach and configure existing Fragments instead of making new ones.
   */
  private void initFragments() {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    // video playback fragment
    videoFragment = (VideoFragment) fragmentManager.findFragmentByTag(VideoFragment.TAG);
    if (videoFragment == null) {
      // create a new fragment and add it
      videoFragment = new VideoFragment();
      transaction.add(R.id.videoFrame, videoFragment, VideoFragment.TAG);
    }

    // channel info overlay fragment
    channelInfoFragment = (ChannelInfoFragment) fragmentManager.findFragmentByTag(ChannelInfoFragment.TAG);
    if (channelInfoFragment == null) {
      // create a new fragment and add it
      channelInfoFragment = new ChannelInfoFragment();
      transaction.add(R.id.videoFrame, channelInfoFragment, ChannelInfoFragment.TAG);
    }
    // intially hidden
    transaction.hide(channelInfoFragment);

    // navigation fragment
    navigationFragment = (NavigationFragment) fragmentManager.findFragmentByTag(NavigationFragment.TAG);
    if (navigationFragment == null) {
      // create a new fragment and add it
      navigationFragment = new NavigationFragment();
      transaction.add(R.id.navigationFrame, navigationFragment, NavigationFragment.TAG);
    }
    // intially hidden
    transaction.hide(navigationFragment);

    // check if any navigation fragments are visible, and hide them if they are
    for (NavigationItem item : NavigationItem.values()) {
      if (item.getTag() != null) {
        Fragment fragment = fragmentManager.findFragmentByTag(item.getTag());
        if (fragment != null) {
          // android restored the fragment for us, so hide it
          transaction.hide(fragment);
        }
      }
    }

    transaction.commit();
  }

  @Subscribe
  public void onNavigate(EventBus.NavigationClickedEvent event) {

    if (currentFragment != null && currentFragment.getTag().equals(event.getItem().getTag())) {
      // same item
      return;
    }

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    if (currentFragment != null) {
      transaction.hide(currentFragment);
    }

    NavigationItem item = event.getItem();
    currentFragment = (BaseFragment) fragmentManager.findFragmentByTag(item.getTag());
    if (currentFragment != null) {
      // fragment already exists, just show it
      transaction.show(currentFragment);
    } else {
      // create the fragment
      currentFragment = (BaseFragment) event.getItem().getFragment();
      if (currentFragment != null) {
        transaction.add(R.id.contentFrame, currentFragment, item.getTag());
      }
    }
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    transaction.commit();

    stopUiTimer();
  }

  private void hideCurrentFragment() {
    // pop the detail fragment
    if (currentFragment != null) {
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.hide(currentFragment);
      fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      fragmentTransaction.commit();
      currentFragment = null;
      navigationFragment.requestFocus();
    }
  }

  @Subscribe
  public void onChannelChanged(EventBus.ChannelChangedEvent event) {
    showChannelInfo();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean handled = false;
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
          if (currentFragment != null) {
            hideCurrentFragment();
            handled = true;
          } else if (isUiVisible()) {
            hideUi();
            handled = true;
          } else {
            // back when navigation fragment focused, confirm quitting the app.
            new AlertDialog.Builder(this).setMessage(R.string.quitConfirmation).setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            }).setNegativeButton(R.string.cancel, null).create().show();
            handled = true;
          }
        }
        break;
        case KeyEvent.KEYCODE_CHANNEL_UP:
          channelInfoFragment.nextChannel();
          handled = true;
          break;
        case KeyEvent.KEYCODE_CHANNEL_DOWN:
          channelInfoFragment.previousChannel();
          handled = true;
          break;
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER: {
          if (!navigationFragment.isVisible()) {
            showNavigation();
            handled = true;
          }
        }
        break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
          videoFragment.pause();
          handled = true;
          break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
          videoFragment.stop();
          handled = true;
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY:
          videoFragment.play();
          handled = true;
          break;
        case KeyEvent.KEYCODE_INFO:
          if (channelInfoFragment.isVisible()) {
            hideUi();
          } else {
            showChannelInfo();
          }
          handled = true;
          break;
      }
    }
    // keep ui visible after any key press
    resetUiTimer();
    return handled ? handled : super.onKeyDown(keyCode, event);
  }

  void resetUiTimer() {
    handler.removeCallbacks(runnable);
    handler.postDelayed(runnable, HIDE_UI_TIMEOUT);
  }

  void stopUiTimer() {
    handler.removeCallbacks(runnable);
  }

  boolean isUiVisible() {
    return navigationFragment.isVisible() || channelInfoFragment.isVisible() || (currentFragment != null);
  }

  void hideUi() {
    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.hide(channelInfoFragment);
    transaction.hide(navigationFragment);
    if (currentFragment != null) {
      transaction.hide(currentFragment);
    }
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    transaction.commit();
    handler.removeCallbacks(runnable);
  }

  void showNavigation() {
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    fragmentTransaction.show(navigationFragment);
    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    fragmentTransaction.commit();
    navigationFragment.requestFocus();
    resetUiTimer();
  }

  void showChannelInfo() {
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    fragmentTransaction.show(channelInfoFragment);
    fragmentTransaction.hide(navigationFragment);
    if (currentFragment != null) {
      fragmentTransaction.hide(currentFragment);
      currentFragment = null;
    }
    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    fragmentTransaction.commit();
    resetUiTimer();
    channelInfoFragment.requestFocus();
  }


}
