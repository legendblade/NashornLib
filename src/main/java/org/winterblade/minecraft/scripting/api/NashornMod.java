package org.winterblade.minecraft.scripting.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies that a particular class utilizes the Nashorn system; this class must implement INashornMod
 * and have a default (parameterless) constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NashornMod {

}
