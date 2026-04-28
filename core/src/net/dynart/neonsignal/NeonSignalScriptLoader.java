package net.dynart.neonsignal;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

import net.dynart.lisa.core.script.Command;
import net.dynart.lisa.core.script.ScriptLoader;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.core.script.AddPlayerAbilityCommand;
import net.dynart.neonsignal.core.script.DelayCommand;
import net.dynart.neonsignal.core.script.MoveCameraToCommand;
import net.dynart.neonsignal.core.script.NexusSaysCommand;
import net.dynart.neonsignal.core.script.NexusSaysCommand.NexusLine;
import net.dynart.neonsignal.core.script.PlayMusicCommand;
import net.dynart.neonsignal.core.script.SayCommand;
import net.dynart.neonsignal.core.script.SetAnimationCommand;
import net.dynart.neonsignal.core.script.SetCameraLimitCommand;
import net.dynart.neonsignal.core.script.SetCameraTargetCommand;
import net.dynart.neonsignal.core.script.SetMovementActive;
import net.dynart.neonsignal.core.script.SetParentCommand;
import net.dynart.neonsignal.core.script.SetVisibleCommand;
import net.dynart.neonsignal.core.script.TriggerCommand;
import net.dynart.neonsignal.core.script.UpgradeDashCooldownCommand;
import net.dynart.neonsignal.core.script.UpgradeDashLongevityCommand;
import net.dynart.neonsignal.core.script.WalkToCommand;

@SuppressWarnings("unused") // the methods called by reflection
public class NeonSignalScriptLoader extends ScriptLoader {

    public Command createDelay(JsonValue value) {
        return new DelayCommand(
            engine,
            value.getFloat("duration")
        );
    }

    public Command createWalkTo(JsonValue value) {
        return new WalkToCommand(
            engine,
            value.getString("entity"),
            value.getString("target"),
            value.getBoolean("exact")
        );
    }

    public Command createSetAnimation(JsonValue value) {
        SetAnimationCommand result = new SetAnimationCommand(
            engine,
            value.getString("entity"),
            value.getString("animation")
        );
        if (value.has("layer")) {
            result.setLayer(value.getInt("layer"));
        }
        if (value.has("flip_x")) {
            result.setFlipX(value.getBoolean("flip_x"));
        }
        if (value.has("flip_y")) {
            result.setFlipY(value.getBoolean("flip_y"));
        }
        if (value.has("time")) {
            result.setTime(value.getFloat("time"));
        }
        return result;
    }

    public Command createSetCameraTarget(JsonValue value) {
        return new SetCameraTargetCommand(
            engine,
            value.getString("target"),
            value.has("smooth") && value.getBoolean("smooth")
        );
    }

    public Command createMoveCameraTo(JsonValue value) {
        return new MoveCameraToCommand(
            engine,
            value.getString("target"),
            value.getFloat("speed", 256) // TODO: default camera speed
        );
    }

    public Command createSay(JsonValue value) {
        return new SayCommand(
            engine,
            value.getString("name", "neonsignal"),
            value.getString("text"),
            value.getBoolean("start", false),
            value.getBoolean("finish", false),
            value.getBoolean("left", false)
        );
    }

    public Command createTrigger(JsonValue value) {
        return new TriggerCommand(
            engine,
            value.getString("name", "")
        );
    }

    public Command createAddPlayerAbility(JsonValue value) {
        PlayerAbility ability = PlayerAbility.valueOf(
            value.getString("ability", "move").toUpperCase()
        );
        return new AddPlayerAbilityCommand(engine, ability);
    }

    public Command createSetCameraLimit(JsonValue value) {
        return new SetCameraLimitCommand(engine, value.getString("entity"));
    }

    public Command createSetVisible(JsonValue value) {
        return new SetVisibleCommand(
            engine,
            value.getString("entity"),
            value.getBoolean("visible")
        );
    }

    public Command createPlayMusic(JsonValue value) {
        return new PlayMusicCommand(
            engine,
            value.getString("name")
        );
    }

    public Command createSetMovementActive(JsonValue value) {
        return new SetMovementActive(
            engine,
            value.getString("entity"),
            value.getBoolean("active"),
            value.getBoolean("finish_on_skip", true),
            value.getInt("finish_index", -1)
        );
    }

    public Command createSetParent(JsonValue value) {
        return new SetParentCommand(
            engine,
            value.getString("entity"),
            value.getString("parent")
        );
    }

    public Command createUpgradeDashCooldown(JsonValue value) {
        return new UpgradeDashCooldownCommand(engine);
    }

    public Command createUpgradeDashLongevity(JsonValue value) {
        return new UpgradeDashLongevityCommand(engine);
    }

    public Command createNexusSays(JsonValue value) {
        List<NexusLine> lines = new ArrayList<>();

        JsonValue linesArray = value.get("lines");
        if (linesArray != null) {
            for (JsonValue lineValue : linesArray) {
                String text = lineValue.getString("text", "");
                float delay = lineValue.getFloat("delay", 0);
                String font = lineValue.getString("font", null);
                float marginBottom = lineValue.getFloat("margin_bottom", 5);
                lines.add(new NexusLine(text, delay, font, marginBottom));
            }
        }

        float charDelay = value.getFloat("char_delay", 0.03f);
        float lineDelay = value.getFloat("line_delay", 0.5f);
        float holdTime = value.getFloat("hold_time", 2.0f);
        String buttonLabel = value.getString("button_label", null);

        return new NexusSaysCommand(engine, lines, charDelay, lineDelay, holdTime, buttonLabel);
    }
}
