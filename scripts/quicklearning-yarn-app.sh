#!/usr/bin/env bash
env
echo "---------------------------------------------------"
ls -aRl
echo "---------------------------------------------------"
ENV_DIR=$PWD/quicklearning
ls -aRl

WEB_DIR=$ENV_DIR/public
BASE_PATH="{APP_BASE_URL}"

#if [[ -n $APPLICATION_WEB_PROXY_BASE ]];then
#    grep -r "$BASE_PATH" $WEB_DIR|awk -F: '{print $1}'|xargs -i sed -i "s#/${BASE_PATH}#$APPLICATION_WEB_PROXY_BASE#g" {}
#fi


CLASSPATH=$ENV_DIR/conf/:$ENV_DIR/*:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH "$@"

