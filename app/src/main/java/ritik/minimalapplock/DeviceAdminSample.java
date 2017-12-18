package ritik.minimalapplock;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class DeviceAdminSample extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d("Ritik", " Device admin Enabled: ");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d("Ritik","Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        Log.d("Ritik","Password was changed successfully");
    }



}