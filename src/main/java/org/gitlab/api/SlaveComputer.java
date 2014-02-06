package org.gitlab.api;

/**
 *
 * @author Tomas Barton
 */
public interface SlaveComputer {

    public String getName();

    public String getCommand();

    public String getUrl(String master);

}
