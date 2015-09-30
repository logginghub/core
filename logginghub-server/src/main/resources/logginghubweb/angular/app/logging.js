var loggerFactoryFactory = function (factoryName) {

    var factory = {};
    var loggersByName = {}
    factory.factoryName = factoryName

    factory.logger = function (name) {

        var logger = loggersByName[name];
        if (logger == undefined) {

            logger = {}

            loggersByName[name] = logger

            logger.levels = {}
            logger.levels.warning = 6
            logger.levels.info = 5
            logger.levels.debug = 4

            logger.filterLevel = logger.levels.info;

            logger.level = function (level) {
                logger.filterLevel = level
            }

            logger.info = function (message) {
                if (logger.filterLevel <= logger.levels.info) {
                    var params = Array.prototype.slice.call(arguments);
                    params.shift();
                    logger.log("INFO", message, params)
                }
            }

            logger.debug = function (message) {
                if (logger.filterLevel <= logger.levels.debug) {
                    var params = Array.prototype.slice.call(arguments);
                    params.shift();
                    logger.log("DEBUG", message, params)
                }
            }

            logger.warning = function (message) {
                if (logger.filterLevel <= logger.levels.warning) {
                    var params = Array.prototype.slice.call(arguments);
                    params.shift();
                    logger.log("WARN", message, params)
                }
            }

            logger.json = function (object) {
                return JSON.stringify(object);
            }

            logger.log = function (level, message, params) {
                var namePad = "             ";
                var messagePart = "|" + factory.padLeft("     ", level) + " | " + factory.padLeft(namePad, name) + " | " + message;

                if (params && params.length > 0) {
                    if (params.length == 1) {
                        console.info(messagePart, params[0])
                    } else if (params.length == 2) {
                        console.info(messagePart, params[0], params[1])
                    } else if (params.length == 3) {
                        console.info(messagePart, params[0], params[1], params[2])
                    } else if (params.length == 4) {
                        console.info(messagePart, params[0], params[2], params[3])
                    } else {
                        console.info(messagePart)
                    }
                } else {
                    console.info(messagePart)
                }
            }


        }

        return logger;
    }

    factory.padLeft = function pad(pad, str, padLeft) {
        if (typeof str === 'undefined')
            return pad;
        if (padLeft) {
            return (pad + str).slice(-pad.length);
        } else {
            return (str + pad).substring(0, pad.length);
        }
    }

    return factory;
}

var loggerFactory = loggerFactoryFactory("globalFactory");
var logger = loggerFactory.logger("global");
