package org.winterblade.minecraft.scripting.internal;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.logging.log4j.Logger;

/**
 * Created by Matt on 4/19/2016.
 */
public class JsonHelper {

    private static JsonStringifyCallback callbackFunc;

    private JsonHelper() {}

    @FunctionalInterface
    public static interface JsonStringifyCallback {
        public String stringifyNbt(Object nbt);
    }

    /**
     * Callback from our script in order to allow us to register the callback from Nashorn.
     * @param callback  The callback function
     */
    public static void registerCallback(JsonStringifyCallback callback) {
        callbackFunc = callback;
    }


    /**
     * Used to translate an object into a JSON string that can be read in by Minecraft (mainly for parsing NBT)
     * @param o The object to parse
     * @return  The JSON string.
     */
    public static String getJsonString(JSObject o, Logger log) {
        if(callbackFunc == null) {
            log.warn("Internal object hasn't been registered yet.");
            return "";
        }

        try {
            return callbackFunc.stringifyNbt(o);
        } catch(Exception e) {
            log.warn("Unable to call JSON.stringify with the provided object.");
            return "";
        }
    }
}
