package net.dynart.neonsignal.core.script;

import net.dynart.neonsignal.core.Engine;
import net.dynart.neonsignal.screens.CutsceneScreen;

import java.util.List;

/**
 * Script command that displays multi-line text with typewriter animation.
 * Supports inline color markup and configurable timing.
 */
public class NexusSaysCommand implements Command, SkippableCommand {

    /**
     * Represents a single line of text with its delay before displaying.
     */
    public static class NexusLine {
        public final String text;
        public final float delay;
        public final String font;

        public NexusLine(String text, float delay, String font) {
            this.text = text;
            this.delay = delay;
            this.font = font;
        }
    }

    private final CutsceneScreen cutsceneScreen;
    private final List<NexusLine> lines;
    private final float charDelay;
    private final float lineDelay;
    private final float holdTime;
    private final String buttonLabel;

    private boolean first = true;

    public NexusSaysCommand(Engine engine, List<NexusLine> lines, float charDelay, float lineDelay, float holdTime, String buttonLabel) {
        this.cutsceneScreen = (CutsceneScreen) engine.getScreen("cutscene");
        this.lines = lines;
        this.charDelay = charDelay;
        this.lineDelay = lineDelay;
        this.holdTime = holdTime;
        this.buttonLabel = buttonLabel;
    }

    @Override
    public boolean act(float delta) {
        if (first) {
            first = false;
            cutsceneScreen.nexusSays(lines, charDelay, lineDelay, holdTime, buttonLabel);
            return false;
        }
        return cutsceneScreen.isNexusSaysFinished();
    }

    @Override
    public void skip() {
        cutsceneScreen.skipNexusSays();
    }
}
