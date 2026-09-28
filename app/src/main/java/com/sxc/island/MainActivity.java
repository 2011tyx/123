package com.sxc.island;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView tip;
    private Button start;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0E1116"));
        root.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("灵动岛");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        tip = new TextView(this);
        tip.setTextSize(15);
        tip.setGravity(Gravity.CENTER);
        tip.setTextColor(Color.parseColor("#9AA6B2"));
        tip.setPadding(0, 50, 0, 70);
        root.addView(tip);

        start = new Button(this);
        start.setOnClickListener(v -> onStart());
        root.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Button stop = new Button(this);
        stop.setText("停止灵动岛");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, IslandService.class));
            refresh();
        });
        root.addView(stop, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (Settings.canDrawOverlays(this)) {
            tip.setText("权限已就绪，点下面按钮启用灵动岛");
            start.setText("启动灵动岛");
        } else {
            tip.setText("还差一步：需要显示在其他应用上层权限");
            start.setText("去开启悬浮窗权限");
        }
    }

    private void onStart() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
            return;
        }
        tip.setText("已启动！抬头看屏幕最上面");
        startForegroundService(new Intent(this, IslandService.class));
    }
}
