package org.gitlab.runner;

import org.gitlab.api.json.BuildInfo;
import org.gitlab.api.State;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.SlaveComputer;
import org.gitlab.api.TaskListener;
import org.gitlab.mesos.Mesos;

/**
 *
 * @author Tomas Barton
 */
public class MesosLauncher {

    private BuildInfo info;
    private boolean completed = false;
    private StringBuffer output;
    private State state = State.waiting;
    private static final Logger logger = Logger.getLogger(MesosLauncher.class.getName());

    // in seconds
    private int timeout = 7200;

    public MesosLauncher(BuildInfo info) {
        this.info = info;
        logger.log(Level.INFO, "Constructing MesosComputerLauncher");
    }

    public void run(SlaveComputer computer, TaskListener listener) {
        state = State.running;

        logger.log(Level.INFO, "Launching slave {0}", computer.getName());

        PrintStream stream = listener.getLogger();

        // Get a handle to mesos.
        Mesos mesos = Mesos.getInstance();

        if (!mesos.isSchedulerRunning()) {
            logger.log(Level.SEVERE, "Not launching {0} because the Mesos "
                    + "Gitlab scheduler is not running", info.commands);
            return;
        }

    }

}
