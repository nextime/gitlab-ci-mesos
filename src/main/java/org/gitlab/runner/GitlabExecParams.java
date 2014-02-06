package org.gitlab.runner;

import com.beust.jcommander.Parameter;

/**
 *
 * @author Tomas Barton
 */
public class GitlabExecParams {

    @Parameter(names = "--gitlab", description = "Gitlab CI coordinator URL", required = true)
    public String gitlab;

    @Parameter(names = "--token", description = "Gitlab CI token (must be already registered)", required = true)
    public String token;

    @Parameter(names = "--task", description = "serialized JSON task info", required = true)
    public String task;

    @Parameter(names = "--dir", description = "Path to build directory (default: ~/tmp/gitlab-runner)")
    public String tmpDir = "~/tmp/gitlab-runner";

}
