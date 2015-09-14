#!/bin/bash
java -jar /opt/scheduler/Scheduler.jar &
echo $! > /var/run/scheduler.pid
