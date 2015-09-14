rm -Rf scheduler_install
mkdir scheduler_install
cp -R dist/* scheduler_install/
cp src/scheduler.conf scheduler_install/scheduler.conf.example
cp src/scheduler.initd.sh scheduler_install/
cp src/scheduler.sh scheduler_install/
cp src/install.sh scheduler_install/
