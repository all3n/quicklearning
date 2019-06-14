#!/usr/bin/env bash
export APP_DIR=`cd "$(dirname $0)/"; pwd`
env
echo "---------------------------------------------------"
ls -aRl
echo "---------------------------------------------------"
tar -zxvf $APP_DIR/ql.tar.gz
ls -aRl

WEB_DIR=$APP_DIR/public
BASE_PATH="{APP_BASE_URL}"

if [[ -n $APPLICATION_WEB_PROXY_BASE ]];then
    grep -r "$BASE_PATH" $WEB_DIR|awk -F: '{print $1}'|xargs -i sed -i "s#/${BASE_PATH}#$APPLICATION_WEB_PROXY_BASE#g" {}
fi


CLASSPATH=$APP_DIR/conf/:$APP_DIR/*:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH "$@"

