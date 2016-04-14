package org.winterblade.minecraft.scripting;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
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
            System.err.println("Unable to load file processing header; things will go badly from here out...");
            headerTemp = "";
        }

        header = headerTemp;
    }

    /**
     * Gets a new context for your mod to use.
     * @param identifier            Your mod's name; this will be used for any print() statements inside your scripts.
     * @return                      The configured ScriptEngine.
     */
    public static ScriptEngine getNewContext(String identifier) {
        return getNewContext(identifier, false);
    }

    /**
     * Gets a new context for your mod to use.
     * @param identifier            Your mod's name; this will be used for any print() statements inside your scripts.
     * @param allowLoad             If scripts run in your mod are allowed to load other scripts (including those from
     *                              the internet); generally this is best left as false.
     * @return                      The configured ScriptEngine.
     */
    public static ScriptEngine getNewContext(String identifier, boolean allowLoad) {
        return getNewContext(identifier, new String[0], allowLoad);
    }

    /**
     * Gets a new context for your mod to use.
     * @param identifier            Your mod's name; this will be used for any print() statements inside your scripts.
     * @param allowedPackageRoots   A list of allowed packages that are able to be loaded from within scripts.
     *                              This should be limited to your mod if you allow users to edit your scripts, as
     *                              access to the Java internals in a script that's user editable could cause significant
     *                              security risks to your end users.  If you don't need to call back to packages in
     *                              your class, you may leave this blank.
     * @return                      The configured ScriptEngine.
     */
    public static ScriptEngine getNewContext(String identifier, String[] allowedPackageRoots) {
        return getNewContext(identifier, allowedPackageRoots, false);
    }


    /**
     * Gets a new context for your mod to use.
     * @param identifier            Your mod's name; this will be used for any print() statements inside your scripts.
     * @param allowedPackageRoots   A list of allowed packages that are able to be loaded from within scripts.
     *                              This should be limited to your mod if you allow users to edit your scripts, as
     *                              access to the Java internals in a script that's user editable could cause significant
     *                              security risks to your end users.  If you don't need to call back to packages in
     *                              your class, you may leave this blank.
     * @param allowLoad             If scripts run in your mod are allowed to load other scripts (including those from
     *                              the internet); generally this is best left as false.
     * @return                      The configured ScriptEngine.
     */
    public static ScriptEngine getNewContext(String identifier, String[] allowedPackageRoots, boolean allowLoad) {
        // TODO: Suppress warnings
        ScriptEngine nashorn = instance.factory.getScriptEngine(new ScriptExecutionSandbox(allowedPackageRoots));
        final Bindings bindings = nashorn.getBindings(ScriptContext.ENGINE_SCOPE);

        // Actually try and load our script header into Nashorn.
        try {
            nashorn.eval(instance.header + "\n__nashornLibInternal.configureLogName(" + identifier + ");");
        } catch (ScriptException e) {
            System.err.println("Error processing script header file; please report this issue: " + e.getMessage());
        }

        // Remove most of our bindings
        bindings.remove("exit");
        bindings.remove("quit");

        if(!allowLoad) {
            bindings.remove("load");
            bindings.remove("loadWithNewGlobal");
        }

        return nashorn;
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
