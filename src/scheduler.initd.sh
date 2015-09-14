#!/bin/bash
#
# chkconfig: 35 90 12
# description: Foo server
#
# Get function from functions library
. /etc/init.d/functions
# Start the service FOO
start() {
        initlog -c "echo -n Starting Scheduler: "
        /opt/scheduler/scheduler.sh
        ### Create the lock file ###
        touch /var/lock/subsys/scheduler
        success $"scheduler startup"
        echo
}
# Restart the service FOO
stop() {
        initlog -c "echo -n Stopping Scheduler: "
        killproc -p `echo /var/run/scheduler.pid`
        ### Now, delete the lock file ###
        rm -f /var/lock/subsys/scheduler
        echo
}
### main logic ###
case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  status)
        status -p `echo /var/run/scheduler.pid` scheduler
        ;;
  restart|reload|condrestart)
        stop
        start
        ;;
  *)
        echo $"Usage: $0 {start|stop|restart|reload|status}"
        exit 1
esac
exit 0
