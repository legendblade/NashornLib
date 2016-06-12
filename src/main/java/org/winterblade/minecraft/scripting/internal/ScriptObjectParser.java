package org.winterblade.minecraft.scripting.internal;

import com.google.common.base.Defaults;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.winterblade.minecraft.scripting.NashornLibMod;
import org.winterblade.minecraft.scripting.api.IScriptObjectDeserializer;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal class for use to help deserialize objects from JSON.
 */
public class ScriptObjectParser {
    private final static Map<String, IScriptObjectDeserializer> deserializerMap = new HashMap<>();

    /**
     * Reflects the Java object passed in and writes relevant data from the script object to fields on the Java object.
     * @param data      The script object
     * @param writeTo   The Java object
     * @param logger    The logger to write to.
     */
    public static void writeScriptObjectToClass(ScriptObjectMirror data, Object writeTo, Logger logger) {
        Class cls = writeTo.getClass();

        for(String key : data.keySet()) {
            try {
                updateField(cls, key, writeTo, data.get(key), logger);
            } catch (Exception e) {
                logger.warn("Unable to deserialize '" + key + "' from the provided data.", e);
            }
        }
    }

    /**
     * Register the map of deserializers.
     * @param deserializers A map of deserializers.
     */
    public static void registerDeserializerClasses(Map<Type, Class<IScriptObjectDeserializer>> deserializers) {
        for(Map.Entry<Type, Class<IScriptObjectDeserializer>> deserializer : deserializers.entrySet()) {
            Class<IScriptObjectDeserializer> instClass = deserializer.getValue();

            try {
                deserializerMap.put(deserializer.getKey().getClassName(), instClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                NashornLibMod.logger.warn("Unable to register deserializer '" + instClass.getName() + "'.", e);
            }
        }
    }

    /**
     * Uses the deserializer map to convert an object into the given class.
     * @param <T>       The type to return
     * @param input     The input to translate.
     * @param cls       The class to convert to.
     * @param logger    The logger to write to
     * @return          The converted object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertData(Object input, Class<T> cls, Logger logger) {
        // If we have an array, this gets messy...
        if(cls.isArray()) {
            if(ScriptObjectMirror.class.isAssignableFrom(input.getClass())) {
                if(((ScriptObjectMirror)input).size() <= 0) return (T) Array.newInstance(cls.getComponentType(),0);
            }

            Object[] items = (Object[]) ScriptUtils.convert(input, Object[].class);

            Class componentType = cls.getComponentType();
            Object[] values = (Object[]) Array.newInstance(componentType, items.length);

            for(int i = 0; i < items.length; i++) {
                try {
                    values[i] = convertData(items[i], componentType, logger);
                } catch (Exception e) {
                    values[i] = null;
                }
            }

            return (T)values;
        }

        try {
            // If we have a deserializer, use it...
            if (deserializerMap.containsKey(cls.getName())) {
                IScriptObjectDeserializer deserializer = deserializerMap.get(cls.getName());
                return (T) deserializer.Deserialize(input);
            }

            // If we don't have an object, go ahead and try to convert it using script utils:
            if(!ScriptObjectMirror.class.isAssignableFrom(input.getClass()) &&
                    !ScriptObject.class.isAssignableFrom(input.getClass())) return (T) ScriptUtils.convert(input, cls);

            // Otherwise, use some basic deserialization:
            ScriptObjectMirror mirror;

            // The first case will probably not happen, but, just in case...
            if(ScriptObjectMirror.class.isAssignableFrom(input.getClass())) {
                mirror = (ScriptObjectMirror) input;
            } else {
                mirror = ScriptUtils.wrap((ScriptObject) input);
            }

            // If we're actually a function, then allow Nashorn to deseralize us:
            if(mirror.isFunction()) {
                return (T) ScriptUtils.convert(input, cls);
            }

            // Now, really do basic deserialization:
            T output = cls.newInstance();

            for(Map.Entry<String, Object> entry : mirror.entrySet()) {
                Field f = getFieldByName(cls, entry.getKey());

                if(f == null) continue;

                // Update our field utilizing conversions:
                updateField(cls, entry.getKey(), output, entry.getValue(), logger);
            }

            return output;
        } catch(Throwable t) {
            logger.warn("Error converting data to type '" + cls.getName(), t);
            return Defaults.defaultValue(cls);
        }
    }

    /**
     * Called to update the given field through either a setter or direct field access.
     * @param cls       The class to write to
     * @param field     The field to write to
     * @param writeTo   The object to write values onto
     * @param value     The value to write
     * @throws InvocationTargetException    If we can't invoke a setter method
     * @throws IllegalAccessException       If we can't invoke the method or set the field.
     */
    private static void updateField(Class cls, String field, Object writeTo, Object value, Logger logger) throws InvocationTargetException, IllegalAccessException {
        // If we have a setter, use that...
        Method m = getFirstMethodByName(cls, field);

        if(m != null) {
            // Convert and call
            Class c = m.getParameterTypes()[0];
            m.invoke(writeTo, convertData(value, c, logger));
            return;
        }

        Field f = getFieldByName(cls, field);

        if(f == null) return;

        f.setAccessible(true);
        f.set(writeTo, convertData(value, f.getType(), logger));
    }

    /**
     * Gets the first setter method for a given field.
     * @param cls   The class to check
     * @param name  The name of the field to check
     * @return      The method, if there is one; null otherwise.
     */
    private static Method getFirstMethodByName(Class cls, String name) {
        Method[] methods = cls.getMethods();
        name = name.toLowerCase();

        for(Method method : methods) {
            if (method.getName().toLowerCase().equals("set" + name) && method.getParameterCount() == 1) return method;
        }

        return null;
    }

    /**
     * Gets a field for the given name.
     * @param cls   The class to check
     * @param name  The name of the field
     * @return      The field, if there is one; null otherwise.
     */
    private static Field getFieldByName(Class cls, String name) {
        Field field = null;

        Class cur = cls;

        do {
            try {
                field = cur.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // Ascend.
            }
            cur = cur.getSuperclass();
        } while(field == null && cur != null);

        if(field == null) return null;

        int modifiers = field.getModifiers();

        // Don't bother deserializing in these cases:
        return Modifier.isFinal(modifiers)
                || Modifier.isTransient(modifiers)
                || Modifier.isAbstract(modifiers) ? null : field;

    }

}
