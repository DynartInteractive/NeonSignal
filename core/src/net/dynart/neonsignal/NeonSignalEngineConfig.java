package net.dynart.neonsignal;

import net.dynart.lisa.core.EngineConfig;

public class NeonSignalEngineConfig extends EngineConfig {

    @SuppressWarnings("unused") // populated via reflection from config.json
    public String analyticsMeasurementId = "";

    @SuppressWarnings("unused") // populated via reflection from config.json
    public String analyticsApiSecret = "";

    private boolean gaDebug;

    public NeonSignalEngineConfig() {
        setVersion(VersionUtil.getVersion());
    }

    public String getAnalyticsMeasurementId() {
        return analyticsMeasurementId;
    }

    public String getAnalyticsApiSecret() {
        return analyticsApiSecret;
    }

    public void setGaDebug(boolean value) {
        gaDebug = value;
    }

    public boolean isGaDebug() {
        return gaDebug;
    }
}
