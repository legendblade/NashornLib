package org.winterblade.minecraft.scripting;

import org.apache.logging.log4j.Logger;
import org.winterblade.minecraft.scripting.api.IScriptContext;

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
}
