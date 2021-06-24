package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import ca.pkay.rcloneexplorer.util.FLog;
import es.dmoral.toasty.Toasty;

public class ActivityHelper {

    private static final String TAG = "ActivityHelper";

    @SuppressLint("CheckResult")
    public static void tryStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showErrorToast(context, intent);
        }
    }

    @SuppressLint("CheckResult")
    public static void tryStartActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        try {
            if (!fragment.isAdded()) {
                FLog.w(TAG, "tryStartActivityForResult: fragment is not attached, can't start");
                return;
            }
            fragment.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(fragment.getContext(), intent);
        }
    }

    public static void tryStartActivityForResult(Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(activity, intent);
        }
    }

    private static boolean requiresDocumentsUI(Intent intent) {
        if (null == intent || null == intent.getAction()) {
            return false;
        }
        switch (intent.getAction()) {
            case Intent.ACTION_GET_CONTENT:
            case Intent.ACTION_CREATE_DOCUMENT:
            case Intent.ACTION_OPEN_DOCUMENT:
            case Intent.ACTION_OPEN_DOCUMENT_TREE:
                return true;
            default:
                return false;
        }
    }

    private static void showErrorToast(final Context context, Intent intent) {
        Looper main = Looper.getMainLooper();
        String errorMessage = requiresDocumentsUI(intent) ?
                context.getString(R.string.documentsui_missing) :
                context.getString(R.string.no_app_found_for_this_link);
        Toast errorToast = Toasty.error(context, errorMessage, Toast.LENGTH_LONG, true);
        if (main.equals(Looper.myLooper())) {
            errorToast.show();
        } else {
            new Handler(main).post(errorToast::show);
        }
    }

    public static void tryStartService(@NonNull Context context, @NonNull Intent intent) {
        try {
            context.startService(intent);
        } catch (IllegalStateException e) {
            FLog.e(TAG, "Host context state is invalid, not starting service", e);
        }
    }

    public static void applyTheme(Activity context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int customPrimaryColor = sharedPreferences.getInt(context.getString(R.string.pref_key_color_primary), R.color.colorPrimary);
        int customAccentColor = sharedPreferences.getInt(context.getString(R.string.pref_key_color_accent), R.color.colorAccent);
        Boolean isDarkTheme = sharedPreferences.getBoolean(context.getString(R.string.pref_key_dark_theme), false);
        context.getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(context, customPrimaryColor), true);
        context.getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(context, customAccentColor), true);
        if (isDarkTheme) {
            context.getTheme().applyStyle(R.style.DarkTheme, true);
        } else {
            context.getTheme().applyStyle(R.style.LightTheme, true);
        }

        //TODO: remove when support for pre marshmallow devices is dropped.
        //      Also this will cause a regression, since now older devices wont fully apply the theme.
        //      I am not sure why this ever worked, since customPrimaryColor was always an identifier, not a color.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int color = context.getColor(customPrimaryColor);
            // set recents app color to the primary color
            ActivityManager.TaskDescription taskDesc;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                taskDesc = new ActivityManager.TaskDescription(
                        context.getString(R.string.app_name), R.mipmap.ic_launcher_round, color);
            } else {
                Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
                //noinspection deprecation
                taskDesc = new ActivityManager.TaskDescription(context.getString(R.string.app_name), bm, color);
            }
            context.setTaskDescription(taskDesc);
        }
    }
}
