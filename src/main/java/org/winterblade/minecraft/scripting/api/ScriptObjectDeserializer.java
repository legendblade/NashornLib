package org.winterblade.minecraft.scripting.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a class for use by the scripting system to deserialize an object from JSON; must also implement the
 * IScriptObjectDeserializer interface and have a default (parameterless) constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptObjectDeserializer {
    Class deserializes();
}