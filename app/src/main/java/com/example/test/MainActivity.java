package com.example.test;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.Fragment;

import android.accessibilityservice.AccessibilityService;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.usage.UsageStatsManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {


    private List<MainActivity.AppList> installedApps;
    private MainActivity.AppAdapter installedAppAdapter;
    ListView userInstalledApps;
    long time = System.currentTimeMillis();
    private static Context context;
    LineGraphSeries<DataPoint> series;

    private static final String TAG = "MyActivity";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        if(!granted){
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);





        userInstalledApps = (ListView) findViewById(R.id.installed_app_list);

        installedApps = getInstalledApps();
        installedAppAdapter = new MainActivity.AppAdapter(MainActivity.this, installedApps);
        userInstalledApps.setAdapter(installedAppAdapter);
        userInstalledApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


                String pkg = installedApps.get(i).packages.substring(0, installedApps.get(i).packages.indexOf(" "));
                String[] colors = {" Open App", " App Info"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose Action")
                        .setItems(colors, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position of the selected item
                                if (which==0){
                                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkg /*installedApps.get(i).packages*/);
                                    if(intent != null){
                                        startActivity(intent);
                                    }
                                    else {
                                        Toast.makeText(MainActivity.this, pkg/*installedApps.get(i).packages*/ + " Error, Please Try Again...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (which==1){
                                    //Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    //Intent intent = new Intent(Settings.ACTION_APP_USAGE_SETTINGS);
                                    Intent intent = new Intent(Settings.ACTION_APP_USAGE_SETTINGS);

                                    //intent.setData(Uri.parse("package:" + pkg /*installedApps.get(i).packages*/));
                                    //intent.putExtra("EXTRA_PACKAGE_NAME:", pkg);
                                    intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
                                    Toast.makeText(MainActivity.this, pkg /*installedApps.get(i).packages*/, Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                }
                            }
                        });
                builder.show();

            }
        });



    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        /*
                        case R.id.navigation_home:
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                            selectedFragment = new HomeFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    selectedFragment).commit();
                            break;
                        */
                        case R.id.navigation_dashboard:
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            break;

                        /*
                        case R.id.navigation_settings:
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                            selectedFragment = new SettingsFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    selectedFragment).commit();
                            break;

                         */
                    }


                    return true;
                }
            };


    private List<MainActivity.AppList> getInstalledApps(){
        PackageManager pm = getPackageManager();

        ApplicationInfo ai;
        long upTimeSec1 = 0;
        int userInstalledAppsSize = 0;
        List<MainActivity.AppList> apps = new ArrayList<MainActivity.AppList>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);

        System.out.println("pack size: "+packs.size());
        double x, y;
        x = 0;
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, +0 );

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(cal.get(Calendar.DAY_OF_MONTH)));
        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, cal.getTimeInMillis(), System.currentTimeMillis());


        for (UsageStats us : queryUsageStats) {

            String appName = "";
            Drawable icon = null;

            String packages = us.getPackageName();



            try {
                if ((pm.getApplicationInfo(packages,0).flags & ApplicationInfo.FLAG_SYSTEM) != 1){
                    userInstalledAppsSize++;
                    appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packages, PackageManager.GET_META_DATA));
                    icon = pm.getApplicationIcon(packages);
                    long usageTimeSec = (us.getTotalTimeInForeground() / 1000);
                    String usageTime = ""+DateUtils.formatElapsedTime(usageTimeSec);

                    System.out.println("names: "+ packages +"time: "+usageTime);
                    System.out.println(apps.size());
                    System.out.println(usageTimeSec);


                    for(int i = 0; i < apps.size(); i++){


                        System.out.println(apps.get(i).name == appName);
                        if(apps.get(i).name == appName){
                            String usgTime = apps.get(i).packages.substring(apps.get(i).packages.indexOf(" ") + 11 , apps.get(i).packages.length());

                            String[] units = usgTime.split(":"); //will break the string up into an array
                            int hours = 0; //first element
                            int minutes = 0; //first element
                            int seconds = 0; //second element
                            long usgTimeSec = 0;

                            if(units.length == 3){
                                hours = Integer.parseInt(units[0]); //first element
                                minutes = Integer.parseInt(units[1]); //first element
                                seconds = Integer.parseInt(units[2]); //second element
                                usgTimeSec = 3600 * hours + 60 * minutes + seconds;
                            }else{
                                minutes = Integer.parseInt(units[0]); //first element
                                seconds = Integer.parseInt(units[1]); //second element
                                usgTimeSec = 60 * minutes + seconds;

                            }
                            System.out.println(usgTimeSec);

                            apps.remove(i);
                            usageTimeSec = (usageTimeSec + usgTimeSec);
                            usageTime = ""+DateUtils.formatElapsedTime(usageTimeSec);



                        }
                    }

                    apps.add(new MainActivity.AppList(appName, icon, packages+" TimeUsed: "+usageTime));





                }

            }catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();


            }



            System.out.println("usg size: "+userInstalledAppsSize);
            Collections.sort(apps, new Comparator<MainActivity.AppList>() {
                @Override
                public int compare(AppList o1, AppList o2) {

                    String usageTimeO1 =  o1.packages.substring(o1.packages.indexOf(" ")+11, o1.packages.length());
                    String[] unitsO1 = usageTimeO1.split(":"); //will break the string up into an array

                    int hoursO1 = 0; //first element
                    int minutesO1 = 0; //first element
                    int secondsO1 = 0; //second element
                    long usgTimeSecO1 = 0;

                    if(unitsO1.length == 3){
                        hoursO1 = Integer.parseInt(unitsO1[0]); //first element
                        minutesO1 = Integer.parseInt(unitsO1[1]); //first element
                        secondsO1 = Integer.parseInt(unitsO1[2]); //second element
                        usgTimeSecO1 = 3600 * hoursO1 + 60 * minutesO1 + secondsO1;
                    }else{
                        minutesO1 = Integer.parseInt(unitsO1[0]); //first element
                        secondsO1 = Integer.parseInt(unitsO1[1]); //second element
                        usgTimeSecO1 = 60 * minutesO1 + secondsO1;
                    }

                    String usageTimeO2 =  o2.packages.substring(o2.packages.indexOf(" ")+11, o2.packages.length());
                    String[] unitsO2 = usageTimeO2.split(":"); //will break the string up into an array
                    System.out.println(usageTimeO2);

                    int hoursO2 = 0; //first element
                    int minutesO2 = 0; //first element
                    int secondsO2 = 0; //second element
                    long usgTimeSecO2 = 0;

                    if(unitsO2.length == 3){
                        hoursO2 = Integer.parseInt(unitsO2[0]); //first element
                        minutesO2 = Integer.parseInt(unitsO2[1]); //first element
                        secondsO2 = Integer.parseInt(unitsO2[2]); //second element
                        usgTimeSecO2 = 3600 * hoursO2 + 60 * minutesO2 + secondsO2;
                    }else{
                        minutesO2 = Integer.parseInt(unitsO2[0]); //first element
                        secondsO2 = Integer.parseInt(unitsO1[1]); //second element
                        usgTimeSecO2 = 60 * minutesO2 + secondsO2;

                    }
                    System.out.println(usgTimeSecO2);
                    return Long.compare(usgTimeSecO2, usgTimeSecO1);
                }
            });
            for (int i = 5; i < apps.size(); i++){
                apps.remove(i);
            }


        }
        int i3 = 0;
        series = new LineGraphSeries<DataPoint>();
        for(int i = cal2.get(Calendar.DAY_OF_MONTH)-1; i <= cal2.get(Calendar.DAY_OF_MONTH) && i >= 0; i--){
            System.out.println(i);
            System.out.println(i3);
            System.out.println(cal.getTimeInMillis() + i3 *(86400000));
            System.out.println(System.currentTimeMillis()-i*(86400000));
            List<UsageStats> queryUsageStatsGraph = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis() + i3*(86400000), System.currentTimeMillis() -i*(86400000));
            i3++;
            for (UsageStats us2 : queryUsageStatsGraph) {

                try {
                    String packagesGraph = us2.getPackageName();
                    if ((pm.getApplicationInfo(packagesGraph,0).flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        long usageTimeSecGraph = (us2.getTotalTimeInForeground() / 1000);
                        upTimeSec1 = upTimeSec1 + usageTimeSecGraph;
                    }
                }catch (PackageManager.NameNotFoundException e){
                    e.printStackTrace();

                }


            }

            System.out.println("uptime: "+upTimeSec1/60/60);
            queryUsageStatsGraph.clear();



            x = i3;
            y = upTimeSec1/60/60;






            upTimeSec1 = 0;
            System.out.println(series);
            series.appendData(new DataPoint(x, y), true, cal2.get(Calendar.DAY_OF_MONTH));


        }
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(series);

        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public class AppAdapter extends BaseAdapter {

        public LayoutInflater layoutInflater;
        public List<MainActivity.AppList> listStorage;

        public AppAdapter(Context context, List<MainActivity.AppList> customizedListView) {
            layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listStorage = customizedListView;
        }

        @Override
        public int getCount() {
            return listStorage.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            MainActivity.AppAdapter.ViewHolder listViewHolder;
            if(convertView == null){
                listViewHolder = new MainActivity.AppAdapter.ViewHolder();
                convertView = layoutInflater.inflate(R.layout.installed_app_list, parent, false);

                listViewHolder.textInListView = (TextView)convertView.findViewById(R.id.list_app_name);
                listViewHolder.imageInListView = (ImageView)convertView.findViewById(R.id.app_icon);
                listViewHolder.packageInListView=(TextView)convertView.findViewById(R.id.app_package);
                convertView.setTag(listViewHolder);
            }else{
                listViewHolder = (MainActivity.AppAdapter.ViewHolder)convertView.getTag();
            }
            listViewHolder.textInListView.setText(listStorage.get(position).getName());
            listViewHolder.imageInListView.setImageDrawable(listStorage.get(position).getIcon());
            listViewHolder.packageInListView.setText(listStorage.get(position).getPackages());

            return convertView;
        }

        class ViewHolder{
            TextView textInListView;
            ImageView imageInListView;
            TextView packageInListView;
        }
    }

    public class AppList{
        private String name;
        Drawable icon;
        private String packages;
        public AppList(String name, Drawable icon, String packages) {
            this.name = name;
            this.icon = icon;
            this.packages = packages;
        }
        public String getName() {
            return name;
        }
        public Drawable getIcon() {
            return icon;
        }
        public String getPackages() {
            return packages;
        }




    }
}
