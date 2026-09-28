package com.sxc.island;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IslandService extends Service {

    private static final int WC = 96;
    private static final int HC = 30;
    private static final int WE = 290;
    private static final int HE = 78;

    private WindowManager wm;
    private LinearLayout box;
    private LinearLayout island;
    private TextView label;
    private GradientDrawable bg;
    private BroadcastReceiver rec;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private Runnable autoHide;
    private boolean expanded = false;
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
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        buildIsland();
        registerListeners();
    }

    private Notification buildNotify() {
        NotificationChannel ch = new NotificationChannel("island", "灵动岛",
                NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        return new Notification.Builder(this, "island")
                .setContentTitle("灵动岛正在运行")
                .setContentText("轻点胶囊展开 充电/音量会自动弹出")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build();
    }

    private void buildIsland() {
        box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER);

        island = new LinearLayout(this);
        island.setOrientation(LinearLayout.HORIZONTAL);
        island.setGravity(Gravity.CENTER);

        bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(HC) / 2f);
        island.setBackground(bg);

        label = new TextView(this);
        label.setTextColor(Color.WHITE);
        label.setTextSize(13);
        label.setGravity(Gravity.CENTER);
        island.addView(label);

        island.setOnClickListener(v -> {
            if (expanded) {
                collapse();
            } else {
                expand("BAT " + battery + "%", 2600);
            }
        });

        box.addView(island, dp(WC), dp(HC));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = Math.max(0, (statusBarHeight() - dp(HC)) / 2);

        try {
            wm.addView(box, lp);
        } catch (Exception e) {
            stopSelf();
            return;
        }

        island.setAlpha(0f);
        island.setScaleX(0.3f);
        island.setScaleY(0.3f);
        island.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }

    private void registerListeners() {
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                String a = i.getAction();
                if (Intent.ACTION_BATTERY_CHANGED.equals(a)) {
                    battery = i.getIntExtra("level", 0);
                    int st = i.getIntExtra("status", 0);
                    boolean charging = (st == 2 || st == 5);
                    if (charging && !expanded) {
                        expand("CHG " + battery + "%", 3200);
                    }
                } else if ("android.media.VOLUME_CHANGED_ACTION".equals(a)) {
                    int v = i.getIntExtra("android.media.E
