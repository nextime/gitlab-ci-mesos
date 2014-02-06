package org.gitlab.runner;

import com.beust.jcommander.Parameter;

/**
 *
 * @author Tomas Barton
 */
public class GitlabCLIParams {

    @Parameter(names = "--gitlab", description = "Gitlab CI coordinator URL", required = true)
    public String gitlab;

    @Parameter(names = "--token", description = "Gitlab CI next token (for new runner registration)", required = true)
    public String token;

    @Parameter(names = "--api", description = "Gitlab API version (v1, v2, v3, ...)")
    public String api = "v1";

    @Parameter(names = "--key", description = "Path to SSH key (default: ~/.ssh/id_rsa.pub)")
    public String key = "~/.ssh/id_rsa.pub";

    @Parameter(names = "--dir", description = "Path to build directory (default: ~/tmp/gitlab-runner)")
    public String tmpDir = "~/tmp/gitlab-runner";

}
