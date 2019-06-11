#!/usr/bin/env bash
export APP_DIR=`cd "$(dirname $0)/"; pwd`
env
tar -zxvf $APP_DIR/ql.tar.gz
ls -aRl
CLASSPATH=$APP_DIR/conf/:$APP_DIR/*:$APP_DIR/libs/*:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH \
  -Dspring.profiles.active=master \
  com.devhc.quicklearning.AppMaster