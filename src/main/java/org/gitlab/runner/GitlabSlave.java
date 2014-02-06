package org.gitlab.runner;

import org.gitlab.api.SlaveComputer;

/**
 *
 * @author Tomas Barton
 */
public class GitlabSlave implements SlaveComputer {

    private final String name;

    public GitlabSlave(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getUrl(String master) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
