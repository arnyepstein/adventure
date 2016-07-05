#!/bin/sh
CONTEXT_NAME=webventure
BUILT_WAR_NAME=${CONTEXT_NAME}
PROJECT=.
if [ $TC_HOME ]
then
	rm -r $TC_HOME/webapps/${CONTEXT_NAME}
	rm $TC_HOME/webapps/${CONTEXT_NAME}.war
	cp -Rp ${PROJECT}/target/${BUILT_WAR_NAME}.war $TC_HOME/webapps/${CONTEXT_NAME}.war
	ls -l $TC_HOME/webapps/
else
	echo "Missing Environment Variable: \$TC_HOME"
fi
