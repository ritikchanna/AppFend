package ritik.minimalapplock;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;



import java.util.Iterator;
import java.util.List;

public class App_monitor extends AccessibilityService {

Helper helper;

    private LockScreenReciever mScreenReceiver;
    @Override
    protected void onServiceConnected() {
        Log.d("Ritik", "Accessibility service connected: ");
        super.onServiceConnected();
        helper=new Helper(getApplicationContext());
        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
        registerScreenStatusReceiver();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("Ritik", "Event is "+event.getEventType()+" "+event.getClassName()+" "+event.getContentDescription());

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()

                );
                Log.d("Ritik", "onAccessibilityEvent: "+event.getClassName()+event.getContentDescription());


                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity)
                    if((helper.isLocked(event.getPackageName().toString())||event.getPackageName().equals(this.getPackageName())||event.getClassName().toString().contains("ccessibility")||event.getClassName().toString().contains("DeviceAdminAdd"))&&(!helper.isSession(event.getPackageName().toString()))&&(!(event.getClassName().toString()).equals(getPackageName()+".login"))){
                        Log.d("Ritik", "actual name: "+event.getClassName().toString());
                        Log.d("Ritik", "figured name: "+getPackageName()+".login");
                       Intent i=new Intent(this,login.class);
                        i.putExtra("message","verify_app");
                        i.putExtra("package",event.getPackageName().toString());
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if(!(event.getPackageName().equals(this.getPackageName())||event.getClassName().toString().contains("ccessibility")||event.getClassName().toString().contains("DeviceAdminAdd")))
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);



                        startActivity(i);

                    }
            }
        }
        else{
            Log.d("Ritik", "Other event "+event.getEventType()+" "+event.getClassName()+" "+event.getContentDescription());

        }
    }
    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenStatusReceiver();
    }

    private void registerScreenStatusReceiver() {
        Log.d("Ritik", "registerScreenStatusReceiver initiated ");
        mScreenReceiver = new LockScreenReciever();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter);
    }

    private void unregisterScreenStatusReceiver() {
        Log.d("Ritik", "unregisterScreenStatusReceiver initiated");
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
                helper.mDPM.lockNow();

            }
        } catch (IllegalArgumentException e) {}
    }
}