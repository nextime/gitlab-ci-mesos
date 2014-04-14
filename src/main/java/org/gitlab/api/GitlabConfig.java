package org.gitlab.api;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Tomas Barton
 */
public interface GitlabConfig {

    /**
     * True when have valid SSH key
     *
     * @return
     */
    public boolean keyExists();

    /**
     *
     * @return Gitlab CI URL
     */
    public String apiUrl();

    /**
     * URL without version suffix
     *
     * @return
     */
    public String baseUrl();

    /**
     *
     * @return token required for new runner registration
     */
    public String getNextToken();

    public String getPublicKey() throws IOException;

    public boolean isConfigured();

    public void saveConfig() throws IOException;

    public void setToken(String token);

    public String getConfigPath();

    public String getToken();

    /**
     * Directory to which repositories will be downloaded
     *
     * @return
     */
    public File getBuildDir();

    /**
     * Path to build directory which will be used for storing repositories
     *
     * @param path
     * @throws IOException
     */
    public void setBuildDir(String path) throws IOException;

    /**
     *
     * @return
     */
    public String getUser();

    /**
     * User account for running tasks
     *
     * @param user
     */
    public void setUser(String user);

}
