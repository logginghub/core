@title Logging Hub
@cd /d %~dp0

call mvn -e exec:java -Dexec.mainClass="com.logginghub.logging.launchers.RunHub" -Dexec.args="%%classpath"

pause