#!/bin/bash

mvn install:install-file \
   -Dfile=wlthint3client.jar \
   -DgroupId=com.oracle.weblogic \
   -DartifactId=wlthint3client \
   -Dversion=12.2.1.4.0 \
   -Dpackaging=jar
