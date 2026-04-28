package net.dynart.neonsignal;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.dynart.lisa.core.Settings;
import net.dynart.lisa.core.analytics.AnalyticsManager;
import net.dynart.lisa.core.analytics.AnalyticsSettings;

import java.util.Map;

public class FirebaseAnalyticsManager implements AnalyticsManager {

    private final FirebaseAnalytics firebaseAnalytics;
    private boolean enabled;

    public FirebaseAnalyticsManager(Context context, Settings settings) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.enabled = AnalyticsSettings.isAnalyticsEnabled(settings);
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
        firebaseAnalytics.setAnalyticsCollectionEnabled(value);
    }

    @Override
    public void track(String eventName, Map<String, Object> params) {
        if (!enabled) return;
        Bundle bundle = new Bundle();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof Integer) {
                    bundle.putInt(entry.getKey(), (Integer) value);
                } else if (value instanceof Long) {
                    bundle.putLong(entry.getKey(), (Long) value);
                } else if (value instanceof Float) {
                    bundle.putFloat(entry.getKey(), (Float) value);
                } else if (value instanceof Double) {
                    bundle.putDouble(entry.getKey(), (Double) value);
                } else if (value instanceof Boolean) {
                    bundle.putBoolean(entry.getKey(), (Boolean) value);
                } else {
                    bundle.putString(entry.getKey(), value.toString());
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    @Override
    public void dispose() {
        // no-op
    }
}
