#!/bin/bash

#script will do the maven builds

echo ""
echo "**********************************************"
echo "* Maven building oep-aq-adapter"
echo "**********************************************"

if [[ -z "${TARGETDIR}" ]]; then
    echo "Environment Variable TARGETDIR is not set - aborting build"
    exit 1
else
   mvn clean install -Dmaven.test.skip
fi



mkdir -p $TARGETDIR/oep/lib

cp -rf target/oep-aq-adapter.jar $TARGETDIR/oep/lib/oep-aq-adapter.jar
