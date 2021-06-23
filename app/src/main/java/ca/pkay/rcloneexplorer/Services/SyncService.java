package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Random;

import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncLog;

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_ARG";
    public static final String REMOTE_PATH_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_PATH_ARG";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.SYNC_LOCAL_PATH_ARG";
    public static final String SYNC_DIRECTION_ARG = "ca.pkay.rcexplorer.SYNC_DIRECTION_ARG";
    public static final String SHOW_RESULT_NOTIFICATION = "ca.pkay.rcexplorer.SHOW_RESULT_NOTIFICATION";
    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final String OPERATION_SUCCESS_GROUP = "ca.pkay.rcexplorer.OPERATION_SUCCESS_GROUP";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.sync_service";
    private final String CHANNEL_NAME = "Sync service";
    private final int PERSISTENT_NOTIFICATION_ID_FOR_SYNC = 162;
    private final int OPERATION_FAILED_NOTIFICATION_ID = 89;
    private final int OPERATION_SUCCESS_NOTIFICATION_ID = 698;
    private final int CONNECTIVITY_CHANGE_NOTIFICATION_ID = 462;
    private Rclone rclone;
    private Log2File log2File;
    private boolean connectivityChanged;
    private boolean transferOnWiFiOnly;
    Process currentProcess;

    public SyncService() {
        super("ca.pkay.rcexplorer.SYNC_SERCVICE");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
        log2File = new Log2File(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        transferOnWiFiOnly = sharedPreferences.getBoolean(getString(R.string.pref_key_wifi_only_transfers), false);

        if (transferOnWiFiOnly) {
            registerBroadcastReceivers();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        if (transferOnWiFiOnly && !checkWifiOnAndConnected()) {
            showConnectivityChangedNotification();
            stopSelf();
            return;
        }

        final RemoteItem remoteItem = intent.getParcelableExtra(REMOTE_ARG);
        final String remotePath = intent.getStringExtra(REMOTE_PATH_ARG);
        final String localPath = intent.getStringExtra(LOCAL_PATH_ARG);
        final int syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1);

        final boolean silentRun = intent.getBooleanExtra(SHOW_RESULT_NOTIFICATION, true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        String title;
        int slashIndex = remotePath.lastIndexOf("/");
        if (slashIndex >= 0) {
            title = remotePath.substring(slashIndex + 1);
        } else {
            title = remotePath;
        }

        Intent foregroundIntent = new Intent(this, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_service, title))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder.build());
        SyncLog.info(this, getString(R.string.start_sync), getString(R.string.syncing_service, title));

        currentProcess = rclone.sync(remoteItem, remotePath, localPath, syncDirection);
        JSONObject stats;
        String notificationContent = "";
        String[] notificationBigText = new String[5];
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    JSONObject logline = new JSONObject(line);
                    if(isLoggingEnable && logline.getString("level").equals("error")){
                        log2File.log(line);
                    } else if(logline.getString("level").equals("warning")){
                        //available stats:
                        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
                        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers
                        stats = logline.getJSONObject("stats");

                        String speed = humanReadableBytes(stats.getLong("speed"))+"/s";
                        String size = humanReadableBytes(stats.getLong("bytes"));
                        String allsize = humanReadableBytes(stats.getLong("totalBytes"));
                        double percent = ((double)  stats.getLong("bytes")/stats.getLong("totalBytes"))*100;

                        notificationContent = String.format("Transfered:   %s / %s %.0f%% %s, ETA %s s",
                        size, allsize, percent, speed, stats.get("eta"));
                        notificationBigText[0]=notificationContent;
                        notificationBigText[1]=String.format("Errors:      %d", stats.getInt("errors"));
                        notificationBigText[2]=String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks"));
                        notificationBigText[3]=String.format("Transferred: %s / %s", size, allsize);
                        notificationBigText[4]=String.format("Elapsed:     %d", stats.getInt("elapsedTime"));
                    }

                    updateNotification(title, notificationContent, notificationBigText);
                }
            } catch (IOException e) {
                FLog.e(TAG, "onHandleIntent: error reading stdout", e);
            } catch (JSONException e) {
                //e.printStackTrace();
            }

            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                FLog.e(TAG, "onHandleIntent: error waiting for process", e);
            }
        }

        sendUploadFinishedBroadcast(remoteItem.getName(), remotePath);

        int notificationId = (int)System.currentTimeMillis();

        if(silentRun){
            if (transferOnWiFiOnly && connectivityChanged) {
                showConnectivityChangedNotification();
            } else if (currentProcess == null || currentProcess.exitValue() != 0) {
                String errorTitle = getString(R.string.notification_sync_failed);
                long logEntryId = SyncLog.error(this, title, notificationContent);
                showFailedNotification(errorTitle, title, notificationId, logEntryId);
            }else{
                long logEntryId = SyncLog.info(this, getString(R.string.operation_success), notificationContent);
                showSuccessNotification(title, notificationId, logEntryId);
            }
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(PERSISTENT_NOTIFICATION_ID_FOR_SYNC);
        stopForeground(true);
    }

    public static String humanReadableBytes(long bytes) {
        //todo: implement conversion properly
        return String.valueOf(bytes)+" MiB";
    }

    private void registerBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(connectivityChangeBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver connectivityChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connectivityChanged = true;
            stopSelf();
        }
    };

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr == null) {
            FLog.e(TAG, "No Wifi found.");
            return false;
        }

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            // WifiManager requires location access. This is not available, so we query the metered instead.
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return !cm.isActiveNetworkMetered();
        }
        else {
            FLog.e(TAG, "Wifi not turned on.");
            return false; // Wi-Fi adapter is OFF
        }
    }

    private void updateNotification(String title, String content, String[] bigTextArray) {
        StringBuilder bigText = new StringBuilder();
        for (int i = 0; i < bigTextArray.length; i++) {
            bigText.append(bigTextArray[i]);
            if (i < 4) {
                bigText.append("\n");
            }
        }

        Intent foregroundIntent = new Intent(this, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_service, title))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }

        if (transferOnWiFiOnly) {
            unregisterReceiver(connectivityChangeBroadcastReceiver);
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String path) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showFailedNotification(String title, String content, int notificationId, long logEntryID) {
        createSummaryNotificationForFailed();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_error)
                .setContentTitle(title)
                .setContentText(content)
                .setGroup(OPERATION_FAILED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getLogActivityIntent(logEntryID));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

    }

    private void showSuccessNotification(String content, int notificationId, long logEntryID) {
        createSummaryNotificationForSuccess();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_success)
                .setContentTitle(getString(R.string.operation_success))
                .setContentText(content)
                .setGroup(OPERATION_SUCCESS_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getLogActivityIntent(logEntryID));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

    }

    private PendingIntent getLogActivityIntent(long logEntryID){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.MAIN_ACTIVITY_START_LOG);
        //todo: use logEntryID to jump to log entry in the future
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void showConnectivityChangedNotification() {
        //todo: add title to differentiate the logs
        SyncLog.error(this, getString(R.string.sync_cancelled), getString(R.string.wifi_connections_isnt_available));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.sync_cancelled))
                .setContentText(getString(R.string.wifi_connections_isnt_available))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(CONNECTIVITY_CHANGE_NOTIFICATION_ID, builder.build());
    }

    private void createSummaryNotificationForFailed() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.operation_failed))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.operation_failed))
                        .setSmallIcon(R.drawable.ic_notification_error)
                        .setGroup(OPERATION_FAILED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(OPERATION_FAILED_NOTIFICATION_ID, summaryNotification);
    }

    private void createSummaryNotificationForSuccess() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.operation_success))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.operation_success))
                        .setSmallIcon(R.drawable.ic_notification_success)
                        .setGroup(OPERATION_SUCCESS_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(OPERATION_SUCCESS_NOTIFICATION_ID, summaryNotification);
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.sync_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
