package com.mrothberg.kakumei.utils;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.Gravity;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;

/** Keeps legacy activity content clear of system bars and the on-screen keyboard. */
public final class WindowInsetsSupport {
    private WindowInsetsSupport() {}

    public static void apply(Activity activity) {
        apply(activity, null);
    }

    public static void apply(Activity activity, Integer statusBarColor) {
        if (Build.VERSION.SDK_INT < 35) return;

        ViewGroup content = activity.findViewById(android.R.id.content);
        View root = content.getChildAt(0);
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        View statusBar = statusBarColor == null ? null : new View(activity);
        if (statusBar != null) {
            statusBar.setBackgroundColor(statusBarColor);
            statusBar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            content.addView(statusBar, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, Gravity.TOP));
            activity.getWindow().getInsetsController().setSystemBarsAppearance(
                    0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            // Older AppCompat can consume the status inset before it reaches content,
            // while still positioning that content underneath the ActionBar.
            WindowInsets windowInsets = view.getRootWindowInsets();
            if (windowInsets == null) windowInsets = insets;
            Insets safeArea = windowInsets.getInsets(WindowInsets.Type.systemBars()
                    | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
            view.setPadding(left + safeArea.left, top + safeArea.top,
                    right + safeArea.right, bottom + safeArea.bottom);
            if (statusBar != null) {
                ViewGroup.LayoutParams params = statusBar.getLayoutParams();
                params.height = windowInsets.getInsets(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.displayCutout()).top;
                statusBar.setLayoutParams(params);
            }
            return WindowInsets.CONSUMED;
        });
        root.requestApplyInsets();
    }
}
