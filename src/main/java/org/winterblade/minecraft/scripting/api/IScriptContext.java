package org.winterblade.minecraft.scripting.api;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.ScriptException;

/**
 * Created by Matt on 4/14/2016.
 */
public interface IScriptContext {
    /**
     * Evaluate the given script
     * @param script    The script to evaluate
     * @return  True if execution succeeded.
     * @throws ScriptException  If any script errors were encountered.
     */
    boolean eval(String script) throws ScriptException;

    /**
     * Converts a JavaScript object into the given type using IScriptObjectDeserializers.
     * @param input    The JavaScript object to parse
     * @param cls      The class to deserialize it into
     * @param <T>      The returned type
     * @return         The deserialized instance of the given object, null otherwise.
     */
    <T> T convertData(Object input, Class<T> cls);


    /**
     * Parses the given script object mirror, writing its data to the passed in object, using IScriptObjectDeserializers
     * @param data       The script object mirror to process.
     * @param writeTo    The object to write it to.
     */
    void parseScriptObject(ScriptObjectMirror data, Object writeTo);


    /**
     * Parses the given JSON object into a JSON string.  Namely used to process NBT from JavaScript
     * @param object    The object to parse
     * @return          The stringified JSON object.
     */
    String stringifyJsonObject(JSObject object);

    /**
     * Invoke a function inside your script context
     * @param function                  The method to run
     * @param args                      The arguments to pass to your method
     * @return                          An object as the result of your method
     * @throws ScriptException
     * @throws NoSuchMethodException
     */
    Object invokeFunction(String function, Object... args) throws ScriptException, NoSuchMethodException;
}
