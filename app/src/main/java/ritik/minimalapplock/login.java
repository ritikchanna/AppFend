package ritik.minimalapplock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.media.Image;
import android.os.Build;
import android.support.constraint.ConstraintLayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.patternlockview.utils.ResourceUtils;
import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;


import java.io.File;
import java.util.List;

public class login extends AppCompatActivity implements FingerPrintAuthCallback {
    private PatternLockView mPatternLockView;
    TextView title;
    ImageView app_icon;
    String message,key,package_name;
    Helper helper;
    FingerPrintAuthHelper mFingerPrintAuthHelper;
    ImageView f_icon;


    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));

            Log.d("Ritik", "onComplete: "+message+PatternLockUtils.patternToString(mPatternLockView, pattern));
            if(PatternLockUtils.patternToString(mPatternLockView, pattern).length()>3) {
                if (message.equals("set_pass")) {
                    Log.d("Ritik", "set_pass: "+PatternLockUtils.patternToString(mPatternLockView, pattern));
                    Intent i = new Intent(getApplicationContext(), login.class);
                    i.putExtra("message", "set_pass_verify");
                    i.putExtra("key", PatternLockUtils.patternToString(mPatternLockView, pattern));
                    startActivity(i);
                    finish();


                } else if (message.equals("set_pass_verify")) {
                    if(key.equals(PatternLockUtils.patternToString(mPatternLockView, pattern))){
                       helper.storeKey(PatternLockUtils.patternToString(mPatternLockView, pattern));
                        Intent i=new Intent(getApplicationContext(),MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    }

                } else if(message.equals("verify_app")){
                    if(helper.verifyKey(PatternLockUtils.patternToString(mPatternLockView, pattern))){
                        helper.addSession(package_name);
                        finish();
                    }

                }
                else {
                    if(helper.verifyKey(PatternLockUtils.patternToString(mPatternLockView, pattern))){
                        Log.d("Ritik", "Without message login complete: ");

                        new AlertDialog.Builder(login.this).setMessage(R.string.clear_data).setCancelable(false).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                helper.reset();
                                finish();
                            }
                        }).create().show();

                    }

                }
            }
            mPatternLockView.clearPattern();
        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        ConstraintLayout loginscreen= (ConstraintLayout) findViewById(R.id.login);
        loginscreen.setBackgroundDrawable(wallpaperDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

       mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
        f_icon=(ImageView)findViewById(R.id.fingerprint_icon);

helper=new Helper(this);
        title=(TextView)findViewById(R.id.lock_Text);
        app_icon=(ImageView)findViewById(R.id.app_icon);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();
try{
        message=(String)b.get("message");
        key=(String)b.get("key");
        package_name=(String)b.get("package");
}catch (Exception e){
    message="null";
    key="null";
    package_name="null";
}
if((message.equals("set_pass")||message.equals("set_pass_verify"))&&helper.isKeySet())
    finish();

        if(b!=null)
        {
            if(message.equals("set_pass"))
           title.setText(R.string.set_password);
            else if(message.equals("set_pass_verify"))
                title.setText(R.string.verify_password);
            else {
                final PackageManager pm = getApplicationContext().getPackageManager();
                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(package_name, 0);
                } catch (final PackageManager.NameNotFoundException e) {
                    ai = null;
                }
                if(ai!=null) {
                    title.setText((ai != null ? pm.getApplicationLabel(ai) : ""));
                    app_icon.setImageDrawable(pm.getApplicationIcon(ai));
                    app_icon.setVisibility(View.VISIBLE);
                }
            }

        }
        mPatternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        mPatternLockView.setDotCount(3);
        mPatternLockView.setDotNormalSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_size));
        mPatternLockView.setDotSelectedSize((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_dot_selected_size));
        mPatternLockView.setPathWidth((int) ResourceUtils.getDimensionInPx(this, R.dimen.pattern_lock_path_width));
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.white));
        mPatternLockView.setInStealthMode(false);
        mPatternLockView.setTactileFeedbackEnabled(true);
        mPatternLockView.setInputEnabled(true);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);


    }


    @Override
    public void onBackPressed() {
        if(message.equals("set_pass_verify")){
            Intent i = new Intent(getApplicationContext(), login.class);
            i.putExtra("message", "set_pass");
            startActivity(i);
            finish();
        }
        else{
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
        finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(message.equals("set_pass_verify")||message.equals("set_pass"))
            f_icon.setVisibility(View.INVISIBLE);
        else
        mFingerPrintAuthHelper.startAuth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFingerPrintAuthHelper.stopAuth();
    }

    @Override
    public void onNoFingerPrintHardwareFound() {
        //Device does not have finger print scanner.
        f_icon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNoFingerPrintRegistered() {
        //There are no finger prints registered on this device.
        f_icon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBelowMarshmallow() {
        //Device running below API 23 version of android that does not support finger print authentication.
        f_icon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        //Authentication sucessful.
        if(!message.equals("null")) {
            helper.addSession(package_name);
            finish();
        }
        else {
            new AlertDialog.Builder(login.this).setMessage(R.string.clear_data).setCancelable(false).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    helper.reset();
                    finish();
                }
            }).create().show();

        }



    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {    //Parse the error code for recoverable/non recoverable error.
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                //Cannot recognize the fingerprint scanned.
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                //This is not recoverable error. Try other options for user authentication. like pin, password.
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                //Any recoverable error. Display message to the user.
                break;
        }
    }


    }

