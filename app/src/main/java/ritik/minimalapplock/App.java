package ritik.minimalapplock;

import android.graphics.drawable.Drawable;

/**
 * Created by superuser on 9/30/17.
 */

public class App {
    String appname , pname;
    Drawable icon;
    public App() {
    }

    public App(String appname, String pname, Drawable icon) {
        this.appname = appname;
        this.pname = pname;
        this.icon = icon;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
