package com.reactnativenavigation.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

public class NavigationBarUtils {
  private static int navigationBarHeight = -1;
  private static int systemNavigationType = -1;

  public static void saveNavigationBarHeight(int height) {
    navigationBarHeight = height;
  }

  public static int getNavigationBarHeight(Context context) {
    if (navigationBarHeight > 0) {
      return navigationBarHeight;
    }
    final Resources resources = context.getResources();
    final int orientation = resources.getConfiguration().orientation;
    final int resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
    if (resourceId > 0) {
      navigationBarHeight = resources.getDimensionPixelSize(resourceId);
    }

    return navigationBarHeight;
  }

  // 0: Navigation is displaying with 3 buttons
  // 1: Navigation is displaying with 2 button(Android P navigation mode)
  // 2: Full screen gesture(Gesture on android Q)
  // https://stackoverflow.com/questions/56689210/how-to-detect-full-screen-gesture-mode-in-android-10
  public static int getSystemNavigationType(Context context) {
    if (systemNavigationType > 0) {
      return systemNavigationType;
    }
    final Resources resources = context.getResources();
    final int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
    if (resourceId > 0) {
      systemNavigationType = resources.getInteger(resourceId);
    } else {
      systemNavigationType = 0;
    }
    return systemNavigationType;
  }

}
