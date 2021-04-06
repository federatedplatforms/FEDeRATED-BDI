#!/bin/bash

###########
# This script does the following:
#  1. Pull the repository to ensure everything is up-to-date;
#  2. Deploy the nodes (i.e. build the jar files);
#  3. Run the nodes (all on the same machine, where this script is run);
#  4. Run the webserver;
#
# Logs are saved in $PATHTOLOGFOLDER
# PID of the processes are logged for an easier burn-down when needed.

PATHTOLOGFOLDER="logs/sensorlabLogs"

echo "Pulling the repository..."
git pull

echo "Deploying nodes..."
./gradlew deployNodes

echo "Initiating nodes..."
nohup java -jar build/nodes/Notary/corda.jar --base-directory=build/nodes/Notary > $PATHTOLOGFOLDER/log_notary.txt & echo $! >> $PATHTOLOGFOLDER/log_pid.txt 
nohup java -jar build/nodes/Netherlands/corda.jar --base-directory=build/nodes/PartyA > $PATHTOLOGFOLDER/log_netherlands.txt & echo $! >> $PATHTOLOGFOLDER/log_pid.txt
nohup java -jar build/nodes/Spain/corda.jar --base-directory=build/nodes/PartyB > $PATHTOLOGFOLDER/log_spain.txt & echo $! >> $PATHTOLOGFOLDER/log_pid.txt
nohup java -jar build/nodes/Italy/corda.jar --base-directory=build/nodes/PartyB > $PATHTOLOGFOLDER/log_italy.txt & echo $! >> $PATHTOLOGFOLDER/log_pid.txt

# Waiting 40 seconds to give the time to the nodes to be up and running,
# otherwise webserver deployment will fail.
sleep 40

echo "Deployment of webserver..."
nohup ./gradlew runTemplateServer > $PATHTOLOGFOLDER/log_webserver.txt & echo $! >> $PATHTOLOGFOLDER/log_pid.txt 

echo "Done."
