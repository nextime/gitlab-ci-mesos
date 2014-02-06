package org.gitlab.api;

import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public interface BuildListener {

    public void buildFinished(BuildInfo build, State state, String trace);

}
