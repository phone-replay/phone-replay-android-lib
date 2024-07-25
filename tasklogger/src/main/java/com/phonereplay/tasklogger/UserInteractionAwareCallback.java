package com.phonereplay.tasklogger;

import android.app.Activity;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window.Callback;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

public class UserInteractionAwareCallback extends GestureDetector.SimpleOnGestureListener implements Callback {

    private static final float MIN_DISTANCE = 10.0f;
    private final Callback originalCallback;
    private final GestureDetector gestureDetector;
    private final List<float[]> currentGestureCoordinates;
    private final Activity activity;
    private Gesture currentGesture;
    private boolean isTrackingGesture;

    public UserInteractionAwareCallback(final Callback originalCallback, final Activity activity) {
        this.originalCallback = originalCallback;
        this.gestureDetector = new GestureDetector(activity, this);
        this.currentGestureCoordinates = new ArrayList<>();
        this.isTrackingGesture = false;
        this.activity = activity;
        String activityName = activity.getClass().getSimpleName();
        Log.d("UICallback", "Initialized for activity: " + activityName);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("ActionRegistered", "Gesture: BOTAO PRESSIONADO");
                currentGestureCoordinates.clear();
                currentGesture = new Gesture();
                isTrackingGesture = true;
                break;
            case MotionEvent.ACTION_UP:
                Log.d("ActionRegistered", "Gesture: BOTAO DESPRESSIONADO");
                if (isTrackingGesture) {
                    isTrackingGesture = false;
                    if (currentGesture != null) {
                        registerGesture(currentGesture);
                        currentGesture = null;
                    }
                }
                break;
        }
        return originalCallback.dispatchTouchEvent(event);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        extracted(e);
        return true;
    }

    private void extracted(MotionEvent e) {
        currentGestureCoordinates.add(new float[]{e.getX(), e.getY()});
        Log.d("ActionRegistered", "Gesture: " + e.getX() + " " + e.getY());
        PhoneReplayApi.registerTouchAction("scroll", e.getX(), e.getY(), currentGesture);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isTrackingGesture && e2 != null) {
            if (currentGestureCoordinates.isEmpty() || distance(e2.getX(), e2.getY(), currentGestureCoordinates.get(currentGestureCoordinates.size() - 1)) > MIN_DISTANCE) {
                extracted(e2);
            }
        }
        return true;
    }

    private float distance(float x1, float y1, float[] lastPoint) {
        return (float) Math.sqrt(Math.pow(x1 - lastPoint[0], 2) + Math.pow(y1 - lastPoint[1], 2));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return originalCallback.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return originalCallback.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return originalCallback.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return originalCallback.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return originalCallback.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        return originalCallback.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return originalCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return originalCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return originalCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return originalCallback.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        originalCallback.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        originalCallback.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        originalCallback.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        originalCallback.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        originalCallback.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        originalCallback.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return originalCallback.onSearchRequested();
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return false;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
    }

    private void registerGesture(Gesture gesture) {
        String activityName = activity.getClass().getSimpleName();
        Log.d("GestureRegistered", "Registering gesture for activity: " + activityName);
        PhoneReplayApi.addActivityGesture(activityName, gesture);
    }
}
