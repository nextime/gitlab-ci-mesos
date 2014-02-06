package org.gitlab.api.json;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author Tomas Barton
 */
public class BuildInfo extends GenericJson {

    private static JsonFactory JSON_FACTORY = new JacksonFactory();

    @Key("id")
    public int id;

    @Key("ref")
    public String ref;

    @Key("sha")
    public String sha;

    @Key("project_id")
    public int projectId;

    @Key("commands")
    public String commands;

    @Key("repo_url")
    public String repoUrl;

    @Key("before_sha")
    public String beforeSha;

    @Key("timeout")
    public int timeout;

    @Key("allow_git_fetch")
    public boolean allowGitFetch;

    @Key("project_name")
    public String projectName;

    /**
     * We need to make sure that there won't be any single, double quotes or
     * spaces in that string because is passed to Bash script and then to Java
     *
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String encode() throws UnsupportedEncodingException, IOException {
        Gson gson = new Gson();
        String g = gson.toJson(this);
        return URLEncoder.encode(g, "UTF-8");
    }
}
