package net.dynart.neonsignal;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] args) {
		// config
		Lwjgl3ApplicationConfiguration config = createConfig();
		DesktopArguments arguments = createArguments(args);
		setUpConfig(config, arguments);

		// app
		NeonSignal app = new NeonSignal("desktop", arguments.getDebug(), arguments.getLevel());
		new Lwjgl3Application(app, config);
	}

	private static Lwjgl3ApplicationConfiguration createConfig() {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true);
		return config;
	}

	private static DesktopArguments createArguments(String[] args) {
		DesktopArguments arguments = new DesktopArguments();
		for (String arg : args) {
			arguments.process(arg);
		}
		return arguments;
	}

	private static void setUpConfig(Lwjgl3ApplicationConfiguration config, DesktopArguments arguments) {
		Graphics.Monitor monitor = getMonitor(arguments);
		Graphics.DisplayMode dm = Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);

		if (arguments.isFullscreen()) {
			config.setFullscreenMode(dm);
			config.setResizable(false);
		} else {
			int width = arguments.getWidth() > 0 ? arguments.getWidth() : dm.width;
			int height = arguments.getHeight() > 0 ? arguments.getHeight() : dm.height;
			config.setWindowedMode(width, height);
			config.setWindowPosition(monitor.virtualX + (dm.width - width) / 2, monitor.virtualY + (dm.height - height) / 2);
			config.setDecorated(!arguments.isBorderless());
		}
		if (arguments.getFps() != 0) {
			config.setForegroundFPS(arguments.getFps());
		}

		config.useVsync(arguments.isVsync());
	}

	private static Graphics.Monitor getMonitor(DesktopArguments arguments) {
		Graphics.Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
		Graphics.Monitor monitor = null;
		if (arguments.getDisplay() == -1) {
			monitor = Lwjgl3ApplicationConfiguration.getPrimaryMonitor();
		} else if (arguments.getDisplay() < monitors.length) {
			monitor = monitors[arguments.getDisplay()];
		} else if (arguments.getDisplay() >= monitors.length) {
			throw new RuntimeException("Display not exists: " + arguments.getDisplay());
		}
		return monitor;
	}
}
