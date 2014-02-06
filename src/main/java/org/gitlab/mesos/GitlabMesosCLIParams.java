package org.gitlab.mesos;

import com.beust.jcommander.Parameter;
import org.gitlab.runner.GitlabCLIParams;

/**
 *
 * @author Tomas Barton
 */
public class GitlabMesosCLIParams extends GitlabCLIParams {

    @Parameter(names = "--master", description = "Mesos master URL", required = true, validateWith = MasterValidator.class)
    public String master;

    @Parameter(names = "--exec-cmd", description = "Command for task execution")
    public String execCmd = "gitlab-ci-exec";

}
