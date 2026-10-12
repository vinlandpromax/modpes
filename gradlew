#!/bin/sh
# Gradle wrapper script
GRADLE_OPTS="${GRADLE_OPTS:-}"
APP_HOME="${APP_HOME:-$(dirname "$0")}"
exec "${JAVA_HOME:-}/bin/java" ${GRADLE_OPTS} -classpath "${APP_HOME}/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
