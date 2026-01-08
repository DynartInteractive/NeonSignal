package net.dynart.neonsignal;

import android.content.Context;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;

import barsoosayque.libgdxoboe.OboeAudio;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		config.useAccelerometer = false;
		config.useCompass = false;

		NeonSignal app = new NeonSignal("android", false, null);

		initialize(app, config);
	}

	// Magic happens here:
	@Override
	public AndroidAudio createAudio(Context context, AndroidApplicationConfiguration config) {
		return new OboeAudio(context.getAssets());
	}
}
