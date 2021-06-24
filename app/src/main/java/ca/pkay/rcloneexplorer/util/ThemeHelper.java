package ca.pkay.rcloneexplorer.util;

import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;

import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.ActivityHelper;
import ca.pkay.rcloneexplorer.R;

/**
 * Copyright (C) 2021  Felix Nüsse
 * Created on 13.06.21 - 22:43
 *
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 */

public class ThemeHelper {


    public static void applyTheme(Activity activity) {
        ActivityHelper.applyTheme(activity);
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        activity.getWindow().setStatusBarColor(typedValue.data);
    }

    public static boolean isDarkTheme(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(activity.getString(R.string.pref_key_dark_theme), false);
    }
}
