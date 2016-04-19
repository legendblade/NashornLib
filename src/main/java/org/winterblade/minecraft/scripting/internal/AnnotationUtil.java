package org.winterblade.minecraft.scripting.internal;

import jline.internal.Log;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.objectweb.asm.Type;
import org.winterblade.minecraft.scripting.api.INashornMod;
import org.winterblade.minecraft.scripting.api.IScriptObjectDeserializer;
import org.winterblade.minecraft.scripting.api.NashornMod;
import org.winterblade.minecraft.scripting.api.ScriptObjectDeserializer;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Borrowed from mezz.
 * https://github.com/mezz/JustEnoughItems/blob/c14f261afadf9605b8163aade1a6a2bffbb06409/src/main/java/mezz/jei/util/AnnotatedInstanceUtil.java
 */
public class AnnotationUtil {
    private AnnotationUtil() {}

    public static Map<Type, Class<IScriptObjectDeserializer>> getScriptObjectDeserializers(@Nonnull ASMDataTable asmDataTable) {
        return getClassMap(asmDataTable, ScriptObjectDeserializer.class, IScriptObjectDeserializer.class, "deserializes");
    }

    public static List<INashornMod> getNashornMods(@Nonnull ASMDataTable asmDataTable) {
        return getInstances(asmDataTable, NashornMod.class, INashornMod.class);
    }

    private static <T> List<T> getInstances(@Nonnull ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        List<T> instances = new ArrayList<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Log.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
        return instances;
    }

    @SuppressWarnings("unchecked")
    private static <T,Tk> Map<Tk, Class<T>> getClassMap(@Nonnull ASMDataTable asmDataTable,
                                                        Class<?> annotationClass,
                                                        Class<T> outputClass,
                                                        String idParam) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmTable = asmDataTable.getAll(annotationClassName);

        Map<Tk,Class<T>> instances = new HashMap<>();
        for (ASMDataTable.ASMData asmData : asmTable) {
            try {
                Class<?> asmClass = Class.forName(asmData.getClassName());

                if(!outputClass.isAssignableFrom(asmClass)) {
                    System.err.println("Attempted to load '" + asmClass.getSimpleName() +
                            "', but it doesn't implement '" + outputClass.getSimpleName() + "'.");
                    continue;
                }

                // Fall back to name
                Object name = null;

                if(asmData.getAnnotationInfo().containsKey(idParam)) {
                    name = asmData.getAnnotationInfo().get(idParam);
                }

                if(name == null) {
                    System.err.println("Attempted to load '" + asmClass.getSimpleName() +
                            "', couldn't find the ID parameter '" + idParam + "' on it.");
                    continue;
                }

                instances.put((Tk) name, (Class<T>) asmClass);
            } catch (ClassNotFoundException e) {
                System.err.println("Failed to load: " + asmData.getClassName() + ".\n" + Arrays.toString(e.getStackTrace()));
            }
        }
        return instances;
    }
}
