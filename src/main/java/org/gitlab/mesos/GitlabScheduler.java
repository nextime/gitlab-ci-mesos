package org.gitlab.mesos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Filters;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.MasterInfo;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.gitlab.api.GitlabCI;
import org.gitlab.runner.GitlabSlave;
import org.gitlab.api.SlaveRequest;
import org.gitlab.api.SlaveTask;
import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public class GitlabScheduler implements Scheduler {

    // We allocate 10% more memory to the Mesos task to account for the JVM overhead.
    private static final double JVM_MEM_OVERHEAD_FACTOR = 0.1;

    private final Queue<Request> requests;
    private final Map<TaskID, Result> results;
    private volatile MesosSchedulerDriver driver;
    private final GitlabCI gitlabMaster;
    private final String mesosMaster;
    private final String cmdExec;
    private final String taskIdent = "gitlab-ci-";

    private static final Logger LOGGER = Logger.getLogger(GitlabScheduler.class.getName());

    public GitlabScheduler(GitlabCI gitlabMaster, String mesosMaster, String slaveCommand) {
        LOGGER.log(Level.INFO, "GitlabScheduler instantiated with gitlab {0} and mesos {1}", new Object[]{gitlabMaster, mesosMaster});

        this.gitlabMaster = gitlabMaster;
        this.mesosMaster = mesosMaster;
        this.cmdExec = slaveCommand;

        requests = new LinkedList<Request>();
        results = new HashMap<TaskID, Result>();
    }

    public synchronized void init() {
        // Start the framework.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Have Mesos fill in the current user.
                FrameworkInfo framework = FrameworkInfo.newBuilder().setUser("")
                        .setName("Gitlab CI Framework").build();

                driver = new MesosSchedulerDriver(GitlabScheduler.this, framework, mesosMaster);

                if (driver.run() != Status.DRIVER_STOPPED) {
                    LOGGER.severe("The mesos driver was aborted!");
                }

                driver = null;
            }
        }).start();
    }

    public synchronized void stop() {
        driver.stop();
    }

    public synchronized boolean isRunning() {
        return driver != null;
    }

    public void requestGitlabSlave(SlaveRequest request, SlaveTask result) {
        LOGGER.info("Enqueuing gitlab slave request");
        requests.add(new Request(request, result));
    }

    public void terminateGitlabSlave(String name) {
        LOGGER.log(Level.INFO, "Terminating gitlab slave {0}", name);

        TaskID taskId = TaskID.newBuilder().setValue(name).build();

        if (results.containsKey(taskId)) {
            LOGGER.log(Level.INFO, "Killing mesos task {0}", taskId);
            driver.killTask(taskId);
        } else {
            LOGGER.log(Level.WARNING, "Asked to kill unknown mesos task {0}", taskId);
        }
    }

    @Override
    public void registered(SchedulerDriver driver, FrameworkID frameworkId, MasterInfo masterInfo) {
        LOGGER.log(Level.INFO, "Framework registered! ID = {0}", frameworkId.getValue());
    }

    @Override
    public void reregistered(SchedulerDriver driver, MasterInfo masterInfo) {
        LOGGER.info("Framework re-registered");
    }

    @Override
    public void disconnected(SchedulerDriver driver) {
        LOGGER.info("Framework disconnected!");
    }

    @Override
    public void resourceOffers(SchedulerDriver driver, List<Offer> offers) {
        LOGGER.log(Level.INFO, "Received offers {0}", offers.size());
        // if we have resource offers, we check for avaiable tasks
        checkForNewTasks();
        for (Offer offer : offers) {
            boolean matched = false;
            for (Request request : requests) {
                if (matches(offer, request)) {
                    matched = true;
                    LOGGER.info("Offer matched! Creating mesos task");
                    try {
                        createMesosTask(offer, request);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    requests.remove(request);
                    break;
                }
            }

            if (!matched) {
                driver.declineOffer(offer.getId());
            }
        }
    }

    private void checkForNewTasks() {
        try {
            BuildInfo buildReq = gitlabMaster.getBuild();
            if (buildReq != null) {
                //yay! we have some work to do
                SlaveRequest request = new SlaveRequest(buildReq, 1, 512);
                requestGitlabSlave(request, new SlaveTask() {

                    @Override
                    public void running(GitlabSlave slave) {
                        LOGGER.log(Level.INFO, "running task ");
                    }

                    @Override
                    public void finished(GitlabSlave slave) {
                        LOGGER.log(Level.INFO, "finished task");
                    }

                    @Override
                    public void failed(GitlabSlave slave) {
                        LOGGER.log(Level.INFO, "task failed");
                    }
                });
            }
        } catch (IOException ex) {
            Logger.getLogger(GitlabScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean matches(Offer offer, Request request) {
        double cpus = -1;
        double mem = -1;

        for (Resource resource : offer.getResourcesList()) {
            if (resource.getName().equals("cpus")) {
                if (resource.getType().equals(Value.Type.SCALAR)) {
                    cpus = resource.getScalar().getValue();
                } else {
                    LOGGER.log(Level.SEVERE, "Cpus resource was not a scalar: {0}", resource.getType().toString());
                }
            } else if (resource.getName().equals("mem")) {
                if (resource.getType().equals(Value.Type.SCALAR)) {
                    mem = resource.getScalar().getValue();
                } else {
                    LOGGER.log(Level.SEVERE, "Mem resource was not a scalar: {0}", resource.getType().toString());
                }
            } else if (resource.getName().equals("disk")) {
                LOGGER.warning("Ignoring disk resources from offer");
            } else if (resource.getName().equals("ports")) {
                LOGGER.info("Ignoring ports resources from offer");
            } else {
                LOGGER.log(Level.WARNING, "Ignoring unknown resource type: {0}", resource.getName());
            }
        }

        if (cpus < 0) {
            LOGGER.severe("No cpus resource present");
        }
        if (mem < 0) {
            LOGGER.severe("No mem resource present");
        }

        // Check for sufficient cpu and memory resources in the offer.
        double requestedCpus = request.request.getCpus();
        double requestedMem = (1 + JVM_MEM_OVERHEAD_FACTOR) * request.request.getMem();

        if (requestedCpus <= cpus && requestedMem <= mem) {
            return true;
        } else {
            LOGGER.info(
                    "Offer not sufficient for slave request:\n"
                    + offer.getResourcesList().toString()
                    + "\nRequested for Gitlab slave:\n"
                    + "  cpus: " + requestedCpus + "\n"
                    + "  mem:  " + requestedMem);
            return false;
        }
    }

    private String taskCommand(BuildInfo info) throws IOException {
        StringBuilder sb = new StringBuilder(cmdExec);
        sb.append(" --task ").append(info.encode()).append(" --token ")
                .append(gitlabMaster.getConfig().getToken())
                .append(" --gitlab ").append(gitlabMaster.getConfig().baseUrl());
        return sb.toString();
    }

    private void createMesosTask(Offer offer, Request request) throws IOException {
        BuildInfo info = request.request.getInfo();
        //task identificator is used also for creating folders structure
        TaskID taskId = TaskID.newBuilder().setValue(taskIdent + info.projectId + "-" + info.sha).build();
        LOGGER.log(Level.INFO, "Launching task {0}", new Object[]{taskId.getValue()});
        String cmd = taskCommand(info);
        LOGGER.log(Level.CONFIG, "Task command {0}", new Object[]{cmd});
        TaskInfo task = TaskInfo
                .newBuilder()
                .setName("task " + taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(
                        Resource
                        .newBuilder()
                        .setName("cpus")
                        .setType(Value.Type.SCALAR)
                        .setScalar(
                                Value.Scalar.newBuilder()
                                .setValue(request.request.getCpus()).build()).build())
                .addResources(
                        Resource
                        .newBuilder()
                        .setName("mem")
                        .setType(Value.Type.SCALAR)
                        .setScalar(
                                Value.Scalar
                                .newBuilder()
                                .setValue((1 + JVM_MEM_OVERHEAD_FACTOR) * request.request.getMem())
                                .build()).build())
                .setCommand(
                        CommandInfo
                        .newBuilder()
                        .setValue(cmd)).build();

        List<TaskInfo> tasks = new ArrayList<TaskInfo>();
        tasks.add(task);
        Filters filters = Filters.newBuilder().setRefuseSeconds(1).build();
        driver.launchTasks(offer.getId(), tasks, filters);

        results.put(taskId, new Result(request.result, new GitlabSlave(offer.getSlaveId()
                .getValue())));
    }

    @Override
    public void offerRescinded(SchedulerDriver driver, OfferID offerId) {
        LOGGER.log(Level.INFO, "Rescinded offer {0}", offerId);
    }

    @Override
    public void statusUpdate(SchedulerDriver driver, TaskStatus status) {
        TaskID taskId = status.getTaskId();
        LOGGER.log(Level.INFO, "Status update: task {0} is in state {1}", new Object[]{taskId, status.getState()});

        if (!results.containsKey(taskId)) {
            throw new IllegalStateException("Unknown taskId: " + taskId);
        }

        Result result = results.get(taskId);

        switch (status.getState()) {
            case TASK_STAGING:
            case TASK_STARTING:
                break;
            case TASK_RUNNING:
                result.result.running(result.slave);
                break;
            case TASK_FINISHED:
                result.result.finished(result.slave);
                break;
            case TASK_FAILED:
            case TASK_KILLED:
            case TASK_LOST:
                result.result.failed(result.slave);
                break;
            default:
                throw new IllegalStateException("Invalid State: " + status.getState());
        }
    }

    @Override
    public void frameworkMessage(SchedulerDriver driver, ExecutorID executorId,
            SlaveID slaveId, byte[] data) {
        LOGGER.log(Level.INFO, "Received framework message from executor {0} of slave {1}", new Object[]{executorId, slaveId});
    }

    @Override
    public void slaveLost(SchedulerDriver driver, SlaveID slaveId) {
        LOGGER.log(Level.WARNING, "Slave {0} lost!", slaveId);
    }

    @Override
    public void executorLost(SchedulerDriver driver, ExecutorID executorId,
            SlaveID slaveId, int status) {
        LOGGER.log(Level.WARNING, "Executor {0} of slave {1} lost!", new Object[]{executorId, slaveId});
    }

    @Override
    public void error(SchedulerDriver driver, String message) {
        LOGGER.severe(message);
    }

    private class Result {

        private final SlaveTask result;
        private final GitlabSlave slave;

        private Result(SlaveTask result, GitlabSlave slave) {
            this.result = result;
            this.slave = slave;
        }
    }

    private class Request {

        private final SlaveRequest request;
        private final SlaveTask result;

        public Request(SlaveRequest request, SlaveTask result) {
            this.request = request;
            this.result = result;
        }
    }
}
