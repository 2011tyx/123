package com.sxc.island;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

public class IslandService extends Service {

    private IslandUI ui;
    private BroadcastReceiver rec;
    private int battery = 0;

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1001, buildNotify());
        ui = new IslandUI(this);
        ui.show();
        registerListeners();
    }

    private Notification buildNotify() {
        NotificationChannel ch = new NotificationChannel("island", "灵动岛",
                NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        return new Notification.Builder(this, "island")
                .setContentTitle("灵动岛正在运行")
                .setContentText("轻点胶囊展开 充电/音量自动弹出")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build();
    }

    private void registerListeners() {
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                String a = i.getAction();
                if (Intent.ACTION_BATTERY_CHANGED.equals(a)) {
                    battery = i.getIntExtra("level", 0);
                    int st = i.getIntExtra("status", 0);
                    if ((st == 2 || st == 5) && !ui.isExpanded()) {
                        ui.expand("CHG " + battery + "%", 3200);
                    }
                } else if ("android.media.VOLUME_CHANGED_ACTION".equals(a)) {
                    int v = i.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1);
                    int m = i.getIntExtra("android.media.EXTRA_VOLUME_STREAM_MAX", -1);
                    if (v >= 0 && m > 0) {
                        ui.expand("VOL " + (v * 100 / m) + "%", 1800);
                    }
                }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_BATTERY_CHANGED);
        f.addAction("android.media.VOLUME_CHANGED_ACTION");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(rec, f, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(rec, f);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (rec != null) {
                unregisterReceiver(rec);
            }
        } catch (Exception e) {
        }
        if (ui != null) {
            ui.hide();
        }
        super.onDestroy();
    }
}
