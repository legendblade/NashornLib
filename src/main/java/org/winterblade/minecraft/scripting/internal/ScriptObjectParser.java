package org.winterblade.minecraft.scripting.internal;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import org.objectweb.asm.Type;
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
     */
    public static void writeScriptObjectToClass(ScriptObjectMirror data, Object writeTo) {
        Class cls = writeTo.getClass();

        for(String key : data.keySet()) {
            try {
                updateField(cls, key, writeTo, data.get(key));
            } catch (Exception e) {
                System.err.println("Unable to deserialize '" + key + "' from the provided data: " + e.getMessage());
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
                System.err.println("Unable to register deserializer '" + instClass.getName() + "' " + e.getMessage());
            }
        }
    }

    /**
     * Uses the deserializer map to convert an object into the given class.
     * @param input The input to translate.
     * @param cls   The class to convert to.
     * @param <T>   The type to return
     * @return      The converted object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertData(Object input, Class<T> cls) {
        // If we have an array, this gets messy...
        if(cls.isArray()) {
            Object[] items = (Object[]) ScriptUtils.convert(input, Object[].class);

            Class componentType = cls.getComponentType();
            Object[] values = (Object[]) Array.newInstance(componentType, items.length);

            for(int i = 0; i < items.length; i++) {
                try {
                    values[i] = convertData(items[i], componentType);
                } catch (Exception e) {
                    values[i] = null;
                }
            }

            return (T)values;
        }

        // If we don't have a deserializer, then try and convert it through the script utils...
        try {
            if (!deserializerMap.containsKey(cls.getName())) return (T) ScriptUtils.convert(input, cls);

            IScriptObjectDeserializer deserializer = deserializerMap.get(cls.getName());
            return (T) deserializer.Deserialize(input);
        } catch(Exception e) {
            System.err.println("Error converting data to type '" + cls.getName() + "': " + e.getMessage());
            return null;
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
    private static void updateField(Class cls, String field, Object writeTo, Object value) throws InvocationTargetException, IllegalAccessException {
        // If we have a setter, use that...
        Method m = getFirstMethodByName(cls, field);

        if(m != null) {
            // Convert and call
            Class c = m.getParameterTypes()[0];
            m.invoke(writeTo, convertData(value, c));
            return;
        }

        Field f = getFieldByName(cls, field);

        if(f == null) return;

        f.setAccessible(true);
        f.set(writeTo, convertData(value, f.getType()));
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
