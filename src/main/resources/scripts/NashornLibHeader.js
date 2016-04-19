var __nashornLibInternalImpl = function() {
    var ns = this;

    ns.JsonHelper = Java.type('org.winterblade.minecraft.scripting.internal.JsonHelper');


    // We'll store our logger here later:
    var logger = null;

    // Log something in the logger:
    ns.log = function(level, args) {
        // It will be undefined if defined... trust me.
        if(logger === null) return;
        var line = "";

        for(var i in args) {
            if(args[i] === null || args[i] === undefined) {
                line = "null";
            } else if(typeof args[i] === 'object' || args[i] instanceof Array) {
                line = JSON.stringify(args[i]);
            } else {
                line = args[i];
            }

            switch(level) {
                case 0:
                    logger.trace(line);
                    break;
                case 1:
                    logger.debug(line);
                    break;
                case 2:
                case 3:
                    logger.info(line);
                    break;
                case 4:
                    logger.warn(line);
                    break;
                case 5:
                    logger.error(line);
                    break;
                case 6:
                    logger.fatal(line);
                    break;
            }
        }
    }

    // Set up the logger:
    ns.configureLogger = function(log) {
        logger = log;
    }

    /*
     * Used by our SEM to parse NBT into a Minecraft format.
     */
    ns.getJsonString = function(obj) {
        return JSON.stringify(obj).replace(/\"([^(\")"]+)\":/g,"$1:");
    }

    ns.JsonHelper.registerCallback(ns.getJsonString);
}

__nashornLibInternal = new __nashornLibInternalImpl();

// We can't call into the object from the SEM, so, pass it here:
__nashornLibInternalConfigureLogger = function(logger) {
    __nashornLibInternal.configureLogger(logger);
}

// Remap print to our internal
print = function() {
    __nashornLibInternal.log(2,arguments);
}

// Also allow console.log/etc
logger = console = {
    trace: function() {__nashornLibInternal.log(0,arguments);},
    debug: function() {__nashornLibInternal.log(1,arguments);},
    log: function() {__nashornLibInternal.log(2,arguments);},
    info: function() {__nashornLibInternal.log(3,arguments);},
    warn: function() {__nashornLibInternal.log(4,arguments);},
    error: function() {__nashornLibInternal.log(5,arguments);},
    fatal: function() {__nashornLibInternal.log(6,arguments);}
}
