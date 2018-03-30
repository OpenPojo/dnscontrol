#!/bin/sh

# This function on MAC allows you to easily switch which JVM to use on the command line.

function switch_jdk() {
  local wanted_java_version=$1
  export JAVA_HOME=`/usr/libexec/java_home -F -v $wanted_java_version -a x86_64 -d64`

  # cleaned PATH
  export PATH=$(echo $PATH | sed -E "s,(/System)?/Library/Java/JavaVirtualMachines/[a-zA-Z0-9._]+/Contents/Home/bin:,,g")

  # prepend wanted JAVA_HOME
  export PATH=$JAVA_HOME/bin:$PATH

  echo "Now using : "
  java -version
}

test_on_jdk() {
  switch_jdk $1
  mvn test jacoco:report
  if [ $? -ne 0 ]; then
    echo "Build failed on JDK $1"
    exit 1
  fi
}

mvn clean
test_on_jdk 1.7
test_on_jdk 1.8
test_on_jdk 9
test_on_jdk 10
