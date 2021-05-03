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

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DashboardActivity extends AppCompatActivity {





    private List<DashboardActivity.AppList> installedApps;
    private DashboardActivity.AppAdapter installedAppAdapter;
    ListView userInstalledApps;

    private static Context context;







    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        DashboardActivity.context = getApplicationContext();



        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);





        userInstalledApps = (ListView) findViewById(R.id.installed_app_list);

        installedApps = getInstalledApps();
        installedAppAdapter = new DashboardActivity.AppAdapter(DashboardActivity.this, installedApps);
        userInstalledApps.setAdapter(installedAppAdapter);
        userInstalledApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


                String pkg = installedApps.get(i).packages.substring(0, installedApps.get(i).packages.indexOf(" "));
                String[] colors = {" Open App", " App Info"};
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
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
                                        Toast.makeText(DashboardActivity.this, pkg/*installedApps.get(i).packages*/ + " Error, Please Try Again...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (which==1){
                                    //Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    //Intent intent = new Intent(Settings.ACTION_APP_USAGE_SETTINGS);
                                    Intent intent = new Intent(Settings.ACTION_APP_USAGE_SETTINGS);

                                    //intent.setData(Uri.parse("package:" + pkg /*installedApps.get(i).packages*/));
                                    //intent.putExtra("EXTRA_PACKAGE_NAME:", pkg);
                                    intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
                                    Toast.makeText(DashboardActivity.this, pkg /*installedApps.get(i).packages*/, Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                }
                            }
                        });
                builder.show();

            }
        });

        //Total Number of Installed-Apps(i.e. List Size)
        String  abc = userInstalledApps.getCount()+"";
        TextView countApps = (TextView)findViewById(R.id.countApps);
        countApps.setText("Total Installed Apps: "+abc);
        Toast.makeText(this, abc+" Apps", Toast.LENGTH_SHORT).show();

    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.navigation_home:
                            startActivity(new Intent(DashboardActivity.this, MainActivity.class));

                            break;
                         /*
                        case R.id.navigation_dashboard:
                            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                            break;

                          */
                        /*
                        case R.id.navigation_settings:
                            //startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                            //selectedFragment = new SettingsFragment();
                            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            //        selectedFragment).commit();
                            break;

                         */
                    }


                    return true;
                }
            };


    private List<DashboardActivity.AppList> getInstalledApps(){
        PackageManager pm = getPackageManager();

        ApplicationInfo ai;


        List<DashboardActivity.AppList> apps = new ArrayList<DashboardActivity.AppList>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);

        System.out.println("pack size: "+packs.size());


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(cal.get(Calendar.DAY_OF_MONTH)));
        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);

        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, cal.getTimeInMillis(), System.currentTimeMillis());


        for (UsageStats us : queryUsageStats) {
            System.out.println("names: "+ us.getPackageName() +"time: "+DateUtils.formatElapsedTime(us.getTotalTimeInForeground() / 1000));
            String appName = "";
            Drawable icon = null;

            String packages = us.getPackageName();




            try {
                if ((pm.getApplicationInfo(packages,0).flags & ApplicationInfo.FLAG_SYSTEM) != 1 || packages.equals("com.google.android.youtube") || packages.equals("com.facebook.katana") || packages.equals("com.android.chrometime")){
                    appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packages, PackageManager.GET_META_DATA));
                    icon = pm.getApplicationIcon(packages);
                    long usageTimeSec = (us.getTotalTimeInForeground() / 1000);
                    String usageTime = ""+DateUtils.formatElapsedTime(usageTimeSec);



                    for(int i = 0; i < apps.size(); i++){



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

                            apps.remove(i);
                            usageTimeSec = (usageTimeSec + usgTimeSec);
                            usageTime = ""+DateUtils.formatElapsedTime(usageTimeSec);

                        }
                    }
                    apps.add(new DashboardActivity.AppList(appName, icon, packages+" TimeUsed: "+usageTime));
                }

            }catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();

            }





        }
        Collections.sort(apps, new Comparator<DashboardActivity.AppList>() {
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
                System.out.println("usg 01 "+usgTimeSecO1);
                String usageTimeO2 =  o2.packages.substring(o2.packages.indexOf(" ")+11, o2.packages.length());
                String[] unitsO2 = usageTimeO2.split(":"); //will break the string up into an array


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

                return Long.compare(usgTimeSecO2, usgTimeSecO1);
            }
        });


        return apps;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public class AppAdapter extends BaseAdapter {

        public LayoutInflater layoutInflater;
        public List<DashboardActivity.AppList> listStorage;

        public AppAdapter(Context context, List<DashboardActivity.AppList> customizedListView) {
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

            DashboardActivity.AppAdapter.ViewHolder listViewHolder;
            if(convertView == null){
                listViewHolder = new DashboardActivity.AppAdapter.ViewHolder();
                convertView = layoutInflater.inflate(R.layout.installed_app_list, parent, false);

                listViewHolder.textInListView = (TextView)convertView.findViewById(R.id.list_app_name);
                listViewHolder.imageInListView = (ImageView)convertView.findViewById(R.id.app_icon);
                listViewHolder.packageInListView=(TextView)convertView.findViewById(R.id.app_package);
                convertView.setTag(listViewHolder);
            }else{
                listViewHolder = (DashboardActivity.AppAdapter.ViewHolder)convertView.getTag();
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
