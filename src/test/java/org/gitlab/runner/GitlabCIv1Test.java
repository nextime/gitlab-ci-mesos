package org.gitlab.runner;

import com.google.api.client.http.GenericUrl;
import org.gitlab.api.json.BuildInfo;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import java.io.IOException;
import org.gitlab.api.GitlabConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author deric
 */
public class GitlabCIv1Test {

    private GitlabCIv1 subject;
    private static final String gitlabCI = "http://gitlab.example.com";
    private static final String token = "c05f03e1fab8ec751def";
    private static final String apiVersion = "v1";
    private static GitlabConfig conf;

    public GitlabCIv1Test() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        conf = new GitlabConfigImpl(gitlabCI, token, apiVersion, "~/.ssh/id_rsa");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class GitlabCIv1.
     */
    @Test
    public void testInit() throws Exception {
    }

    /**
     * Test of getNextToken method, of class GitlabCIv1.
     */
    @Test
    public void testGetToken() {
    }

    /**
     * Test of toString method, of class GitlabCI.
     */
    @Test
    public void testToString() {
        subject = new GitlabCIv1(conf);
        assertNotNull(subject.toString());
    }

    /**
     * Test of getKey method, of class GitlabCIv1.
     */
    @Test
    public void testGetKey() {
    }

    /**
     * Test of setKey method, of class GitlabCIv1.
     */
    @Test
    public void testSetKey() {
    }

    /**
     * Test of request method, of class GitlabCIv1.
     */
    /*   @Test(expected = HttpResponseException.class)
     public void testRequest() throws Exception {
     subject = new GitlabCIv1(conf);
     subject.request(gitlabCI + "/v1/runners", "");
     }*/
    private MockLowLevelHttpRequest forbidden() {
        return new MockLowLevelHttpRequest() {
            @Override
            public LowLevelHttpResponse execute() throws IOException {
                MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                result.setContentType(Json.MEDIA_TYPE);
                result.setContent("{\"message\":\"403 Forbidden\"}");
                return result;
            }
        };
    }

    private HttpTransport mockUrl(final String mockUrl, final String content) {
        return new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
                if (url.equals(conf.apiUrl() + mockUrl)) {
                    return new MockLowLevelHttpRequest() {
                        @Override
                        public LowLevelHttpResponse execute() throws IOException {
                            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
                            result.setContentType(Json.MEDIA_TYPE);
                            result.setContent(content);
                            return result;
                        }
                    };
                } else {
                    return forbidden();
                }
            }
        };
    }

    /**
     * Test of registerRunner method, of class GitlabCIv1.
     */
    @Test
    public void testRegisterRunner() throws Exception {
        final String registration = "{\"id\":1,\"token\":\"7fd780383447036873791dc4459431\"}";
        HttpTransport transport = mockUrl("/runners/register.json", registration);

        subject = new GitlabCIv1(conf, transport);
        subject.registerRunner();
        assertEquals("7fd780383447036873791dc4459431", subject.getToken());
    }

    /**
     * Test of content method, of class GitlabCIv1.
     */
    @Test
    public void testContent() throws Exception {
    }

    @Test
    public void testGetBuilds() throws Exception {
        String response = "{\"id\":2,\"commands\":\"ls -la\",\"ref\":\"master\",\"sha\":\"e9036dab984b4f913e8cd9980ac7d0e4c5523038\",\"project_id\":8,\"repo_url\":\"http://gitlab-ci-token:fb3c220c40ec5f9bafa16ce4cbbf95@gitlab.example.com/root/build-test.git\",\"before_sha\":\"0000000000000000000000000000000000000000\",\"timeout\":1800,\"allow_git_fetch\":true,\"project_name\":\"Administrator / build-test\"}";
        HttpTransport transport = mockUrl("/builds/register.json", response);

        subject = new GitlabCIv1(conf, transport);
        BuildInfo buildInfo = subject.getBuild();
        assertNotNull(buildInfo);
        System.out.println(buildInfo.toPrettyString());
        assertEquals(2, buildInfo.id);
        assertEquals(8, buildInfo.projectId);
        assertEquals(true, buildInfo.allowGitFetch);
    }

    /**
     * Test of post method, of class GitlabCIv1.
     */
    @Test
    public void testPost() {
    }

    /**
     * Test of request method, of class GitlabCIv1.
     */
    @Test
    public void testRequest() throws Exception {
    }

    /**
     * Test of getBuild method, of class GitlabCIv1.
     */
    @Test
    public void testGetBuild() throws Exception {
    }

    /**
     * Test of pushBuild method, of class GitlabCIv1.
     */
    @Test
    public void testPushBuild() throws Exception {
    }

    /**
     * Test of isRegistered method, of class GitlabCIv1.
     */
    @Test
    public void testIsRegistered() throws IOException {
        subject = new GitlabCIv1(conf);
        subject.init();
        assertEquals(true, subject.isRegistered());
    }

    /**
     * Test of getConfig method, of class GitlabCIv1.
     */
    @Test
    public void testGetConfig() {
    }

    /**
     * Test of buildUrl method, of class GitlabCIv1.
     */
    @Test
    public void testBuildUrl() {
        subject = new GitlabCIv1(conf);
        GenericUrl url = subject.buildUrl(13);
        System.out.println("url: " + url.toString());
        assertEquals("http://gitlab.example.com/api/v1/builds/13.json", url.toString());
    }

}
