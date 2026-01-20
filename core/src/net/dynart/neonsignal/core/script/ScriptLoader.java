package net.dynart.neonsignal.core.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import net.dynart.neonsignal.core.EntityManager;
import net.dynart.neonsignal.core.PlayerAbility;
import net.dynart.neonsignal.core.utils.StringUtil;
import net.dynart.neonsignal.core.script.NexusSaysCommand.NexusLine;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.dynart.neonsignal.core.Engine;

@SuppressWarnings("unused") // the methods called by reflection
public class ScriptLoader {

    protected Engine engine;
    protected EntityManager entityManager;
    protected List<SkippableCommand> skippableCommandList = new LinkedList<>();

    public void init(Engine engine) {
        this.engine = engine;
    }

    public List<Command> load(String path) {
        JsonReader json = new JsonReader();
        JsonValue root = json.parse(Gdx.files.internal(path));
        return loadCommands(root.get("sequence"));
    }

    public List<SkippableCommand> getSkippableCommandList() {
        return skippableCommandList;
    }

    private List<Command> loadCommands(JsonValue values) {
        List<Command> result = new LinkedList<>();
        for (JsonValue value : values) {
            Command command = loadCommand(value);
            result.add(command);
        }
        return result;
    }

    private Command loadCommand(JsonValue value) {
        if (value.child() == null) {
            throw new RuntimeException("Could not load command: " + value);
        }
        return callCreateMethodForCommand(value.child(), value.child().name());
    }

    private Command callCreateMethodForCommand(JsonValue value, String commandName) {
        String methodName = "create" + StringUtil.camelize(commandName);
        String fullMethodName = getClass() + "::" + methodName + "()";
        Command result;
        try {
            Method method = getClass().getMethod(methodName, JsonValue.class);
            Object objectResult = method.invoke(this, value);
            result = (Command)objectResult;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't create command (no such method): " + fullMethodName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't create command (illegal access): " + fullMethodName);
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            throw new RuntimeException("Can't create command (runtime error): " + fullMethodName);
        }
        if (result instanceof SkippableCommand) {
            skippableCommandList.add((SkippableCommand)result);
        }
        return result;
    }

    public Command createParallel(JsonValue value) {
        List<Command> commands = loadCommands(value);
        ParallelCommand result = new ParallelCommand();
        result.init(commands);
        return result;
    }

    public Command createSequence(JsonValue value) {
        List<Command> commands = loadCommands(value);
        SequenceCommand result = new SequenceCommand();
        result.init(commands);
        return result;
    }

    public Command createDelay(JsonValue value) {
        return new DelayCommand(engine,
            value.getFloat("duration")
        );
    }

    public Command createWalkTo(JsonValue value) {
        return new WalkToCommand(engine,
            value.getString("entity"),
            value.getString("target"),
            value.getBoolean("exact")
        );
    }

    public Command createSetAnimation(JsonValue value) {
        SetAnimationCommand result = new SetAnimationCommand(engine,
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
        return new SetCameraTargetCommand(engine,
            value.getString("target"),
            value.has("smooth") && value.getBoolean("smooth")
        );
    }

    public Command createMoveCameraTo(JsonValue value) {
        return new MoveCameraToCommand(engine,
            value.getString("target"),
            value.getFloat("speed", 256) // TODO: default camera speed
        );
    }

    public Command createSay(JsonValue value) {
        return new SayCommand(engine,
            value.getString("name", "neonsignal"),
            value.getString("text"),
            value.getBoolean("start", false),
            value.getBoolean("finish", false),
            value.getBoolean("left", false)
        );
    }

    public Command createTrigger(JsonValue value) {
        return new TriggerCommand(engine,
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
        return new SetVisibleCommand(engine,
            value.getString("entity"),
            value.getBoolean("visible")
        );
    }

    public Command createPlayMusic(JsonValue value) {
        return new PlayMusicCommand(engine,
            value.getString("name")
        );
    }

    public Command createSetMovementActive(JsonValue value) {
        return new SetMovementActive(engine,
            value.getString("entity"),
            value.getBoolean("active"),
            value.getBoolean("finish_on_skip", true),
            value.getInt("finish_index", -1)
        );
    }

    public Command createSetParent(JsonValue value) {
        return new SetParentCommand(engine,
            value.getString("entity"),
            value.getString("parent")
        );
    }

    public Command createNexusSays(JsonValue value) {
        List<NexusLine> lines = new ArrayList<>();

        JsonValue linesArray = value.get("lines");
        if (linesArray != null) {
            for (JsonValue lineValue : linesArray) {
                String text = lineValue.getString("text", "");
                float delay = lineValue.getFloat("delay", 0);
                lines.add(new NexusLine(text, delay));
            }
        }

        float charDelay = value.getFloat("char_delay", 0.03f);
        float lineDelay = value.getFloat("line_delay", 0.5f);
        float holdTime = value.getFloat("hold_time", 2.0f);

        return new NexusSaysCommand(engine, lines, charDelay, lineDelay, holdTime);
    }
}
