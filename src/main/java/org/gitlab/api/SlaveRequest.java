package org.gitlab.api;

import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public class SlaveRequest {

    private BuildInfo info;
    private final int cpus;
    private final int mem;

    public SlaveRequest(BuildInfo info, int cpus, int mem) {
        this.info = info;
        this.cpus = cpus;
        this.mem = mem;
    }

    public int getCpus() {
        return cpus;
    }

    public int getMem() {
        return mem;
    }

    public BuildInfo getInfo() {
        return info;
    }

}
