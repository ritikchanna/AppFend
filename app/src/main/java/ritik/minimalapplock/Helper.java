package ritik.minimalapplock;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by superuser on 9/30/17.
 */

public class Helper {





    SharedPreferences lockedapps,sessionapps;
    Context context;
    public DevicePolicyManager mDPM;

public Helper(Context context){
    lockedapps = context.getSharedPreferences("Applock", MODE_PRIVATE);
    sessionapps=context.getSharedPreferences("sessionapps",MODE_PRIVATE);
    mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    this.context=context;

    }

    public void toggle(String Package_name){
        SharedPreferences.Editor editor = lockedapps.edit();
        if(isLocked(Package_name)){
            editor.putString(encrypt(Package_name), encrypt("false"));

        }
        else{

            editor.putString(encrypt(Package_name), encrypt("true"));

        }
        editor.apply();
    }
    public Boolean isLocked(String Package_name){

        String prefs = lockedapps.getString(encrypt(Package_name), encrypt("default"));
        prefs= decrypt(prefs);
        if(prefs.equals("true"))
        return true;
        else
            return false;
    }
    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decrypt(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }
    public void addSession(String package_name){
        SharedPreferences.Editor editor = sessionapps.edit();
        editor.putString(encrypt(package_name), encrypt("true"));
        editor.apply();
    }
    public void removeSession(){
        SharedPreferences.Editor editor = sessionapps.edit();
        editor.clear();
        editor.apply();
    }
    public Boolean isSession(String package_name){
        String prefs = sessionapps.getString(encrypt(package_name), encrypt("default"));
        prefs= decrypt(prefs);
        if(prefs.equals("true"))
            return true;
        else
            return false;
    }
    public void storeKey(String key){
        Log.d("Ritik", "storeKey: "+key);
        SharedPreferences.Editor editor = lockedapps.edit();
        editor.putString(encrypt("key"), encrypt(key));
        editor.apply();
    }
    public Boolean verifyKey(String key){
        String prefs = lockedapps.getString(encrypt("key"), encrypt("default"));
        prefs= decrypt(prefs);
        if(prefs.equals(key))
            return true;
        else
            return false;

    }
    public Boolean isKeySet(){
        String prefs = lockedapps.getString(encrypt("key"), encrypt("default"));
        prefs= decrypt(prefs);
        Log.d("Ritik", "isKeySet: "+prefs);
        if(prefs.equals("default"))
            return false;
        else
            return true;
    }
    public void reset(){
       lockedapps.edit().clear().apply();
        sessionapps.edit().clear().apply();


    }



}
