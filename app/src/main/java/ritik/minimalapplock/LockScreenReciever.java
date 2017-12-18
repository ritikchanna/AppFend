package ritik.minimalapplock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockScreenReciever extends BroadcastReceiver {


    public Helper helper;

    @Override
    public void onReceive(Context context, Intent intent) {
       helper = new Helper(context);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("Ritik", "onReceive: Screen off");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
           helper.removeSession();
            Log.d("Ritik", "onReceive: Screen on");
        }
    }
}
