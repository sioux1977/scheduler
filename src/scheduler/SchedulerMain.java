/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * $Id: $
 * $Log: $
 *
 * @author sfreddio
 */
public class SchedulerMain {

    private static final String CONFIGFILE = "scheduler.conf";
    private static final Logger log = Logger.getLogger(SchedulerMain.class.getName());
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        Properties config = new Properties();
        loadConfig(config);
        setupLog(config);
        
        int jobs = Integer.parseInt(config.getProperty("jobs", "0"));
        if (jobs == 0) {
            log.warning("jobs properties is set to 0 or not exists in config file. Nothing to do.");
            System.exit(-1);
        }
        
        for (int i = 0; i < jobs; i++) {
            String prefix = "job."+i;
            
            String name = config.getProperty(prefix+".name", "job "+i);
            String cmd = config.getProperty(prefix+".cmd");
            Long period = Long.parseLong(config.getProperty(prefix+".period"));
            Boolean alivecheck = config.getProperty(prefix+".alivecheck", "0").compareToIgnoreCase("1") == 0;
            
            log.info("Creating Job "+i+" - name '"+name+"' period: "+period+"ms alivecheck:"+alivecheck+" cmd:"+cmd);
            Job j = new Job(name, cmd, period, alivecheck);
            j.start();
            log.info("Job "+i+" started");
        }
        
        log.info("Scheduler started, enetering infinite loop.");
        while (true) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ex) {
                log.warning("infinite loop sleep interrupted!!! ex:"+ex.getMessage());
            }
            log.info("scheduler loop.");
        }
    }

    private static void setupLog(Properties config) throws IOException {
        Logger global = Logger.getGlobal();
        for (Handler h: global.getHandlers()) {
            global.removeHandler(h);
        }
        Logger anon = Logger.getLogger("");
        for (Handler h: anon.getHandlers()) {
            anon.removeHandler(h);
        }
        FileHandler fh = new FileHandler(config.getProperty("log", "/var/log/scheduler.log"), true);
        fh.setLevel(Level.ALL);
        fh.setFormatter(new SimpleFormatter());
        global.addHandler(fh);
        global.setLevel(Level.ALL);
        anon.addHandler(fh);
        anon.setLevel(Level.ALL);
    }

    private static void loadConfig(Properties config) throws IOException {
        File f = new File("/etc/"+CONFIGFILE);
        if (!(f.exists() && f.canRead())) {
            f = new File("./"+CONFIGFILE);
        }
        if (f.exists() & f.canRead()) {
            config.load(new FileReader(f));
        } else {
            throw new IOException("Config file not found. Cannot continue.");
        }
    }
    
}
