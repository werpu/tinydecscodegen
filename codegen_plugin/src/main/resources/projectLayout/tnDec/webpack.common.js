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



