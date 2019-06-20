#!/usr/bin/env bash
env
echo "---------------------------------------------------"
ls -aRl
echo "---------------------------------------------------"
ENV_DIR=$PWD/quicklearning

WEB_DIR=$ENV_DIR/web/appmaster
BASE_PATH="{APP_BASE_URL}"


CLASSPATH=$ENV_DIR/conf/:$ENV_DIR/*:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH "$@"

