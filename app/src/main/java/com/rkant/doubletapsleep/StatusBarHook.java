package com.rkant.doubletapsleep;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class StatusBarHook implements IXposedHookLoadPackage {

    private static final String TAG = "DoubleTapSleep";
    private static final String SYSTEMUI = "com.android.systemui";
    private static final String STATUS_BAR_VIEW =
            "com.android.systemui.statusbar.phone.PhoneStatusBarView";
    private static final String FIELD_DETECTOR = "dtsGestureDetector";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!SYSTEMUI.equals(lpparam.packageName)) return;

        try {
            XposedHelpers.findAndHookMethod(
                    STATUS_BAR_VIEW, lpparam.classLoader,
                    "onTouchEvent", MotionEvent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                View view = (View) param.thisObject;
                                MotionEvent event = (MotionEvent) param.args[0];

                                GestureDetector detector = (GestureDetector)
                                        XposedHelpers.getAdditionalInstanceField(view, FIELD_DETECTOR);
                                if (detector == null) {
                                    detector = createDetector(view.getContext());
                                    XposedHelpers.setAdditionalInstanceField(view, FIELD_DETECTOR, detector);
                                }
                                detector.onTouchEvent(event);
                            } catch (Throwable t) {
                                XposedBridge.log(TAG + ": touch hook error: " + t);
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hooked " + STATUS_BAR_VIEW);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook: " + t);
        }
    }

    private GestureDetector createDetector(final Context context) {
        return new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                goToSleep(context);
                return true;
            }
        });
    }

    private void goToSleep(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            long now = SystemClock.uptimeMillis();
            try {
                XposedHelpers.callMethod(pm, "goToSleep", now, 4, 0);
            } catch (Throwable first) {
                XposedHelpers.callMethod(pm, "goToSleep", now);
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": goToSleep failed: " + t);
        }
    }
}
