/**
 * 
 */
package edu.asupoly.aspira.monitorservice;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.asupoly.aspira.AspiraSettings;
import edu.asupoly.aspira.dmp.DMPException;

/**
 * @author kevinagary
 *
 */
public final class MonitoringService {
    private static final int DEFAULT_MAX_TIMER_TASKS = 10;
    private static final int DEFAULT_INTERVAL = 10;  // in seconds
    private static final String PROPERTY_FILENAME = "properties/monitoringservice.properties";
    private static final String MAX_TIMERTASKS_KEY = "MaxTimerTasks";
    private static final String DEFAULT_INTERVAL_KEY = "DefaultTaskInterval";
    private static final String TASK_KEY_PREFIX = "MonitorTask";
    private static final String TASKINTERVAL_KEY_PREFIX = "TaskInterval";
    
    private Timer __timer;
    private HashMap<String, TimerTask> __tasks;
    private Properties __props;

    private static MonitoringService __theMonitoringService = null;
    private static Logger LOGGER = AspiraSettings.getAspiraLogger();
    
    public static MonitoringService getMonitoringService() {
        if (__theMonitoringService == null) {
            try {
                __theMonitoringService = new MonitoringService();
            } catch (Throwable t) {
                LOGGER.log(Level.INFO, "MonitorService unable to initialize");
                __theMonitoringService = null;
            }
        }
        return __theMonitoringService;
    }
    
    public String[] listTasks() {
        if (__tasks == null || __tasks.isEmpty()) return null;
        String[] rval = new String[0];
        if (__tasks != null) {
            Set<Map.Entry<String,TimerTask>> t = __tasks.entrySet();
            if (t != null) {
                Iterator<Map.Entry<String,TimerTask>> iter = t.iterator();
                rval = new String[t.size()];
                int index = 0;
                while (iter.hasNext() && index < t.size()) {
                    Map.Entry<String,TimerTask> next = iter.next();
                    rval[index++] = next.getKey() + " : " + next.getValue().toString();
                }
            }
        }
        return rval;
    }
    
    public boolean cancelTask(String taskName) {
        boolean rval = false;
        TimerTask tt = __tasks.get(taskName);
        if (tt != null && tt.cancel()) {
            // clear it out of our Map and the Timer task Queue
            __tasks.remove(taskName);
            __timer.purge();
            rval = true;
        }
        return rval;
    }
    
    public void shutdownService() throws DMPException {
        // Canceling the Timer gets rid of all tasks, allowing
        // the current one to complete.
        __timer.cancel();
        // If the singleton accessor is called again it will fire up another Timer
        MonitoringService.__theMonitoringService = null;
        LOGGER.log(Level.INFO, "Shutting down MonitorService");
    }
    
    /**
     * 
     */
    private MonitoringService() throws DMPException {
        __timer = new Timer();
        __tasks = new HashMap<String, TimerTask>();
        int defaultInterval = DEFAULT_INTERVAL; // all intervals in seconds
        int maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
        __props = new Properties();
        InputStreamReader  isr = null;
        try {
            isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
            //isr = new InputStreamReader(new FileInputStream(PROPERTY_FILENAME));
            __props.load(isr);
            defaultInterval = Integer.parseInt(__props.getProperty(DEFAULT_INTERVAL_KEY));
            maxTimerTasks = Integer.parseInt(__props.getProperty(MAX_TIMERTASKS_KEY));
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, 
                    "NFE Problem initializing MonitorService from properties, continuing with defaults\n" + nfe.getMessage());
            defaultInterval = DEFAULT_INTERVAL;
            maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
        } catch (NullPointerException npe) {
            if (__props == null) {
                npe.printStackTrace();
                throw new DMPException(npe);
            }
            LOGGER.log(Level.INFO, 
                    "NPE Problem initializing MonitorService from properties, continuing with defaults\n" + npe.getMessage());
            defaultInterval = DEFAULT_INTERVAL;
            maxTimerTasks   = DEFAULT_MAX_TIMER_TASKS;
        }
        catch (IOException ie) {
            LOGGER.log(Level.INFO, 
                    "IO Problem initializing MonitorService from properties, aborting\n" + ie.getMessage());
            throw new DMPException(ie);
        }
        catch (Throwable t1) {
            LOGGER.log(Level.INFO, 
                    "Unknown problem initializing MonitorService from properties, aborting\n" + t1.getMessage());
            throw new DMPException(t1);
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (Throwable t) {
                LOGGER.log(Level.INFO, 
                        "Unable to close properties input stream" + t.getMessage());
            }
        }

        // for each TimerTask indicated in the property, schedule it
        int i = 1;
        int interval = defaultInterval;
        String intervalProp = null;
        String taskClassName = __props.getProperty(TASK_KEY_PREFIX+i);
        AspiraTimerTask nextTask = null;
        while (i <= maxTimerTasks && taskClassName != null) {
            try {
                intervalProp = __props.getProperty(TASKINTERVAL_KEY_PREFIX+i);
                if (intervalProp != null) {
                    try {
                        interval = Integer.parseInt(intervalProp);
                    } catch (NumberFormatException nfe) {
                        interval = defaultInterval;
                        LOGGER.log(Level.INFO, 
                                "NFE initializing MonitorService task from properties, using default\n" + nfe.getMessage());
                    }
                } else { // no interval specified, use default
                    interval = defaultInterval;
                }
                // let's create a TimerTask of that class and start it
                Class<?> taskClass = Class.forName(taskClassName);
                nextTask = (AspiraTimerTask)taskClass.newInstance();
                if (nextTask.init(__props)) {
                    __tasks.put(TASK_KEY_PREFIX+i, nextTask);
                    // fire up the task 1 second from now and execute at fixed delay
                    __timer.schedule(nextTask, 1000L, interval*1000L); // repeat task in seconds
                    //__timer.scheduleAtFixedRate(nextTask, 1000L, interval*1000L); // repeat task in seconds
                    LOGGER.log(Level.INFO, 
                            "Created timer task " + (TASK_KEY_PREFIX+i) + " for task class " + taskClassName);
                } else {
                    LOGGER.log(Level.INFO, 
                            "1. Unable to initialize MonitorService task from properties, skipping " + taskClassName);
                }
            } catch (Throwable t) {
                // something prevented us from creating the task, skip it
                LOGGER.log(Level.INFO, 
                        "2. Unable to initialize MonitorService task from properties, skipping\n" + 
                                edu.asupoly.aspira.Aspira.stackToString(t));
            }
            i++;
            taskClassName = __props.getProperty(TASK_KEY_PREFIX+i);
        }
    }
}
