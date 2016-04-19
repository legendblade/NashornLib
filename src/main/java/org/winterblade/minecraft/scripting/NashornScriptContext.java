package org.winterblade.minecraft.scripting;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.logging.log4j.Logger;
import org.winterblade.minecraft.scripting.api.IScriptContext;
import org.winterblade.minecraft.scripting.internal.ScriptObjectParser;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Created by Matt on 4/14/2016.
 */
public class NashornScriptContext implements IScriptContext {
    private final Logger logger;
    private final ScriptEngine nashorn;

    public NashornScriptContext(Logger logger, ScriptEngine nashorn) {
        this.logger = logger;
        this.nashorn = nashorn;
    }

    @Override
    public boolean eval(String script) {
        try {
            nashorn.eval(script);
        } catch (ScriptException e) {
            logger.error("Error evaluating script file.", e);
            return false;
        }
        return true;
    }

    /**
     * Converts a JavaScript object into the given type using IScriptObjectDeserializers.
     *
     * @param input The JavaScript object to parse
     * @param cls   The class to deserialize it into
     * @return The deserialized instance of the given object, null otherwise.
     */
    @Override
    public <T> T convertData(Object input, Class<T> cls) {
        return ScriptObjectParser.convertData(input, cls);
    }

    /**
     * Parses the given script object mirror, writing its data to the passed in object, using IScriptObjectDeserializers
     *
     * @param data    The script object mirror to process.
     * @param writeTo The object to write it to.
     */
    @Override
    public void parseScriptObject(ScriptObjectMirror data, Object writeTo) {
        ScriptObjectParser.writeScriptObjectToClass(data, writeTo);
    }
}
