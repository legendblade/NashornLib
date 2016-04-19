package org.winterblade.minecraft.scripting;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.logging.log4j.Logger;
import org.winterblade.minecraft.scripting.api.IScriptContext;

import javax.script.*;
import java.io.IOException;

/**
 * Created by Matt on 4/9/2016.
 */
public class ScriptExecutionManager {
    private final static ScriptExecutionManager instance = new ScriptExecutionManager();
    private final NashornScriptEngineFactory factory;
    private final String header;

    private ScriptExecutionManager() {
        factory = new NashornScriptEngineFactory();

        // Load our header, once:
        String headerTemp;
        try {
            headerTemp = Resources.toString(Resources.getResource("scripts/NashornLibHeader.js"), Charsets.UTF_8);
        } catch (IOException e) {
            NashornLibMod.logger.error("Unable to load file processing header; things will go badly from here out...", e);
            headerTemp = "";
        }

        header = headerTemp;
    }

    /**
     * Gets a new context for your mod to use.
     * @param logger                A logger that will handle messages coming from the print/console.log statements in
     *                              your script file.
     * @return                      The configured ScriptEngine.
     */
    public static IScriptContext getNewContext(Logger logger) {
        return getNewContext(logger, new String[0]);
    }


    /**
     * Gets a new context for your mod to use.
     * @param logger                A logger that will handle messages coming from the print/console.log statements in
     *                              your script file.
     * @param allowedPackageRoots   A list of allowed packages that are able to be loaded from within scripts.
     *                              This should be limited to your mod if you allow users to edit your scripts, as
     *                              access to the Java internals in a script that's user editable could cause significant
     *                              security risks to your end users.  If you don't need to call back to packages in
     *                              your class, you may leave this blank.
     * @return                      The configured ScriptEngine.
     */
    public static IScriptContext getNewContext(Logger logger, String[] allowedPackageRoots) {
        // TODO: Suppress warnings
        ScriptEngine nashorn = instance.factory.getScriptEngine(new ScriptExecutionSandbox(allowedPackageRoots));
        final Bindings bindings = nashorn.getBindings(ScriptContext.ENGINE_SCOPE);

        // Actually try and load our script header into Nashorn.
        try {
            nashorn.eval(instance.header);
        } catch (ScriptException e) {
            NashornLibMod.logger.error("Error processing script header file; please report this issue.", e);
        }

        Invocable invocable = (Invocable) nashorn;
        try {
            invocable.invokeFunction("__nashornLibInternalConfigureLogger", logger);
        } catch (ScriptException | NoSuchMethodException e) {
            NashornLibMod.logger.error("Unable to configure logger", e);
        }

        // Remove most of our bindings; in testing, we don't have access to these anyway
        bindings.remove("exit");
        bindings.remove("quit");
        bindings.remove("load");
        bindings.remove("loadWithNewGlobal");

        return new NashornScriptContext(logger, nashorn);
    }

    private static class ScriptExecutionSandbox implements ClassFilter {
        private final String[] allowedPackageRoots;

        ScriptExecutionSandbox(String[] allowedPackageRoots) {
            this.allowedPackageRoots = allowedPackageRoots;
        }

        @Override
        public boolean exposeToScripts(String s) {
            // Prevent scripts from using another SEM to gain access to more packages:
            if(s.equals("org.winterblade.minecraft.scripting.ScriptExecutionManager")) return false;

            // Some internal classes we need to hook into:
            if(s.equals("org.winterblade.minecraft.scripting.internal.JsonHelper")) return  true;

            // Figure out if we're in the allowed list:
            for(String allowedRoot : allowedPackageRoots) {
                if(s.startsWith(allowedRoot)) {
                    return true;
                }
            }

            return false;
        }
    }
}
