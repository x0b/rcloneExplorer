package ca.pkay.rcloneexplorer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ca.pkay.rcloneexplorer.Services.TriggerService;
import ca.pkay.rcloneexplorer.util.FLog;

public class TriggerReciever extends BroadcastReceiver {

    private static final String TAG = "TriggerReciever";

    @Override
    public void onReceive(Context context, Intent intent) {
        FLog.e(TAG, "Recieved Intent");

        assert intent != null;
        if(intent.getAction().equals(TriggerService.TRIGGER_RECIEVE)){
            long i = intent.getLongExtra(TriggerService.TRIGGER_ID, -1);
            FLog.e(TAG, "Start Trigger: "+i);
            if(i==-1)
                return;

            Intent service = new Intent(context, TriggerService.class);
            service.setAction(TriggerService.TRIGGER_RECIEVE);
            service.putExtra(TriggerService.TRIGGER_ID, i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            }else{
                context.startService(service);
            }
        }
    }

}
