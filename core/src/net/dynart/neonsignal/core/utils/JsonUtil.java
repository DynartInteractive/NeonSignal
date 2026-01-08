package net.dynart.neonsignal.core.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

public class JsonUtil {

    public static JsonValue tryToLoad(JsonReader jsonReader, FileHandle fileHandle) {
        try {

            if (fileHandle.exists()) {
                return jsonReader.parse(fileHandle);
            } else {
                Gdx.app.log("Resources", "JSON file not found: " + fileHandle.path());
            }
        } catch (Exception e) {
            Gdx.app.error("Resources", "Error reading JSON " + fileHandle.path() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Merges two JsonValue objects. If a key exists in both, externalJson's value takes precedence.
     * Returns a new JsonValue object representing the merged result.
     */
    public static JsonValue mergeJson(JsonValue internalJson, JsonValue externalJson) {
        // If no external JSON, return a copy of internal JSON (or empty object if null)
        if (externalJson == null) {
            return internalJson != null ? deepCopy(internalJson) : new JsonValue(JsonValue.ValueType.object);
        }

        // If no internal JSON, return a copy of external JSON
        if (internalJson == null) {
            return deepCopy(externalJson);
        }

        // Both JSONs exist, perform a recursive merge
        JsonValue mergedJson;
        if (internalJson.isObject() && externalJson.isObject()) {
            mergedJson = new JsonValue(JsonValue.ValueType.object);
            // Copy all internal JSON fields
            for (JsonValue entry = internalJson.child; entry != null; entry = entry.next) {
                mergedJson.addChild(entry.name, deepCopy(entry));
            }
            // Merge or add external JSON fields
            for (JsonValue entry = externalJson.child; entry != null; entry = entry.next) {
                if (mergedJson.has(entry.name)) {
                    // If key exists, merge the values recursively
                    JsonValue existing = mergedJson.get(entry.name);
                    mergedJson.remove(entry.name);
                    mergedJson.addChild(entry.name, mergeJson(existing, entry));
                } else {
                    // If key doesn't exist, add a copy
                    mergedJson.addChild(entry.name, deepCopy(entry));
                }
            }
        } else {
            // For primitive and array values, external takes precedence
            mergedJson = deepCopy(externalJson);
        }

        return mergedJson;
    }


    /**
     * Creates a deep copy of a JsonValue object.
     */
    private static JsonValue deepCopy(JsonValue original) {
        if (original == null) {
            return new JsonValue(JsonValue.ValueType.object);
        }

        JsonValue copy;
        switch (original.type()) {
            case object:
                copy = new JsonValue(JsonValue.ValueType.object);
                for (JsonValue child = original.child; child != null; child = child.next) {
                    copy.addChild(child.name, deepCopy(child));
                }
                break;
            case array:
                copy = new JsonValue(JsonValue.ValueType.array);
                for (JsonValue child = original.child; child != null; child = child.next) {
                    copy.addChild(deepCopy(child));
                }
                break;
            case stringValue:
                copy = new JsonValue(original.asString());
                break;
            case doubleValue:
                copy = new JsonValue(original.asDouble());
                break;
            case longValue:
                copy = new JsonValue(original.asLong());
                break;
            case booleanValue:
                copy = new JsonValue(original.asBoolean());
                break;
            case nullValue:
                copy = new JsonValue(JsonValue.ValueType.nullValue);
                break;
            default:
                copy = new JsonValue(JsonValue.ValueType.object); // Fallback
                break;
        }

        return copy;
    }

}
