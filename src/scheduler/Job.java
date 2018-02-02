/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * $Id: $
 * $Log: $
 *
 * @author Simone Freddio <sioux1977@gmail.com>
 */
public class Job extends TimerTask {
    private static final Logger log = Logger.getLogger(Job.class.getName());
    
    private String name;
    private Timer timer;
    private Process proc;
    private String cmdline;
    private long period;
    private boolean alivecheck;
    private final String logfile;
    private long logfileLastSize = -1;
    private long logfileLastSizeTimestamp = -1;
    private final long logfileCheckPeriod;
    private final String auxiliaryKillCmd;
    

    public Job(String name, String cmdline, long period, boolean alivecheck, String logfile, long logfileCheckPeriod, String auxiliaryKillCmd) {
        this.cmdline = cmdline;
        this.period = period;
        this.alivecheck = alivecheck;
        this.name = name;
        this.logfile = logfile;
        this.logfileCheckPeriod = logfileCheckPeriod;
        this.auxiliaryKillCmd = auxiliaryKillCmd;
    }
    
    public void start() {
        log.info("Job "+name+" Starting timer...");
        timer = new Timer(name, true);
        timer.schedule(this, period, period);
        log.info("Job "+name+" Timer started.");
    }

    @Override
    public void run() {
        
        Integer exitcode = null;
        
        if (proc != null) {
            
            try {
                exitcode = proc.exitValue();
                log.fine("Job "+name+" process is dead, exitcode: "+exitcode);
            } catch (IllegalThreadStateException ex) {
                log.fine("Job "+name+" process still running...");
                if (logfile != null) {
                    log.fine("Job "+name+" checking log file size");
                    try {
                        if (checkLogFileGrowing()) {
                            log.fine("Job "+name+" log file is growing, process is alive and running");
                        } else {
                            log.warning("Job "+name+" log file is NOT growing, killing hanged process");
                            proc.destroy();
                            if (auxiliaryKillCmd != null) {
                                log.fine("Job "+name+": launching auxiliary kill");
                                try {
                                    auxiliaryKill();
                                } catch (IOException ex1) {
                                    log.log(Level.WARNING, "Job "+name+": exception during auxiliary kill", ex);
                                }
                            }
                            exitcode = -9999;
                            log.fine("Job "+name+" process destoyed forcibly");
                        }
                    } catch (FileNotFoundException ex1) {
                        log.warning("Job "+name+": log file not found");
                    }
                }
            }
            
            if (alivecheck && exitcode == null) {
                log.info("Job "+name+" process schedule delayed.");
            } else {
                log.fine("Job "+name+" Starting new process...");
                try {
                    startJob();
                } catch (IOException ex) {
                    log.warning("Job "+name+" Exception during job start: "+ex.getMessage());
                }
            }
            
        } else {
            log.fine("Job "+name+" Starting new process...");
            try {
                startJob();
            } catch (IOException ex) {
                log.warning("Job "+name+" Exception during job start: "+ex.getMessage());
            }
        }
        
    }

    private void startJob() throws IOException {
        Runtime rt = Runtime.getRuntime();
        String cmd[] = new String[] {
            "/bin/bash",
            "-c",
            cmdline
        };
        proc = rt.exec(cmd);
    }
    
    private void auxiliaryKill() throws IOException {
        Runtime rt = Runtime.getRuntime();
        String cmd[] = new String[] {
            "/bin/bash",
            "-c",
            auxiliaryKillCmd
        };
        proc = rt.exec(cmd);
    }

    private boolean checkLogFileGrowing() throws FileNotFoundException {
        File f = new File(logfile);
        if (f.exists()) {
            long fileSize = f.length();
            log.fine("Job "+name+": log file size is "+fileSize);
            if (logfileLastSize == -1) {
                logfileLastSize = fileSize;
                logfileLastSizeTimestamp = System.currentTimeMillis();
            }
            if (logfileLastSize == fileSize) {
                long elapsed = System.currentTimeMillis() - logfileLastSizeTimestamp;
                if (elapsed > logfileCheckPeriod) {
                    // log file is not growing
                    return false;
                }
            } else {
                // log file is growing, update saved size and timestamp
                logfileLastSize = fileSize;
                logfileLastSizeTimestamp = System.currentTimeMillis();
            }
            return true;
        } else {
            throw new FileNotFoundException("file "+logfile+" didn't exists");
        }
    }
    
}
