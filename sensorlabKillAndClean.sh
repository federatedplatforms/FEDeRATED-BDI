#!/bin/bash

###########
# This script does the following:
#  1. Kill all processes whose PID are logged in logs/sensorlabLogs/log_pid.txt
#  2. Delete every log_* file in logs/sensorlabLogs 

PATHTOLOGFOLDER="logs/sensorlabLogs"

echo "Retrieving PIDs from log file..."
mapfile -t < $PATHTOLOGFOLDER/log_pid.txt

echo "Killing processes..."
kill ${MAPFILE[@]}

echo "Deleting logs..."
rm $PATHTOLOGFOLDER/log_*

echo "Done."
