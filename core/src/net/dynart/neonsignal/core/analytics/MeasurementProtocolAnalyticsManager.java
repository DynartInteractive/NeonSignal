package net.dynart.neonsignal.core.analytics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import net.dynart.lisa.core.Settings;
import net.dynart.lisa.core.User;
import net.dynart.lisa.core.analytics.AnalyticsManager;
import net.dynart.lisa.core.analytics.AnalyticsSettings;
import net.dynart.neonsignal.NeonSignalEngineConfig;
import net.dynart.neonsignal.VersionUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MeasurementProtocolAnalyticsManager implements AnalyticsManager {

    private static final String ENDPOINT = "https://www.google-analytics.com/mp/collect";
    private static final String GEO_URL = "https://ipapi.co/json/";
    private static final String LOG_TAG = "AnalyticsManager";

    private final String measurementId;
    private final String apiSecret;
    private final String clientId;
    private final long sessionId;
    private final String platform;
    private final Map<String, Integer> attemptCounts = new HashMap<>();
    private final boolean gaDebug;
    private boolean enabled;
    private long lastEventTime;

    private volatile String geoIp = null;
    private volatile String geoCountryCode = null;
    private volatile String geoRegionCode = null;
    private volatile String geoCity = null;

    public MeasurementProtocolAnalyticsManager(
        NeonSignalEngineConfig config, User user, Settings settings) {
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

        switch (Gdx.app.getType()) {
            case Android:
                platform = "android";
                break;
            case iOS:
                platform = "ios";
                break;
            case HeadlessDesktop:
                platform = "muos";
                break;
            default:
                platform = "desktop";
                break;
        }

        fetchGeoData();
    }

    private void fetchGeoData() {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder
            .newRequest()
            .method(Net.HttpMethods.GET)
            .url(GEO_URL)
            .build();
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    JsonValue json = new JsonReader().parse(httpResponse.getResultAsString());
                    geoIp = json.getString("ip", null);
                    geoCountryCode = json.getString("country_code", null);
                    geoRegionCode = json.getString("region_code", null);
                    geoCity = json.getString("city", null);
                    Gdx.app.log(
                        LOG_TAG, "Geo: ip=" + geoIp + " country=" + geoCountryCode
                            + " region=" + geoRegionCode + " city=" + geoCity
                    );
                } catch (Exception e) {
                    Gdx.app.log(LOG_TAG, "Failed to parse geo data");
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log(LOG_TAG, "Failed to fetch geo data");
            }

            @Override
            public void cancelled() {
                // no-op
            }
        });
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void track(String eventName, Map<String, Object> params) {
        // Bookkeeping for level attempts: reset on level_start, increment on
        // player_death.
        if ("level_start".equals(eventName)) {
            Object levelName = params.get("level_name");
            if (levelName != null) {
                attemptCounts.put(levelName.toString(), 0);
            }
        } else if ("player_death".equals(eventName)) {
            Object levelName = params.get("level_name");
            if (levelName != null) {
                String key = levelName.toString();
                int attempt = attemptCounts.containsKey(key) ? attemptCounts.get(key) + 1 : 1;
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

    protected void send(String eventName, Map<String, Object> params) {
        if (!enabled || measurementId.isEmpty() || apiSecret.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        params.put("session_id", sessionId);
        params.put("platform", platform);
        params.put("version", VersionUtil.getVersion());
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

    protected String buildBody(String eventName, Map<String, Object> params) {
        StringBuilder paramsJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!first)
                paramsJson.append(",");
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

    protected void sendHttp(String url, String body) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(url)
            .header("Content-Type", "application/json")
            .content(body)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                // fire and forget
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log(LOG_TAG, "Failed to send event");
            }

            @Override
            public void cancelled() {
                // no-op
            }
        });
    }
}
