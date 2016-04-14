var __nashornLibInternalImpl = function() {
    var ns = this;

    // Save our access to the print binding:
    var printImpl = print;
    var modId = "";

    ns.configureLogName = function(id) {
        ns.modId = id;
    }

    ns.log(level, args) {
        var lineStart = modId + " [" + level + "]: ";

        for(var i in args) {
            if(args[i] === null || args[i] === undefined) {
                printImpl.apply(this, lineStart + "null");
                return;
            }

            if(typeof args[i] === 'object' || args[i] instanceof Array) {
                printImpl.apply(this, lineStart + JSON.stringify(args[i]));
            } else {
                printImpl.apply(this, lineStart + args[i]);
            }
        }
    }
}

__nashornLibInternal = new __nashornLibInternalImpl();

// Remap print to our internal
print = function() {
    __nashornLibInternal.log("INFO ",arguments);
}

// Also allow console.log/etc
console = {
    debug: function() {__nashornLibInternal.log("DEBUG",arguments);},
    log: function() {__nashornLibInternal.log("INFO ",arguments);},
    info: function() {__nashornLibInternal.log("INFO ",arguments);},
    error: function() {__nashornLibInternal.log("ERROR",arguments);}
}
