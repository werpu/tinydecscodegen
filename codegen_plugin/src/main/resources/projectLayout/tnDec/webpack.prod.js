/**
 * production file handling for our webpack build
 */
const merge = require('webpack-merge');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const common = require('./webpack.common.js');
const ExtractTextPlugin = require("mini-css-extract-plugin");
const CleanWebpackPlugin = require('clean-webpack-plugin');
var WebpackMd5Hash = require('webpack-md5-hash');

module.exports = merge(common, {
    devtool: 'source-map',
    output: {
        filename: '[name].[chunkhash].js',
        chunkFilename: "[chunkhash].[id].chunk.js"
    },
    module: {
        rules: [
            {
                test: /\.less$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: [{
                        loader: "css-loader", options: {
                            sourceMap: true,
                            minimize: true || {/* CSSNano Options */}
                        }
                    }, 'less-loader']
                })
            }
        ]
    },
    plugins: [
        new CleanWebpackPlugin(['../../target']),
        new UglifyJSPlugin({
            sourceMap: true
        }),
        new WebpackMd5Hash(),
        new ExtractTextPlugin({
            filename: 'index.[chunk' +
                'hash].css',
            disable: false,
            allChunks: true
        })
    ]
});

