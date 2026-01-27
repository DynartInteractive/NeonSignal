package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SoundManager {

    /*
    THIS LOOKS LIKE RESOLVED BY libgdx-oboe

    static class AudioThreadItem {
        Sound sound;
        float volume;
    }

    // This is a workaround of the stutter/lag when we call sound.play() on Android
    static class AudioThread extends Thread {

        // ConcurrentLinkedQueue: "is an appropriate choice when many threads will share access to a common collection"
        private Queue<AudioThreadItem> queue = new ConcurrentLinkedQueue<>();

        // volatile because we modify this from the main thread as well
        private volatile boolean running = true;

        public void run() {
            try {
                while (running) {
                    for (AudioThreadItem item = queue.poll(); item != null; item = queue.poll()) {
                        item.sound.play(item.volume);
                    }
                    Thread.sleep(16); // We don't want exhaust the CPU
                }
            } catch (InterruptedException ignored) {}
        }

        public void play(Sound sound, float volume) { // called from main thread
            AudioThreadItem item = new AudioThreadItem();
            item.sound = sound;
            item.volume = volume;
            queue.add(item);
        }

        public void exit() { // called from main thread
            try {
                running = false;
                join();
            } catch (InterruptedException ignored) {}
        }
    }
    */

    static class Channel {
        private Sound sound;
        private Long instance;
    }

    private final Engine engine;
    private final Map<String, String> soundPaths = new HashMap<String, String>();
    private final Map<String, String> musicPaths = new HashMap<String, String>();
    private final Map<String, Sound> sounds = new HashMap<String, Sound>();
    private float volume = 1.0f;
    private float musicVolume = 0.8f;
    private Music music;
    private String lastMusicPath;
    private final AssetManager assetManager;
    private final Map<String, Float> dnpTime = new HashMap<>(); // dnpTime = do not play before time
    private final Map<String[], Float> dnpaTime = new HashMap<>(); // dnpTime = do not play before time array (for random sound)
    private final Channel[] channels = new Channel[4];
    private int currentChannel;
    private final Map<Long, Sound> instanceToSound = new HashMap<>();
    //private final AudioThread audioThread;

    public SoundManager(Engine engine) {
        this.engine = engine;
        Settings settings = engine.getSettings();
        assetManager = engine.getAssetManager();
        setVolume(settings.getSoundVolume());
        setMusicVolume(settings.getMusicVolume());
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new Channel();
        }
        /*
        audioThread = new AudioThread();
        audioThread.start();
        */
    }

    public void load(JsonValue resourcesJson) {
        JsonValue sounds = resourcesJson.get("sounds");
        for (JsonValue sound = sounds.child(); sound != null; sound = sound.next()) {
            String soundPath = sound.getString("path");
            assetManager.load(soundPath, Sound.class);
            add(sound.name(), soundPath);
        }
        JsonValue allMusic = resourcesJson.get("music");
        for (JsonValue music = allMusic.child(); music != null; music = music.next()) {
            musicPaths.put(music.name(), music.getString("path"));
        }
    }

    public void add(String name, String soundPath) {
        soundPaths.put(name, soundPath);
        dnpTime.put(name, -1f);
    }

    public Long play(String name) {
        return play(name, 1.0f);
    }

    public Long play(String name, float volume) {
        return play(name, volume, 0.02f);
    }

    public Long play(String name, float volume, float dnpDuration) {
        if (!soundPaths.containsKey(name)) {
            return 0L;
            //throw new RuntimeException("Sound not found: " + name);
        }
        if (!sounds.containsKey(name)) {
            sounds.put(name, assetManager.get(soundPaths.get(name), Sound.class));
        }
        if (dnpTime.get(name) < engine.getElapsedTime()) {
            Sound sound = sounds.get(name);
            if (channels[currentChannel].sound != null) {
                channels[currentChannel].sound.stop(channels[currentChannel].instance);
            }
            //audioThread.play(sound, this.volume * volume);
            channels[currentChannel].sound = sound;
            channels[currentChannel].instance = sound.play(this.volume * volume);
            instanceToSound.put(channels[currentChannel].instance, sound);
            dnpTime.put(name, engine.getElapsedTime() + dnpDuration);
            currentChannel++;
            if (currentChannel == channels.length) {
                currentChannel = 0;
            }
            return channels[currentChannel == 0 ? channels.length - 1 : currentChannel - 1].instance;
        }
        return null;
    }

    public void stop(Long id) {
        if (id != null && instanceToSound.containsKey(id)) {
            instanceToSound.get(id).stop(id);
            instanceToSound.remove(id);
        }
    }

    public void setVolume(Long id, float volume) {
        if (id != null && instanceToSound.containsKey(id)) {
            instanceToSound.get(id).setVolume(id, this.volume * volume);
        }
    }

    public void update() {
    }

    public void setVolume(float value) {
        volume = value * value; // exponential sounds natural
    }


    public void setMusicVolume(float value) {
        musicVolume = value * value; // exponential sounds natural
        if (music != null) {
            music.setVolume(musicVolume);
        }
    }

    public void playMusic(String name) {
        if (!musicPaths.containsKey(name)) {
            throw new RuntimeException("Music not exists: " + name);
        }
        String path = musicPaths.get(name);
        if (path.equals(lastMusicPath)) {
            return;
        }
        lastMusicPath = path;
        if (music != null) {
            music.dispose();
        }
        music = Gdx.audio.newMusic(Gdx.files.internal(path));
        music.setVolume(musicVolume);
        music.setLooping(true);
        music.play();
    }

    public void pause() {
        if (music != null) {
            music.pause();
        }
        for (Sound sound : sounds.values()) {
            sound.stop();
        }
    }

    public void resume() {
        if (music != null) {
            music.play();
        }
    }

    public void dispose() {
        if (music != null) {
            music.dispose();
        }
        //audioThread.exit();
    }

    public void playRandom(String[] all) {
        playRandom(all, 1.0f);
    }

    // choose a sound randomly from all except the last one,
    // play it, then swap it with the last one
    // .. so we will not repeat the same sound
    public void playRandom(String[] all, float volume) {
        if (!dnpaTime.containsKey(all)) {
            dnpaTime.put(all, engine.getElapsedTime() - 0.02f);
        }
        if (dnpaTime.get(all) < engine.getElapsedTime()) {
            dnpaTime.put(all, engine.getElapsedTime() + 0.02f);
            play(getRandom(all), volume);
        }
    }

    public String getRandom(String[] all) {
        int lastIndex = all.length - 1;
        int index = (int)(Math.random() * lastIndex);
        String tmp = all[index];
        all[index] = all[lastIndex];
        all[lastIndex] = tmp;
        return tmp;
    }

    public Sound get(String name) {
        return sounds.get(name);
    }

}
