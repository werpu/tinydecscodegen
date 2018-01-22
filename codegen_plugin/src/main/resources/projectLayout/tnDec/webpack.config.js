/*
* various config settings for the build
*/
const path = require('path');

var config = {

    /*
    * Per default webpack uses the standard index.js from node to determine the main file
    * however, some modules do not have a main file but multiple entries points
    * and for such cases we can aliases. Also you can use aliases
    * to point to ts files hosted by the various modules so that
    * you can use ts instead of js, if given.
    */
    shims: {
        TinyDecorations$: path.resolve(__dirname, 'node_modules/ts-ng-tinydecorations/dist/TinyDecorations'),
        Routing$: path.resolve(__dirname, 'node_modules/ts-ng-tinydecorations/dist/Routing'),
        Cache$: path.resolve(__dirname, 'node_modules/ts-ng-tinydecorations/dist/Cache'),
        Dto$: path.resolve(__dirname, 'node_modules/ts-ng-tinydecorations/dist/Dto')
    },

    /**
     * a proxy configuration
     * for our rest calls, only used
     * in dev mode where we run two separate
     * server instances
     */
    proxy: {
        "/rest": "http://localhost:8080"
    }
};


module.exports = config;