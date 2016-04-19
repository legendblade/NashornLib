package org.winterblade.minecraft.scripting;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.winterblade.minecraft.scripting.api.INashornMod;
import org.winterblade.minecraft.scripting.internal.AnnotationUtil;
import org.winterblade.minecraft.scripting.internal.ScriptObjectParser;

import java.util.List;

/**
 * Created by Matt on 4/14/2016.
 */
@Mod(modid = NashornLibMod.MODID, version = NashornLibMod.VERSION)
public class NashornLibMod {
    public static final String MODID = "NashornLib";
    public static final String VERSION = "@VERSION@";
    private List<INashornMod> nashornMods;
    static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        nashornMods = AnnotationUtil.getNashornMods(evt.getAsmData());
        ScriptObjectParser.registerDeserializerClasses(AnnotationUtil.getScriptObjectDeserializers(evt.getAsmData()));
        logger = evt.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        for(INashornMod mod : nashornMods) {
            try {
                mod.onScriptContextCreated(
                        ScriptExecutionManager.getNewContext(
                                mod.getLogger(),
                                mod.getAllowedPackageRoots()));
            } catch(Exception ex) {
                logger.error("Error creating script context for '" + mod.getClass().getName() + "'.");
            }
        }
    }
}
