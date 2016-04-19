package org.winterblade.minecraft.scripting.api;

import org.apache.logging.log4j.Logger;

/**
 * Identifies that a particular class utilizes the Nashorn system; this class must have the @NashornMod attribute on it
 * and have a default (parameterless) constructor.
 */

public interface INashornMod {
    /**
     * Returns an instance of the logger for your mod
     * @return  The Logger (generally from FML's getModLog() method)
     */
    Logger getLogger();

    /**
     * Returns a string array of allowed package roots that scripts created in your
     * context are allowed to execute.  This is generally safe to leave as new String[0],
     * unless you have a script that needs to create a class/call a static class in your mod,
     * in which case you should pass it the package name(s) of your class/classes.
     * @return  A string array of accepted package roots that are allowed to be called from a script.
     */
    String[] getAllowedPackageRoots();

    /**
     * Called when NashornLib has created your script context.
     * @param context   The script context created for your mod.
     */
    void onScriptContextCreated(IScriptContext context);
}
