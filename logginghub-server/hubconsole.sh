#!/bin/bash
REPO=~/.m2/repository

function dep() {
echo Running dep
 eval "$1='$1:$REPO/$2/$3/$4/$3-$4.$5'"
}

cp=''

dep cp jline jline 2.12 jar

cp=$cp:../vl-utils/target/classes
cp=$cp:../vl-logging-client/target/classes

echo $cp

debugAgent=''

if [ $1 == "debug" ]
then
debugAgent='-agentlib:jdwp=transport=dt_socket,suspend=y,server=y,address=localhost:12345'
fi

java $debugAgent -cp target/classes:$cp com.logginghub.logging.commandline.LoggingHubCLI 

