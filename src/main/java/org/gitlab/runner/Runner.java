package org.gitlab.runner;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.BuildListener;
import org.gitlab.api.GitlabCI;
import org.gitlab.api.GitlabConfig;
import org.gitlab.api.State;
import org.gitlab.api.json.BuildInfo;

/**
 * Runner should be run as a daemon which periodically checks for new builds at
 * GitLab CI API
 *
 * @author Tomas Barton
 */
public class Runner implements Runnable, BuildListener {

    private final GitlabCI gitlabci;
    private boolean running = true;
    private long sleepTime = 3000;
    private static final Logger logger = Logger.getLogger(Runner.class.getName());

    public Runner(GitlabCI gitlabci) {
        this.gitlabci = gitlabci;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Runner started");
        BuildInfo current;
        while (running) {

            try {
                current = gitlabci.getBuild();
                if (current != null) {
                    Build build = new Build(current, gitlabci.getConfig().getBuildDir());
                    build.addListener(this);
                    new Thread(build).start();
                }
                Thread.sleep(sleepTime);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        logger.log(Level.INFO, "Runner finished.");
    }

    public void stop() {
        running = false;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public void buildFinished(BuildInfo build, State state, String trace) {
        try {
            logger.log(Level.INFO, "pushing build {0}", build.id);
            gitlabci.pushBuild(build, state, trace);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GitlabCLIParams params = parseArguments(args);
        GitlabConfig config;
        GitlabCI ci;
        try {
            config = new GitlabConfigImpl(params.gitlab, params.token, params.api, params.key);
            config.setBuildDir(params.tmpDir);
            ci = new GitlabCIv1(config);
            if (!config.keyExists()) {
                System.err.println("key " + params.key + " does not exist");
                System.exit(1);
            }

            ci.init();
            //start inifinite loop
            new Thread(new Runner(ci)).start();

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(1);
        }

    }

    private static GitlabCLIParams parseArguments(String[] args) {
        GitlabCLIParams params = new GitlabCLIParams();
        JCommander cmd = new JCommander(params);
        Utils.printUsage(args, cmd);
        return params;
    }

}
