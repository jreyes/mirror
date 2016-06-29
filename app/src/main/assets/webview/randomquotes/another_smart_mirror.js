'use strict';

var Log = (function () {
    return {
        info: function (message) {
            console.info(message);
        },
        log: function (message) {
            console.log(message);
        },
        error: function (message) {
            console.error(message);
        },
        warn: function (message) {
            console.warn(message);
        },
        group: function (message) {
            console.group(message);
        },
        groupCollapsed: function (message) {
            console.groupCollapsed(message);
        },
        groupEnd: function () {
            console.groupEnd();
        },
        time: function (message) {
            console.time(message);
        },
        timeEnd: function (message) {
            console.timeEnd(message);
        },
        timeStamp: function (message) {
            console.timeStamp(message);
        }
    };
})();

var Module = Class.create({

    // Module config defaults.
    defaults: {},

    // Timer reference used for showHide animation callbacks.
    showHideTimer: null,

    init: function () {
        this.name = '';
        this.identifier = '';
        this.hidden = false;
    },

    /* start()
     * Is called when the module is started.
     */
    start: function () {
        Log.info("Starting module: " + this.name);
    },

    /* getScripts()
     * Returns a list of scripts the module requires to be loaded.
     *
     * return Array<String> - An array with filenames.
     */
    getScripts: function () {
        return [];
    },

    /* getStyles()
     * Returns a list of stylesheets the module requires to be loaded.
     *
     * return Array<String> - An array with filenames.
     */
    getStyles: function () {
        return [];
    },

    /* getTranslations()
     * Returns a map of translation files the module requires to be loaded.
     *
     * return Map<String, String> - A map with langKeys and filenames.
     */
    getTranslations: function () {
        return false;
    },

    /* getDom()
     * This method generates the dom which needs to be displayed. This method is called by the Magic Mirror core.
     * This method needs to be subclassed if the module wants to display info on the mirror.
     *
     * return domobject - The dom to display.
     */
    getDom: function () {
        return document.createElement("div");
    },

    /* suspend()
     * This method is called when a module is hidden.
     */
    suspend: function () {
        Log.log(this.name + " is suspend.");
    },

    /* resume()
     * This method is called when a module is shown.
     */
    resume: function () {
        Log.log(this.name + " is resumed.");
    },

    /* updateDom(speed)
     * Request an (animated) update of the module.
     *
     * argument speed Number - The speed of the animation. (Optional)
     */
    updateDom: function (speed) {
        var newContent = this.getDom();
        var module = this;

        if (!this.hidden) {

            if (!this.moduleNeedsUpdate(newContent)) {
                return;
            }

            if (!speed) {
                this.updateModuleContent(newContent);
                return;
            }

            this.hideModule(speed / 2, function () {
                module.updateModuleContent(newContent);
                if (!module.hidden) {
                    module.showModule(speed / 2);
                }
            });
        } else {
            this.updateModuleContent(newContent);
        }
    },

    /* moduleNeedsUpdate(newContent)
     * Check if the content has changed.
     *
     * argument newContent Domobject - The new content that is generated.
     *
     * return bool - Does the module need an update?
     */
    moduleNeedsUpdate: function (newContent) {
        var moduleWrapper = document.getElementById(this.identifier);
        var contentWrapper = moduleWrapper.getElementsByClassName("module-content")[0];

        var tempWrapper = document.createElement("div");
        tempWrapper.appendChild(newContent);

        return tempWrapper.innerHTML !== contentWrapper.innerHTML;
    },

    /* moduleNeedsUpdate(newContent)
     * Update the content of a module on screen.
     *
     * argument newContent Domobject - The new content that is generated.
     */
    updateModuleContent: function (content) {
        var moduleWrapper = document.getElementById(this.identifier);
        var contentWrapper = moduleWrapper.getElementsByClassName("module-content")[0];

        contentWrapper.innerHTML = null;
        contentWrapper.appendChild(content);
    },

    /* hideModule(speed, callback)
     * Hide the module.
     *
     * argument speed Number - The speed of the hide animation.
     * argument callback function - Called when the animation is done.
     */
    hideModule: function (speed, callback) {
        var moduleWrapper = document.getElementById(this.identifier);
        if (moduleWrapper !== null) {
            moduleWrapper.style.transition = "opacity " + speed / 1000 + "s";
            moduleWrapper.style.opacity = 0;

            clearTimeout(this.showHideTimer);
            this.showHideTimer = setTimeout(function () {
                // To not take up any space, we just make the position absolute.
                // since it"s fade out anyway, we can see it lay above or
                // below other modules. This works way better than adjusting
                // the .display property.
                moduleWrapper.style.position = "absolute";

                if (typeof callback === "function") {
                    callback();
                }
            }, speed);
        }
    },

    /* showModule(speed, callback)
     * Show the module.
     *
     * argument speed Number - The speed of the show animation.
     * argument callback function - Called when the animation is done.
     */
    showModule: function (speed, callback) {
        var moduleWrapper = document.getElementById(this.identifier);
        if (moduleWrapper !== null) {
            moduleWrapper.style.transition = "opacity " + speed / 1000 + "s";
            // Restore the postition. See hideModule() for more info.
            moduleWrapper.style.position = "static";
            moduleWrapper.style.opacity = 1;

            clearTimeout(this.showHideTimer);
            this.showHideTimer = setTimeout(function () {
                if (typeof callback === "function") {
                    callback();
                }
            }, speed);
        }
    }
});

Module.definitions = {};

Module.create = function (name) {

    //Define the clone method for later use.
    function cloneObject(obj) {
        if (obj === null || typeof obj !== "object") {
            return obj;
        }

        var temp = obj.constructor(); // give temp the original obj"s constructor
        for (var key in obj) {
            temp[key] = cloneObject(obj[key]);
        }

        return temp;
    }

    var moduleDefinition = Module.definitions[name];
    var clonedDefinition = cloneObject(moduleDefinition);

    // Note that we clone the definition. Otherwise the objects are shared, which gives problems.
    var ModuleClass = Class.create(Module, clonedDefinition);
    var module = new ModuleClass();
    //Object.assign(module.defaults, {});
    module.name = name;
    module.identifier = "module_" + module;
    module.config = module.defaults;

    var dom = document.createElement("div");
    dom.id = module.identifier;
    dom.className = "module " + module.name + " " + module;
    dom.opacity = 0;
    document.body.appendChild(dom);

    var moduleContent = document.createElement("div");
    moduleContent.className = "module-content";
    dom.appendChild(moduleContent);

    module.updateDom(0);

    return module;
};

Module.register = function (name, moduleDefinition) {
    Module.definitions[name] = moduleDefinition;
};