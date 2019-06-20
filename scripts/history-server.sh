#!/usr/bin/env bash
export APP_DIR=`cd "$(dirname $0)/../"; pwd`

WEB_DIR=$APP_DIR/web/historyserver
PORT=0

CLASSPATH=$APP_DIR/conf/:$APP_DIR/*:`hadoop classpath`:$CLASSPATH
$JAVA_HOME/bin/java \
  -cp $CLASSPATH com.devhc.quicklearning.history.HistoryServer -w $WEB_DIR -p $PORT "$@"

