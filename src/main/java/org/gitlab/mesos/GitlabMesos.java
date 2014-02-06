package org.gitlab.mesos;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.GitlabCI;
import org.gitlab.api.GitlabConfig;
import org.gitlab.runner.GitlabCIv1;
import org.gitlab.runner.GitlabConfigImpl;
import org.gitlab.runner.Utils;

/**
 * GitLab CI framework for Mesos
 *
 * @author deric
 */
public class GitlabMesos {

    private static final Logger logger = Logger.getLogger(GitlabMesos.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GitlabMesosCLIParams params = parseArguments(args);
        GitlabConfig config;
        GitlabCI ci = null;
        try {
            logger.log(Level.INFO, "loading config");
            config = new GitlabConfigImpl(params.gitlab, params.token, params.api, params.key);
            config.setBuildDir(params.tmpDir);
            ci = new GitlabCIv1(config);
            if (!config.keyExists()) {
                System.err.println("key " + params.key + " does not exist");
                System.exit(1);
            }

            logger.log(Level.INFO, "runner registered? {0}", ci.isRegistered());
            ci.init();

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            System.err.print(ex.getMessage());
            System.exit(1);
        }

        if (ci != null) {
            GitlabScheduler scheduler = new GitlabScheduler(ci, params.master, params.execCmd);
            //start mesos framework
            scheduler.init();
        } else {
            System.err.println("failed to initialize Gitlab CI connection");
            System.exit(1);
        }
    }

    protected static GitlabMesosCLIParams parseArguments(String[] args) {
        GitlabMesosCLIParams params = new GitlabMesosCLIParams();
        JCommander cmd = new JCommander(params);
        Utils.printUsage(args, cmd);
        return params;
    }

}
