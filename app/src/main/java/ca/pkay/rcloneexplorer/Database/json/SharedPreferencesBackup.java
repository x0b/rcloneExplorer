package ca.pkay.rcloneexplorer.Database.json;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import ca.pkay.rcloneexplorer.R;

public class SharedPreferencesBackup {

    public static String export(Context context) throws JSONException {

        //General Settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showThumbnails = sharedPreferences.getBoolean(context.getString(R.string.pref_key_show_thumbnails), false);
        boolean isWifiOnly = sharedPreferences.getBoolean(context.getString(R.string.pref_key_wifi_only_transfers), false);
        boolean useProxy = sharedPreferences.getBoolean(context.getString(R.string.pref_key_use_proxy), false);
        String proxyProtocol = sharedPreferences.getString(context.getString(R.string.pref_key_proxy_protocol), "http");
        String proxyHost = sharedPreferences.getString(context.getString(R.string.pref_key_proxy_host), "localhost");
        int proxyPort = sharedPreferences.getInt(context.getString(R.string.pref_key_proxy_port), 8080);

        // File Access
        boolean safEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_enable_saf), false);
        boolean refreshLaEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_refresh_local_aliases), true);
        boolean vcpEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_enable_vcp), false);
        boolean vcpDeclareLocal = sharedPreferences.getBoolean(context.getString(R.string.pref_key_vcp_declare_local), true);
        boolean vcpGrantAll = sharedPreferences.getBoolean(context.getString(R.string.pref_key_vcp_grant_all), false);

        // Look and Feel
        boolean isDarkTheme = sharedPreferences.getBoolean(context.getString(R.string.pref_key_dark_theme), false);
        boolean isWrapFilenames = sharedPreferences.getBoolean(context.getString(R.string.pref_key_wrap_filenames), true);
        int defaultColorPrimary = sharedPreferences.getInt(context.getString(R.string.pref_key_color_primary), R.color.colorPrimary);
        int defaultColorAccent = sharedPreferences.getInt(context.getString(R.string.pref_key_color_accent), R.color.colorAccent);

        // Notifications
        boolean appUpdates = sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates), true);
        boolean betaUpdates = sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates_beta), false);

        // Logging
        boolean useLogs = sharedPreferences.getBoolean(context.getString(R.string.pref_key_logs), false);
        boolean crashReports = sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports),
                context.getResources().getBoolean(R.bool.default_crash_log_enable));


        JSONObject main = new JSONObject();

        main.put("showThumbnails", showThumbnails);
        main.put("isWifiOnly", isWifiOnly);
        main.put("useProxy", useProxy);
        main.put("proxyProtocol", proxyProtocol);
        main.put("proxyHost", proxyHost);
        main.put("proxyPort", proxyPort);
        main.put("safEnabled", safEnabled);
        main.put("refreshLaEnabled", refreshLaEnabled);
        main.put("vcpEnabled", vcpEnabled);
        main.put("vcpDeclareLocal", vcpDeclareLocal);
        main.put("vcpGrantAll", vcpGrantAll);
        main.put("isDarkTheme", isDarkTheme);
        main.put("isWrapFilenames", isWrapFilenames);
        main.put("defaultColorPrimary", defaultColorPrimary);
        main.put("defaultColorAccent", defaultColorAccent);
        main.put("appUpdates", appUpdates);
        main.put("betaUpdates", betaUpdates);
        main.put("useLogs", useLogs);
        main.put("crashReports", crashReports);

        return main.toString();
    }

    public static void importJson(String json, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //General Settings
        editor.putBoolean(context.getString(R.string.pref_key_app_updates_beta), jsonObject.getBoolean("showThumbnails"));
        editor.putBoolean(context.getString(R.string.pref_key_wifi_only_transfers), jsonObject.getBoolean("isWifiOnly"));
        editor.putBoolean(context.getString(R.string.pref_key_use_proxy), jsonObject.getBoolean("useProxy"));
        editor.putString(context.getString(R.string.pref_key_proxy_protocol), jsonObject.getString("proxyProtocol"));
        editor.putString(context.getString(R.string.pref_key_proxy_host), jsonObject.getString("proxyHost"));
        editor.putInt(context.getString(R.string.pref_key_proxy_port), jsonObject.getInt("proxyPort"));

        // File Access
        editor.putBoolean(context.getString(R.string.pref_key_enable_saf), jsonObject.getBoolean("safEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_refresh_local_aliases), jsonObject.getBoolean("refreshLaEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_enable_vcp), jsonObject.getBoolean("vcpEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_vcp_declare_local), jsonObject.getBoolean("vcpDeclareLocal"));
        editor.putBoolean(context.getString(R.string.pref_key_vcp_grant_all), jsonObject.getBoolean("vcpGrantAll"));

        // Look and Feel
        editor.putBoolean(context.getString(R.string.pref_key_dark_theme), jsonObject.getBoolean("isDarkTheme"));
        editor.putBoolean(context.getString(R.string.pref_key_wrap_filenames), jsonObject.getBoolean("isWrapFilenames"));

        //Todo: migrate those keys to identifiers, to make it more robust.
        // This is bound to break since resource id's are ephemeral. Therefore they are disabled for now.
        // editor.putInt(context.getString(R.string.pref_key_color_primary), jsonObject.getInt("defaultColorPrimary"));
        // editor.putInt(context.getString(R.string.pref_key_color_accent), jsonObject.getInt("defaultColorAccent"));

        // Notifications
        editor.putBoolean(context.getString(R.string.pref_key_app_updates), jsonObject.getBoolean("appUpdates"));
        editor.putBoolean(context.getString(R.string.pref_key_app_updates_beta), jsonObject.getBoolean("betaUpdates"));

        // Logging
        editor.putBoolean(context.getString(R.string.pref_key_logs), jsonObject.getBoolean("useLogs"));
        editor.putBoolean(context.getString(R.string.pref_key_crash_reports), jsonObject.getBoolean("crashReports"));

        editor.apply();
    }

}
