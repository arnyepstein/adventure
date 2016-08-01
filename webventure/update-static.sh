#!/bin/sh
APPNAME=webventure
cp -pvf ./src/main/webapp/*.xhtml $TC_HOME/webapps/$APPNAME/
cp -pvf ./src/main/webapp/*.html $TC_HOME/webapps/$APPNAME/
cp -pvf ./src/main/webapp/js/*.js $TC_HOME/webapps/$APPNAME/js/
cp -pvf ./src/main/webapp/js/ae/*.js $TC_HOME/webapps/$APPNAME/js/ae/
cp -pvf ./src/main/webapp/css/*.css $TC_HOME/webapps/$APPNAME/css/
# cp -pvfn ./src/main/webapp/images/*.png $TC_HOME/webapps/$APPNAME/images/
# cp -pvfn ./src/main/webapp/images/*.svg $TC_HOME/webapps/$APPNAME/images/


