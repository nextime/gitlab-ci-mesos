package org.gitlab.runner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.BuildListener;
import org.gitlab.api.GitlabCI;
import org.gitlab.api.State;
import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public class Run implements Runnable, BuildListener {

    private final GitlabCI gitlabci;
    private static final Logger logger = Logger.getLogger(Run.class.getName());
    private final BuildInfo info;

    public Run(GitlabCI gitlabci, BuildInfo info) {
        this.gitlabci = gitlabci;
        this.info = info;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "starting build");
        Build build = new Build(info, gitlabci.getConfig());
        build.addListener(this);
        build.run();

    }

    @Override
    public void buildFinished(BuildInfo build, State state, String trace, int ret) {
        try {
            logger.log(Level.INFO, "pushing build {0}", build.id);
            gitlabci.pushBuild(build, state, trace);
            logger.log(Level.INFO, "build finished with code {0}", ret);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        System.exit(ret);
    }

}
