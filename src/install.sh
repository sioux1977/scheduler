mkdir /opt/scheduler
cp * /opt/scheduler
cp scheduler.initd.sh /etc/init.d/scheduler
chmod 755 /etc/init.d/scheduler
chmod 755 /opt/scheduler/scheduler.sh
