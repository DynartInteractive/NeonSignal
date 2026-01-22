package net.dynart.neonsignal.core.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import net.dynart.neonsignal.core.SoundManager;

/**
 * Scene2D Action that reveals text character-by-character with typewriter effect.
 * Handles LibGDX color markup tags by skipping over [...] during reveal.
 */
public class TypewriterAction extends Action {

    private final Label label;
    private final String fullText;
    private final float charDelay;
    private final Runnable onComplete;
    private final SoundManager soundManager;
    private final String soundName;

    private float elapsed = 0;
    private int visibleCharCount = 0;
    private int totalVisibleChars;
    private boolean finished = false;
    private boolean skipped = false;
    private boolean started = false;
    private Long soundId;
    private boolean fadingOut = false;
    private float fadeVolume = 1f;
    private static final float FADE_SPEED = 10f; // fade out in ~0.1 seconds

    public TypewriterAction(Label label, String fullText, float charDelay, Runnable onComplete) {
        this(label, fullText, charDelay, onComplete, null, null);
    }

    public TypewriterAction(Label label, String fullText, float charDelay, Runnable onComplete, SoundManager soundManager, String soundName) {
        this.label = label;
        this.fullText = fullText;
        this.charDelay = charDelay;
        this.onComplete = onComplete;
        this.soundManager = soundManager;
        this.soundName = soundName;
        this.totalVisibleChars = countVisibleChars(fullText);
    }

    @Override
    public boolean act(float delta) {
        // Handle sound fade-out
        if (fadingOut && soundId != null) {
            fadeVolume -= delta * FADE_SPEED;
            if (fadeVolume <= 0) {
                fadeVolume = 0;
                if (soundManager != null) {
                    soundManager.stop(soundId);
                }
                soundId = null;
                fadingOut = false;
            } else if (soundManager != null) {
                soundManager.setVolume(soundId, fadeVolume);
            }
        }

        if (finished) {
            return !fadingOut; // Wait for fade to complete
        }

        if (skipped) {
            label.setText(fullText);
            finish();
            return !fadingOut;
        }

        // Start sound on first act
        if (!started) {
            started = true;
            startSound();
        }

        elapsed += delta;
        int targetChars = (int) (elapsed / charDelay);

        if (targetChars > visibleCharCount) {
            visibleCharCount = Math.min(targetChars, totalVisibleChars);
            label.setText(getTextUpToVisibleChar(visibleCharCount));
        }

        if (visibleCharCount >= totalVisibleChars) {
            label.setText(fullText);
            finish();
            return !fadingOut;
        }

        return false;
    }

    private void startSound() {
        if (soundManager != null && soundName != null) {
            soundId = soundManager.play(soundName, 1f, 0f);
        }
    }

    private void stopSound() {
        if (soundId != null) {
            fadingOut = true;
        }
    }

    /**
     * Skip the typewriter animation and show full text immediately.
     */
    public void skip() {
        skipped = true;
        // Stop sound immediately on skip (no fade)
        if (soundManager != null && soundId != null) {
            soundManager.stop(soundId);
            soundId = null;
        }
        fadingOut = false;
    }

    /**
     * Check if the animation has finished.
     */
    public boolean isFinished() {
        return finished;
    }

    private void finish() {
        finished = true;
        stopSound();
        if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Count visible characters in text, excluding markup tags like [color] and [/].
     */
    private int countVisibleChars(String text) {
        int count = 0;
        boolean inTag = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                // Check if this is an escaped bracket [[
                if (i + 1 < text.length() && text.charAt(i + 1) == '[') {
                    count++; // [[ counts as one visible [
                    i++; // skip next [
                } else {
                    inTag = true;
                }
            } else if (c == ']' && inTag) {
                inTag = false;
            } else if (!inTag) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get substring of text up to the given visible character count,
     * preserving complete markup tags.
     */
    private String getTextUpToVisibleChar(int targetVisibleChars) {
        StringBuilder result = new StringBuilder();
        int visibleCount = 0;
        boolean inTag = false;
        int tagStart = -1;

        for (int i = 0; i < fullText.length() && visibleCount < targetVisibleChars; i++) {
            char c = fullText.charAt(i);

            if (c == '[') {
                // Check for escaped bracket [[
                if (i + 1 < fullText.length() && fullText.charAt(i + 1) == '[') {
                    result.append('[');
                    visibleCount++;
                    i++; // skip next [
                    continue;
                }
                inTag = true;
                tagStart = i;
                result.append(c);
            } else if (c == ']' && inTag) {
                inTag = false;
                result.append(c);
            } else if (inTag) {
                result.append(c);
            } else {
                result.append(c);
                visibleCount++;
            }
        }

        // If we stopped mid-tag, complete it
        if (inTag && tagStart >= 0) {
            // Find the end of the current tag
            int tagEnd = fullText.indexOf(']', result.length());
            if (tagEnd >= 0) {
                result.append(fullText, result.length(), tagEnd + 1);
            }
        }

        return result.toString();
    }
}
