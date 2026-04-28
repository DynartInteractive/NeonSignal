package net.dynart.neonsignal;

import java.util.HashMap;
import java.util.Map;

import net.dynart.lisa.core.Engine;
import net.dynart.lisa.core.EngineConfig;
import net.dynart.lisa.core.Screen;
import net.dynart.neonsignal.core.analytics.AnalyticsManager;
import net.dynart.neonsignal.core.analytics.AnalyticsManagerFactory;
import net.dynart.neonsignal.core.analytics.NoOpAnalyticsManager;

public class NeonSignalEngine extends Engine {

    private final Map<Screen, String> screenNames = new HashMap<>();
    private AnalyticsManager analyticsManager = new NoOpAnalyticsManager();
    private AnalyticsManagerFactory analyticsManagerFactory;

    public NeonSignalEngine(EngineConfig engineConfig, boolean debug) {
        super(engineConfig, debug);
    }

    public AnalyticsManager getAnalyticsManager() {
        return analyticsManager;
    }

    public void setAnalyticsManagerFactory(AnalyticsManagerFactory factory) {
        this.analyticsManagerFactory = factory;
    }

    @Override
    public void addScreen(String name, Screen screen) {
        super.addScreen(name, screen);
        screenNames.put(screen, name);
    }

    @Override
    public void loadingFinished() {
        super.loadingFinished();
        if (analyticsManagerFactory != null) {
            analyticsManager = analyticsManagerFactory.create(
                (NeonSignalEngineConfig) getConfig(), getUser(), getSettings()
            );
        }
    }

    @Override
    public void changeToNextScreen() {
        Screen previous = getCurrentScreen();
        super.changeToNextScreen();
        Screen current = getCurrentScreen();
        if (current != null && current != previous) {
            String name = screenNames.get(current);
            if (name != null) {
                analyticsManager.trackScreen(name);
            }
        }
    }
}
