/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scheduler;

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
 * @author sfreddio
 */
public class Job extends TimerTask {
    private static final Logger log = Logger.getLogger(Job.class.getName());
    
    private String name;
    private Timer timer;
    private Process proc;
    private String cmdline;
    private long period;
    private boolean alivecheck;
    

    public Job(String name, String cmdline, long period, boolean alivecheck) {
        this.cmdline = cmdline;
        this.period = period;
        this.alivecheck = alivecheck;
        this.name = name;
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
    
}
