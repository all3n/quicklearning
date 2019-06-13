/*
 * vue.config.js
 * Copyright (C) 2019 wanghuacheng <wanghuacheng@wanghuacheng-PC>
 *
 * Distributed under terms of the MIT license.
 */
module.exports = {
  publicPath: process.env.NODE_ENV === 'production'
    ? '{APP_BASE_URL}/'
    : '/'
}
