package org.gitlab.api;

import org.gitlab.runner.GitlabSlave;

/**
 *
 * @author Tomas Barton
 */
public interface SlaveTask {

    void running(GitlabSlave slave);

    void finished(GitlabSlave slave);

    void failed(GitlabSlave slave);
}
