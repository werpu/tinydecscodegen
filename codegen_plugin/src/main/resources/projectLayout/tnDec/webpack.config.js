/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

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