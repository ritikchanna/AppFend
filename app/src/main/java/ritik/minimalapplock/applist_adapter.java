package ritik.minimalapplock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by superuser on 9/30/17.
 */

public class applist_adapter extends RecyclerView.Adapter<applist_adapter.MyViewHolder> {

    private List<ApplicationInfo> appList;
    private Context context;
    final PackageManager packageManager;
    Helper helper;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView aname, pname;
        public ImageView icon;


        public MyViewHolder(View view) {
            super(view);
            aname = (TextView) view.findViewById(R.id.aname);
            pname = (TextView) view.findViewById(R.id.pname);
            icon = (ImageView) view.findViewById(R.id.appicon);

        }
    }


    public applist_adapter(Context context, List<ApplicationInfo> appList) {
        this.context=context;
        this.appList = appList;
        packageManager = context.getPackageManager();
        helper=new Helper(context);

    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_row_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.aname.setText(packageManager.getApplicationLabel(appList.get(position)));
        holder.pname.setText(appList.get(position).packageName);
        if(helper.isLocked(appList.get(position).packageName)) {
            holder.icon.setBackground(packageManager.getApplicationIcon(appList.get(position)));
            holder.icon.getBackground().setAlpha(120);

            holder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tick));
        }
            else{
        holder.icon.setImageDrawable(packageManager.getApplicationIcon(appList.get(position)));
            holder.icon.setBackground(packageManager.getApplicationIcon(appList.get(position)));
            holder.icon.getBackground().setAlpha(255);
            }

    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

}
