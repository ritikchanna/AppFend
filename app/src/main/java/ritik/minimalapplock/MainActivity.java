package ritik.minimalapplock;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    applist_adapter mAdapter;
    List<ApplicationInfo> appInfos;
    Helper helper;
    private static final int REQUEST_CODE = 1442;
   // private DevicePolicyManager mDPM;
    private ComponentName mAdminName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        helper=new Helper(this);
        if(!helper.isKeySet()){
            Intent i=new Intent(this,login.class);
            i.putExtra("message","set_pass");
            startActivity(i);
            finish();
        }
        // Initiate DevicePolicyManager.
       // mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Set DeviceAdminDemo Receiver for active the component with different option
        mAdminName = new ComponentName(this, DeviceAdminSample.class);
        appInfos = new ArrayList<ApplicationInfo>(0);
        recyclerView = (RecyclerView) findViewById(R.id.app_recycler_view);
        mAdapter = new applist_adapter(getApplicationContext(),appInfos);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ApplicationInfo appinfo = appInfos.get(position);
                //Toast.makeText(getApplicationContext(), appinfo.packageName + " is selected!", Toast.LENGTH_SHORT).show();
                helper.toggle(appinfo.packageName);
                mAdapter.notifyItemChanged(position);
            }

            @Override
            public void onLongClick(View view, int position) {



            }
        }));
        if(helper.isKeySet())
        new GetAppList().execute();




    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!helper.mDPM.isAdminActive(mAdminName)) {
            // try to become active
            Log.d("Ritik", "Trying admin now");

            final Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
            new AlertDialog.Builder(MainActivity.this).setMessage(R.string.prevent_uninstall).setCancelable(false).setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(intent,REQUEST_CODE);
                }
            }).create().show();

        }


       else if (!isAccessibilitySettingsOn(getApplicationContext())) {

            new AlertDialog.Builder(MainActivity.this).setMessage(R.string.require_accessibility).setCancelable(false).setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
            }).create().show();

        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode)
        {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("Ritik", "admin success");
            } else {
                Log.d("Ritik", "admin fail"+" "+requestCode+" "+resultCode);
            }
        }
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + App_monitor.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d("Ritik", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Ritik", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.d("Ritik", "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.d("Ritik", "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v("Ritik", "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v("Ritik", "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }



    private class GetAppList extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdialog;

        @Override
        protected void onPreExecute() {
            pdialog = ProgressDialog.show(MainActivity.this,
                    "Loading Apps",
                    "Please wait while the apps are being loaded");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pdialog.cancel();


            mAdapter.notifyDataSetChanged();


            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final PackageManager packageManager = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resInfos = packageManager.queryIntentActivities(intent, 0);
            //using hashset so that there will be no duplicate packages,
            //if no duplicate packages then there will be no duplicate apps
            HashSet<String> packageNames = new HashSet<String>(0);


            //getting package names and adding them to the hashset
            for (ResolveInfo resolveInfo : resInfos) {
                packageNames.add(resolveInfo.activityInfo.packageName);
            }

            //now we have unique packages in the hashset, so get their application infos
            //and add them to the arraylist
            for (String packageName : packageNames) {
                try {
                    appInfos.add(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    //Do Nothing
                }
            }

            //to sort the list of apps by their names
            Collections.sort(appInfos, new ApplicationInfo.DisplayNameComparator(packageManager));
            return null;
        }
    }

}
