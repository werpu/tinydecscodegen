/**
 * dev server webpack definition
 *
 */
const merge = require('webpack-merge');
const common = require('./webpack.common.js');
const webpackConfig = require('./webpack.config.js');

const config = merge(common, {
    devServer: {
        port: 4200,
        hot: true,
        inline: true,
        proxy: webpackConfig.proxy
    }
});

module.exports = config;
