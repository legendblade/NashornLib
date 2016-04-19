package org.winterblade.minecraft.scripting.api;

/**
 * Identifies a class for use by the scripting system to deserialize an object from JSON; must also implement the
 * ScriptObjectDeserializer attribute and have a default (parameterless) constructor.
 */
public interface IScriptObjectDeserializer {
    Object Deserialize(Object input);
}
