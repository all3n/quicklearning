/*
 * vue.config.js
 * Copyright (C) 2019 wanghuacheng <wanghuacheng@wanghuacheng-PC>
 *
 * Distributed under terms of the MIT license.
 */

IS_PROD = process.env.NODE_ENV === 'production'

module.exports = {
  publicPath: IS_PROD
    ? '{APP_BASE_URL}/'
    : '/',
  chainWebpack: config => {
    // 修复HMR
    if (!IS_PROD) {
      config.resolve.symlinks(true);
    }
  },
  devServer: {
    disableHostCheck: true,
    proxy: {
      '/api': {
        target: 'http://localhost:33333',
        ws: true,
        changeOrigin: true,
        pathRequiresRewrite: {
          "^/api": "/"
        }
      }
    }
  },
  css: {
    modules: false,
    extract: IS_PROD,
  }
}
