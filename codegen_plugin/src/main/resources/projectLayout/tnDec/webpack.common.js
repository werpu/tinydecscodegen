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

/**
 * common baseline for our
 * webpack build
 */
const path = require('path');
const webpackConfig = require('./webpack.config');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');


var extractLESS = new ExtractTextPlugin('styles/[name].css', {
    allChunks: true
});

const config = {
    entry: {vendor: ['./src/main/typescript/vendor.ts'], app:['./src/main/typescript/App.ts'], styles: './src/main/resources/styles.less'},
    output: {
        path: path.resolve(__dirname, '../../target'),
        filename: '[name].js'
    },

    resolve: {
        // Add `.ts` and `.tsx` as a resolvable extension.
        extensions: ['.ts', '.tsx', '.js'],

        alias: webpackConfig.shims
    },
    module: {
        rules: [{
            test: /\.less$/,
            use: [{
                loader: "style-loader"
            }, {
                loader: "css-loader", options: {
                    sourceMap: true,
                    url: false
                }
            }, {
                loader: "less-loader", options: {
                    sourceMap: true,
                    relativeUrls: true

                }
            }]
        },
            {test: /\.ts$/, loader: 'ts-loader'}
        ]

    },
    plugins: [
        new HtmlWebpackPlugin({template: './index.html', alwaysWriteToDisk: true}),
        new TsconfigPathsPlugin({configFile: "./src/main/typescript/tsconfig.json"}),
        new ExtractTextPlugin({
            filename: 'index.css',
            disable: false,
            allChunks: true
        }),
        new CopyWebpackPlugin([
            {from: 'src/main/resources/assets', to: 'assets'}])
    ],
    optimization: {
        splitChunks: {      // old CommonsChunkPlugin
            chunks: "all"
        }
    }
};

module.exports = config;



