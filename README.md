#Nashorn Library

##Information
Library for providing the Java 8 Nashorn JavaScript script engine to Forge mods.

##Developing
In order to develop with the mod, follow the below process.

###Referencing It
* Add the following to your build.gradle at the top level (not inside the buildscript portion):

repositories {
    maven { url "http://maven.winterblade.org/content/repositories/minecraft/" }
}

* Add the following to dependencies, replacing the third part with your preferred build of Nashorn lib:
    deobfCompile "org.winterblade.minecraft:nashorn-lib:1.9.0-1.8.77-1.1.0.3"

###Using it
You'll need to create a class in your mod that extends INashornMod and has an @NashornMod annotation.  This class
requires three methods; one that returns your mod's logger, one that returns a list of packages that are safe to
access from your scripts (you may leave this as new String[0] if you don't need to access your classes from your
scripts), and one that's called when the Nashorn engine creates your context for you.  This is done during the
Initialization phase of starting the game, so, don't rely upon it being available until then.

In addition, you will need a public, default constructor for your class.  An example of this is below:

```java
@NashornMod
public class HelloWorldExample implements INashornMod {
    public HelloWorldExample() {}

    @Override
    public Logger getLogger() {
        return YourMod.logger;
    }

    @Override
    public String[] getAllowedPackageRoots() {
        return new String[] { "org.winterblade.minecraft.harmony"};
    }

    @Override
    public void onScriptContextCreated(IScriptContext iScriptContext) {
        iScriptContext.eval("print('Hello world');";
    }
}
```

Once you have your context, you may begin passing strings to the eval statement in order to run them.

##Versioning
The Nashorn library is versioned with a standard pattern; this standard pattern will be:
* 1.9.0-1.8.77-1.0.0

This has three parts, the first being the version of Minecraft the library has been built for; in this case, 1.9.0.
The second part will be the library version, starting with the version of Java the contained Nashorn library was 
pulled from (1.8.77).  The third and final part is the version of the mod itself, split into major, minor, and revision 
numbers.  Only minor implementation details will be changed between revisions, non-breaking API changes between minor 
versions, and API changes between major versions.