package org.gitlab.api;

import java.io.IOException;
import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public interface GitlabCI {

    /**
     * Initialize connection to GitlabCI API
     *
     * @throws IOException
     */
    public void init() throws IOException;

    /**
     * Register Gitlab runner at CI API
     *
     * @throws IOException
     */
    public void registerRunner() throws IOException;

    public boolean isRegistered();

    /**
     * Checks for new build
     *
     * @return
     * @throws IOException
     */
    public BuildInfo getBuild() throws IOException;

    /**
     * When build finished update its state at API
     *
     * @param info
     * @param state
     * @param trace
     * @throws IOException
     */
    public void pushBuild(BuildInfo info, State state, String trace) throws IOException;

    /**
     *
     * @return GitLab configuration
     */
    public GitlabConfig getConfig();

}
