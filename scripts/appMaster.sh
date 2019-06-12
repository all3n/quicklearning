#!/usr/bin/env bash
export APP_DIR=`cd "$(dirname $0)/"; pwd`
env
tar -zxvf $APP_DIR/ql.tar.gz
ls -aRl
CLASSPATH=$APP_DIR/conf/:$APP_DIR/*:$APP_DIR/libs/*:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH \
  com.devhc.quicklearning.master.AppMaster -w $APP_DIR/public