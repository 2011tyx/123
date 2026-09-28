package com.sxc.island;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IslandUI {

    private static final int WC = 96, HC = 30, WE = 290, HE = 78;

    private final Context ctx;
    private final WindowManager wm;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private LinearLayout island;
    private TextView label;
    private GradientDrawable bg;
    private Runnable autoHide;
    private boolean expanded = false;

    public IslandUI(Context c) {
        ctx = c;
        wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void show() {
        island = new LinearLayout(ctx);
        island.setOrientation(LinearLayout.HORIZONTAL);
        island.setGravity(Gravity.CENTER);

        bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#000000"));
        bg.setCornerRadius(dp(HC) / 2f);
        island.setBackground(bg);

        label = new TextView(ctx);
        label.setTextColor(Color.WHITE);
        label.setTextSize(13);
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
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = dp(4);
        wm.addView(island, lp);

        island.setAlpha(0f);
        island.setScaleX(0.3f);
        island.setScaleY(0.3f);
        island.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }

    public void hide() {
        ui.removeCallbacksAndMessages(null);
        try {
            if (island != null) {
                wm.removeView(island);
            }
        } catch (Exception e) {
        }
        island = null;
    }

    public void expand(String text, long autoHideMs) {
        if (island == null) {
            return;
        }
        expanded = true;
        label.setText(text);
        resize(dp(WE), dp(HE));
        if (autoHide != null) {
            ui.removeCallbacks(autoHide);
        }
        autoHide = this::collapse;
        ui.postDelayed(autoHide, autoHideMs);
    }

    private void collapse() {
        if (island == null) {
            return;
        }
        expanded = false;
        label.setText("");
        resize(dp(WC), dp(HC));
    }

    private void resize(final int tw, final int th) {
        final int w0 = Math.max(1, island.getWidth());
        final int h0 = Math.max(1, island.getHeight());
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(430);
        a.setInterpolator(new OvershootInterpolator(1.4f));
        a.addUpdateListener(an -> {
            float f = an.getAnimatedFraction();
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) island.getLayoutParams();
            p.width = Math.max(1, (int) (w0 + (tw - w0) * f));
            p.height = Math.max(1, (int) (h0 + (th - h0) * f));
            island.setLayoutParams(p);
            bg.setCornerRadius(p.height / 2f);
        });
        a.start();
    }

    private int dp(int v) {
        return (int) (v * ctx.getResources().getDisplayMetrics().density + 0.5f);
    }
          }
