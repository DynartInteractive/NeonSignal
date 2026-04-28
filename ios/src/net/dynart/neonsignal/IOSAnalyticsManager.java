package net.dynart.neonsignal;

import com.badlogic.gdx.Gdx;

import net.dynart.lisa.core.Settings;
import net.dynart.lisa.core.User;
import net.dynart.lisa.core.analytics.AnalyticsManager;
import net.dynart.lisa.core.analytics.AnalyticsSettings;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSMutableURLRequest;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSStringEncoding;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLSession;
import org.robovm.apple.foundation.NSURLSessionDataTask;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IOSAnalyticsManager implements AnalyticsManager {

    private static final String ENDPOINT = "https://www.google-analytics.com/mp/collect";
    private static final String GEO_URL = "https://ipapi.co/json/";
    private static final String LOG_TAG = "AnalyticsManager";

    private final String measurementId;
    private final String apiSecret;
    private final String clientId;
    private final long sessionId;
    private final Map<String, Integer> attemptCounts = new HashMap<>();
    private final boolean gaDebug;
    private boolean enabled;
    private long lastEventTime;

    private volatile String geoIp = null;
    private volatile String geoCountryCode = null;
    private volatile String geoRegionCode = null;
    private volatile String geoCity = null;

    public IOSAnalyticsManager(NeonSignalEngineConfig config, User user, Settings settings) {
        this.enabled = AnalyticsSettings.isAnalyticsEnabled(settings);
        this.measurementId = config.getAnalyticsMeasurementId();
        this.apiSecret = config.getAnalyticsApiSecret();
        this.gaDebug = config.isGaDebug();
        this.sessionId = System.currentTimeMillis();
        this.lastEventTime = sessionId;

        String id = user.getPreferences().getString("analytics_client_id", "");
        if (id.isEmpty()) {
            id = java.util.UUID.randomUUID().toString();
            user.getPreferences().putString("analytics_client_id", id).flush();
        }
        this.clientId = id;

        fetchGeoData();
    }

    private void fetchGeoData() {
        NSURL url = new NSURL(GEO_URL);
        NSMutableURLRequest request = new NSMutableURLRequest(url);
        request.setHTTPMethod("GET");

        NSURLSession session = NSURLSession.getSharedSession();
        NSURLSessionDataTask task = session.newDataTask(request, (data, response, error) -> {
            if (error != null || data == null) {
                Gdx.app.log(LOG_TAG, "Failed to fetch geo data");
                return;
            }
            try {
                String json = data.toString();
                // Simple JSON parsing without java.nio or external libs
                geoIp = extractJsonString(json, "ip");
                geoCountryCode = extractJsonString(json, "country_code");
                geoRegionCode = extractJsonString(json, "region_code");
                geoCity = extractJsonString(json, "city");
                Gdx.app.log(
                    LOG_TAG, "Geo: ip=" + geoIp + " country=" + geoCountryCode
                        + " region=" + geoRegionCode + " city=" + geoCity
                );
            } catch (Exception e) {
                Gdx.app.log(LOG_TAG, "Failed to parse geo data");
            }
        });
        task.resume();
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx + search.length());
        if (colonIdx < 0) return null;
        int startQuote = json.indexOf("\"", colonIdx + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void track(String eventName, Map<String, Object> params) {
        // Bookkeeping for level attempts: reset on level_start, increment on player_death.
        if ("level_start".equals(eventName)) {
            Object levelName = params.get("level_name");
            if (levelName != null) {
                attemptCounts.put(levelName.toString(), 0);
            }
        } else if ("player_death".equals(eventName)) {
            Object levelName = params.get("level_name");
            if (levelName != null) {
                String key = levelName.toString();
                int attempt =
                    attemptCounts.containsKey(key) ? attemptCounts.get(key) + 1 : 1;
                attemptCounts.put(key, attempt);
                params.put("attempt_number", attempt);
            }
        }
        Gdx.app.log(LOG_TAG, eventName + ": " + params);
        send(eventName, params);
    }

    @Override
    public void dispose() {
        // no-op
    }

    private void send(String eventName, Map<String, Object> params) {
        if (!enabled || measurementId.isEmpty() || apiSecret.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        params.put("session_id", sessionId);
        params.put("platform", "ios");
        params.put("version", net.dynart.neonsignal.VersionUtil.getVersion());
        params.put("engagement_time_msec", now - lastEventTime);
        params.put("locale", Locale.getDefault().toString());
        if (gaDebug) {
            params.put("debug_mode", 1);
        }
        lastEventTime = now;

        String body = buildBody(eventName, params);
        String url = ENDPOINT + "?measurement_id=" + measurementId + "&api_secret=" + apiSecret;

        if (gaDebug) {
            Gdx.app.log(LOG_TAG, body);
        }

        sendHttp(url, body);
    }

    private String buildBody(String eventName, Map<String, Object> params) {
        StringBuilder paramsJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!first) paramsJson.append(",");
            paramsJson.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number) {
                paramsJson.append(value);
            } else {
                paramsJson.append("\"").append(value).append("\"");
            }
            first = false;
        }
        paramsJson.append("}");

        StringBuilder bodyBuilder = new StringBuilder("{\"client_id\":\"").append(clientId)
            .append("\"");
        if (geoCountryCode != null) {
            bodyBuilder.append(",\"user_location\":{");
            if (geoCity != null)
                bodyBuilder.append("\"city\":\"").append(geoCity).append("\",");
            bodyBuilder.append("\"country_id\":\"").append(geoCountryCode).append("\"");
            if (geoRegionCode != null) {
                bodyBuilder.append(",\"region_id\":\"").append(geoCountryCode).append("-")
                    .append(geoRegionCode).append("\"");
            }
            bodyBuilder.append("}");
        }
        if (geoIp != null) {
            bodyBuilder.append(",\"ip_override\":\"").append(geoIp).append("\"");
        }
        bodyBuilder.append(",\"events\":[{\"name\":\"").append(eventName).append("\",")
            .append("\"params\":").append(paramsJson).append("}]}");
        return bodyBuilder.toString();
    }

    private void sendHttp(String url, String body) {
        NSURL nsUrl = new NSURL(url);
        NSMutableURLRequest request = new NSMutableURLRequest(nsUrl);
        request.setHTTPMethod("POST");
        request.setHTTPHeaderField("Content-Type", "application/json");
        NSData bodyData = new NSString(body).toData(NSStringEncoding.UTF8);
        request.setHTTPBody(bodyData);

        NSURLSession session = NSURLSession.getSharedSession();
        NSURLSessionDataTask task = session.newDataTask(request, (data, response, error) -> {
            if (error != null) {
                Gdx.app.log(LOG_TAG, "Failed to send event");
            }
        });
        task.resume();
    }
}
