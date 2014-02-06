package org.gitlab.mesos;

import org.gitlab.api.GitlabCI;
import org.gitlab.api.SlaveRequest;
import org.gitlab.api.SlaveTask;

/**
 *
 * @author Tomas Barton
 */
public abstract class Mesos {

    private static MesosImpl mesos;

    abstract public void startScheduler(GitlabCI gitlabMaster, String mesosMaster, String cmdExec);

    abstract public boolean isSchedulerRunning();

    abstract public void stopScheduler();

    /**
     * Starts a gitlab slave asynchronously in the mesos cluster.
     *
     * @param request slave request.
     * @param result  this callback will be called when the slave starts.
     */
    abstract public void startGitlabSlave(SlaveRequest request, SlaveTask result);

    /**
     * Stop a gitlab slave asynchronously in the mesos cluster.
     *
     * @param name gitlab slave.
     *
     */
    abstract public void stopGitlabSlave(String name);

    /**
     * @return the mesos implementation instance
     */
    public static synchronized Mesos getInstance() {
        if (mesos == null) {
            mesos = new MesosImpl();
        }
        return mesos;
    }

    public static class MesosImpl extends Mesos {

        @Override
        public synchronized void startScheduler(GitlabCI gitlabMaster, String mesosMaster, String cmdExec) {
            stopScheduler();
            scheduler = new GitlabScheduler(gitlabMaster, mesosMaster, cmdExec);
            scheduler.init();
        }

        @Override
        public synchronized boolean isSchedulerRunning() {
            return scheduler != null && scheduler.isRunning();
        }

        @Override
        public synchronized void stopScheduler() {
            if (scheduler != null) {
                scheduler.stop();
                scheduler = null;
            }
        }

        @Override
        public synchronized void startGitlabSlave(SlaveRequest request, SlaveTask result) {
            if (scheduler != null) {
                scheduler.requestGitlabSlave(request, result);
            }
        }

        @Override
        public synchronized void stopGitlabSlave(String name) {
            if (scheduler != null) {
                scheduler.terminateGitlabSlave(name);
            }
        }

        private GitlabScheduler scheduler;
    }
}
