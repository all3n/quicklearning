#!/usr/bin/env bash
export APP_DIR=`cd "$(dirname $0)/../"; pwd`

APP_JAR=`ls $APP_DIR/*.jar|head -n 1`

java -cp $APP_DIR/conf/:$APP_DIR/libs/*:$APP_DIR/*:`hadoop classpath` \
    -Dspring.profiles.active=client \
    com.devhc.quicklearning.Client \
  "$@"


