package org.winterblade.minecraft.scripting;

import jline.internal.Log;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.winterblade.minecraft.scripting.api.INashornMod;
import org.winterblade.minecraft.scripting.api.NashornMod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Borrowed from mezz.
 * https://github.com/mezz/JustEnoughItems/blob/c14f261afadf9605b8163aade1a6a2bffbb06409/src/main/java/mezz/jei/util/AnnotatedInstanceUtil.java
 */
public class AnnotatedInstanceUtil {
    private AnnotatedInstanceUtil() {

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
}
