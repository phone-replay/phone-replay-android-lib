package com.phonereplay.tasklogger;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserInteractionAwareCallback extends GestureDetector.SimpleOnGestureListener implements Callback {

    private static final long SWIPE_THRESHOLD = 100;

    private static final long SWIPE_VELOCITY_THRESHOLD = 100;

    private final Callback originalCallback;

    private final Activity activity;


    private final GestureDetector gestureDetector;


    public UserInteractionAwareCallback(final Callback originalCallback, final Activity activity) {
        this.originalCallback = originalCallback;
        this.activity = activity;
        this.gestureDetector = new GestureDetector(activity, this);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return originalCallback.dispatchTouchEvent(event);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        handleTouchAction("Single Tap", e.getX(), e.getY());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        assert e1 != null && e2 != null; // Assegura que e1 e e2 não são nulos
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) < Math.abs(diffY)) {
            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    handleTouchAction("Swipe Down", e2.getX(), e2.getY());
                } else {
                    handleTouchAction("Swipe Up", e2.getX(), e2.getY());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        //handleTouchAction("Trail", e1.getEventTime());
        return true;
    }

    private void handleTouchAction(String action, float x, float y) {
        PhoneReplayApi.registerTouchAction(action, x, y);
        if (activity != null) {
            activity.onUserInteraction();
        }
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
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        return originalCallback.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, @NonNull Menu menu) {
        return originalCallback.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, @NonNull Menu menu) {
        return originalCallback.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
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
    public void onPanelClosed(int featureId, @NonNull Menu menu) {
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

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Nullable
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
}