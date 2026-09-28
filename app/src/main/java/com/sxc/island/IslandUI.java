package com.sxc.island;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IslandUI {

    private static final float REF = 440f, RW = 126f, RH = 37.3f, EW = 371f;

    private final Context ctx;
    private final WindowManager wm;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private LinearLayout island;
    private TextView label;
    private GradientDrawable bg;
    private Runnable autoHide;
    private boolean expanded;
    private int cw, ch, ew;
    private float k = 1f;

    public IslandUI(Context c) {
        ctx = c;
        wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        size();
    }

    public boolean isExpanded() {
        return expanded;
    }

    private void size() {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        k = Math.min(dm.widthPixels, dm.heightPixels) / REF;
        cw = Math.round(RW * k);
        ch = Math.round(RH * k);
        ew = Math.round(EW * k);
    }

    private int topY() {
        int id = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int sb = id > 0 ? ctx.getResources().getDimensionPixelSize(id) : ch * 2;
        return Math.max(0, (sb - ch) / 2);
    }

    public void show() {
        island = new LinearLayout(ctx);
        island.setOrientation(LinearLayout.HORIZONTAL);
        island.setGravity(Gravity.CENTER);

        bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(ch / 2f);
        island.setBackground(bg);

        label = new TextView(ctx);
        label.setTextColor(Color.WHITE);
        label.setTextSize(13f * k);
        label.setGravity(Gravity.CENTER);
        island.addView(label);

        island.setOnClickListener(v -> {
            if (expanded) {
                collapse();
            } else {
                String t = new java.text.SimpleDateFormat("HH:mm",
                        java.util.Locale.getDefault()).format(new java.util.Date());
                expand(t, 2200);
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.width = cw;
        lp.height = ch;
        lp.y = topY();
        wm.addView(island, lp);

        island.setAlpha(0f);
        island.animate().alpha(1f).setDuration(500).start();
    }

    public void hide() {
        ui.removeCallbacksAndMessages(null);
        try {
            wm.removeView(island);
        } catch (Exception e) {
        }
        island = null;
    }

    public void expand(String text, long ms) {
        if (island == null) {
            return;
        }
        expanded = true;
        label.setText(text);
        resize(ew, ch);
        if (autoHide != null) {
            ui.removeCallbacks(autoHide);
        }
        autoHide = this::collapse;
        ui.postDelayed(autoHide, ms);
    }

    private void collapse() {
        if (island == null) {
            return;
        }
        expanded = false;
        label.setText("");
        resize(cw, ch);
    }

    private void resize(int tw, int th) {
        WindowManager.LayoutParams p0 = (WindowManager.LayoutParams) island.getLayoutParams();
        final int w0 = p0.width;
        final int h0 = p0.height;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(430);
        a.setInterpolator(new OvershootInterpolator(1.4f));
        a.addUpdateListener(an -> {
            float f = an.getAnimatedFraction();
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) island.getLayoutParams();
            p.width = Math.max(1, (int) (w0 + (tw - w0) * f));
            p.height = Math.max(1, (int) (h0 + (th - h0) * f));
            bg.setCornerRadius(p.height / 2f);
            wm.updateViewLayout(island, p);
        });
        a.start();
    }
}
