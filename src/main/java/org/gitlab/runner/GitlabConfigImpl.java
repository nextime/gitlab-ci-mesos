package org.gitlab.runner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.gitlab.api.GitlabConfig;

/**
 *
 * @author Tomas Barton
 */
public class GitlabConfigImpl implements GitlabConfig {

    private String version = "v1";
    private String key;
    private String nextToken = null;
    private String apiUrl;
    private static final String configDir = ".gitlab-ci-runner-mesos";
    private File buildDir;
    private File configFile;
    private String url = "";
    private String token = "";

    public GitlabConfigImpl(String url) throws IOException {
        this.url = url;
        loadConfig();
    }

    public GitlabConfigImpl(String url, String token, String version, String key) throws IOException {
        this.url = url;
        this.nextToken = token;
        this.version = version;
        setKey(key);
        loadConfig();
    }

    @Override
    public String apiUrl() {
        if (apiUrl == null) {
            apiUrl = joinPaths(joinPaths(url, "api"), version);
        }
        return apiUrl;
    }

    /**
     * Token for new runner registration
     *
     * @return
     */
    @Override
    public String getNextToken() {
        return nextToken;
    }

    /**
     * Contents of a public key
     *
     * @return
     * @throws IOException
     */
    @Override
    public String getPublicKey() throws IOException {
        if (keyExists()) {
            return Utils.readFile(new File(key).getAbsolutePath(), Charset.defaultCharset());
        }
        return key;

    }

    public String getKey() {
        return key;
    }

    /**
     *
     * @param keyPath path to public key
     */
    public final void setKey(String keyPath) {
        this.key = Utils.normalizePath(keyPath);
    }

    private String joinPaths(String prefix, String suffix) {
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (suffix.startsWith("/")) {
            suffix = suffix.substring(1, suffix.length());
        }

        return prefix + '/' + suffix;
    }

    @Override
    public boolean keyExists() {
        File file = new File(key);
        return file.exists();
    }

    private void loadConfig() throws IOException {
        File dir = new File(System.getProperty("user.home"), configDir);
        configFile = new File(dir, "config.properties");
        if (configFile.exists()) {
            Properties props = new Properties();

            props.load(new FileReader(configFile));
            if (url == null) {
                url = props.getProperty("url");
            }
            token = props.getProperty("token");
        }
    }

    @Override
    public void saveConfig() throws IOException {
        configFile.getParentFile().mkdirs();
        Properties props = new Properties();

        props.setProperty("url", url);
        props.setProperty("token", token);
        props.store(new FileWriter(configFile), null);
    }

    @Override
    public boolean isConfigured() {
        return !url.isEmpty() && !token.isEmpty();
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getConfigPath() {
        return configFile.getAbsolutePath();
    }

    @Override
    public void setBuildDir(String path) throws IOException {
        this.buildDir = new File(Utils.normalizePath(path));
        if (!buildDir.exists()) {
            if (!buildDir.mkdirs()) {
                throw new IOException("failed to create temp folder for builds at " + buildDir.getAbsolutePath());
            }
        }
    }

    @Override
    public File getBuildDir() {
        return buildDir;
    }

    @Override
    public String baseUrl() {
        return this.url;
    }

}
