#Nashorn Library

##Information
Library for providing the Java 8 Nashorn JavaScript script engine to Forge mods.

##Developing
* Add the following to your build.gradle at the top level (not inside the buildscript portion):

repositories {
    maven { url "http://maven.winterblade.org/content/repositories/minecraft/" }
}

* Add the following to dependencies, replacing the third part with your preferred build of Nashorn lib:
    deobfCompile "org.winterblade.minecraft:nashorn-lib:1.9.0-1.8.77-1.0.0"


##Versioning
The Nashorn library is versioned with a standard pattern; this standard pattern will be:
* 1.9.0-1.8.77-1.0.0

This has three parts, the first being the version of Minecraft the library has been built for; in this case, 1.9.0.  
The second part will be the library version, starting with the version of Java the contained Nashorn library was 
pulled from (1.8.77).  The third and final part is the version of the mod itself, split into major, minor, and revision 
numbers.  Only minor implementation details will be changed between revisions, non-breaking API changes between minor 
versions, and API changes between major versions.