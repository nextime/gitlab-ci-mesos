package org.gitlab.runner;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.GitlabCI;
import org.gitlab.api.GitlabConfig;
import org.gitlab.api.State;
import org.gitlab.api.json.BuildInfo;
import org.gitlab.api.json.FetchBuilds;
import org.gitlab.api.json.PushBuild;
import org.gitlab.api.json.Registration;
import org.gitlab.api.json.RegistrationToken;

/**
 *
 * @author Tomas Barton
 */
public class GitlabCIv1 implements GitlabCI {

    private static JsonFactory JSON_FACTORY = new JacksonFactory();
    private HttpRequestFactory requestFactory;
    private GenericUrl registerUrl;
    private GenericUrl fetchBuildsUrl;
    private final GitlabConfig config;
    private static final Logger logger = Logger.getLogger(GitlabCIv1.class.getName());

    public GitlabCIv1(GitlabConfig config) {
        this.config = config;
        setup(new NetHttpTransport());
    }

    /**
     * Constructor for tests
     *
     * @param config
     * @param transport class to mock
     */
    public GitlabCIv1(GitlabConfig config, HttpTransport transport) {
        this.config = config;
        setup(transport);
    }

    private void setup(HttpTransport transport) {
        requestFactory = transport
                .createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        registerUrl = new GenericUrl(config.apiUrl() + "/runners/register.json");
        fetchBuildsUrl = new GenericUrl(config.apiUrl() + "/builds/register.json");
    }

    @Override
    public void init() throws IOException {
        if (!isRegistered()) {
            registerRunner();
        }
    }

    public HttpResponse request(String reqUrl, String content) throws IOException {
        GenericUrl url = new GenericUrl(reqUrl);

        HttpRequest request = requestFactory.buildGetRequest(url);
        return request.execute();
    }

    @Override
    public void registerRunner() throws IOException {
        Registration reg = new Registration();
        reg.token = config.getNextToken();
        reg.publicKey = config.getPublicKey();
        JsonHttpContent content = new JsonHttpContent(JSON_FACTORY, reg);
        HttpRequest request = requestFactory.buildPostRequest(registerUrl, content);
        logger.log(Level.INFO, "trying to register runner at {0}", registerUrl.toString());
        RegistrationToken regToken = null;
        try {
            HttpResponse response = request.execute();
            if (response.isSuccessStatusCode()) {
                //content(response);
                regToken = response.parseAs(RegistrationToken.class);
            } else {
                logger.log(Level.SEVERE, "failed to register as gilab runner at {0}", registerUrl.toString());
            }
        } catch (HttpResponseException ex) {
            logger.log(Level.SEVERE, "failed to register gilab runner  with token = '{2}' at {0}, "
                    + "because of {1}", new Object[]{registerUrl.toString(), ex.getMessage(), reg.token});
        }
        if (regToken == null || regToken.id == 0) {
            throw new IOException("failed to register gilab runner");
        }
        config.setToken(regToken.token);
        logger.log(Level.INFO, "registered runner with ID = {0}", regToken.id);
        config.saveConfig();
    }

    /**
     * If no builds available, request will return 404. Otherwise precisely one
     * build is assigned to this worker
     *
     * @return build information | null
     * @throws IOException
     */
    @Override
    public BuildInfo getBuild() throws IOException {
        FetchBuilds fetchBuilds = new FetchBuilds();
        fetchBuilds.token = this.getToken();
        logger.log(Level.FINER, "getting builds at {0} token = {1}", new Object[]{fetchBuildsUrl.toString(), fetchBuilds.token});
        JsonHttpContent content = new JsonHttpContent(JSON_FACTORY, fetchBuilds);
        HttpRequest request = requestFactory.buildPostRequest(fetchBuildsUrl, content);
        BuildInfo buildInfo = null;
        try {
            HttpResponse response = request.execute();
            buildInfo = response.parseAs(BuildInfo.class);
        } catch (HttpResponseException ex) {
            if (ex.getStatusCode() == 404) {
                // 404 means that no build is available
                logger.log(Level.FINEST, "no builds available");
                return null;
            }
            throw new IOException(ex.getMessage());
        }
        if (buildInfo.id == 0) {
            throw new IOException("got empty build info");
        }
        return buildInfo;
    }

    protected GenericUrl buildUrl(int id) {
        return new GenericUrl(config.apiUrl() + "/builds/" + id + ".json");
    }

    @Override
    public void pushBuild(BuildInfo info, State state, String trace) throws IOException {
        Date current = new Date();
        logger.log(Level.INFO, "{0} submitting build {1} at state {2}",
                   new Object[]{current.toString(), info.id, state});

        PushBuild pushBuild = new PushBuild();
        pushBuild.state = state.toString();
        pushBuild.token = getToken();
        pushBuild.trace = trace;

        GenericUrl url = buildUrl(info.id);
        JsonHttpContent content = new JsonHttpContent(JSON_FACTORY, pushBuild);
        logger.log(Level.INFO, "pusing to {0}", url.toString());
        HttpRequest request = requestFactory.buildPutRequest(url, content);

        // should return 200 OK - otherwise exception will be thrown
        request.execute();
        //        content(response);
    }

    public void content(HttpResponse response) throws IOException {
        //Result result = response.parseAs(Result.class);

        System.out.println(response.getStatusCode());
        System.out.println(response.getStatusMessage());
        System.out.println(response.getContentType());

        InputStream is = response.getContent();
        int ch;
        while ((ch = is.read()) != -1) {
            System.out.print((char) ch);
        }
        response.disconnect();
    }

    @Override
    public boolean isRegistered() {
        if (config == null) {
            return false;
        }
        return config.isConfigured();
    }

    public String getToken() {
        if (!isRegistered()) {
            try {
                registerRunner();
            } catch (IOException ex) {
                Logger.getLogger(GitlabCIv1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return config.getToken();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GitlabCI: (");
        sb.append(config.apiUrl()).append(")");
        return sb.toString();
    }

    @Override
    public GitlabConfig getConfig() {
        return config;
    }
}
