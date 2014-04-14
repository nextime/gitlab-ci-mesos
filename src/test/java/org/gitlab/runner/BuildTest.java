package org.gitlab.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.gitlab.api.GitlabConfig;
import org.gitlab.api.json.BuildInfo;
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
public class BuildTest {

    private Build subject;

    public BuildTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of run method, of class Build.
     */
    @Test
    public void testRun() {
    }

    /**
     * Test of addListener method, of class Build.
     */
    @Test
    public void testAddListener() {
    }

    /**
     * Test of getGitCmd method, of class Build.
     */
    @Test
    public void testGetGitCmd() {
    }

    /**
     * Test of cloneCmd method, of class Build.
     */
    @Test
    public void testCloneCmd() {
    }

    /**
     * Test of safeProjectName method, of class Build.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSafeProjectName() throws IOException {
        BuildInfo info = new BuildInfo();
        info.repoUrl = "http://gitlab-ci-token:fb3c220c40ec5f9bafa16ce4cbbf95@example.com/root/build-test.git";
        GitlabConfig conf = new GitlabConfigImpl("localhost");
        conf.setBuildDir("/tmp/test");
        subject = new Build(info, conf);
        assertEquals("build-test", subject.safeProjectName());
    }

    @Test
    public void testGetBuildProcess() throws IOException {
        BuildInfo info = new BuildInfo();
        info.repoUrl = "http://gitlab-ci-token:fb3c220c40ec5f9bafa16ce4cbbf95@example.com/root/build-test.git";
        GitlabConfig conf = new GitlabConfigImpl("localhost");
        conf.setBuildDir("/tmp/test");
        subject = new Build(info, conf);
        File temp = File.createTempFile("foo-bar", ".tmp");
        ProcessBuilder proc = subject.getBuildProcess(temp);
        assertEquals("sh", proc.command().get(0));
    }

    @Test
    public void testGetBuildProcessWithGivenUser() throws IOException {
        BuildInfo info = new BuildInfo();
        info.repoUrl = "http://gitlab-ci-token:fb3c220c40ec5f9bafa16ce4cbbf95@example.com/root/build-test.git";
        GitlabConfig conf = new GitlabConfigImpl("localhost");
        conf.setBuildDir("/tmp/test");
        conf.setUser("deploy");
        subject = new Build(info, conf);
        File temp = File.createTempFile("foo-bar", ".tmp");
        ProcessBuilder proc = subject.getBuildProcess(temp);
        assertEquals("su", proc.command().get(0));
        List<String> cmds = proc.command();
        assertEquals("deploy", cmds.get(cmds.size() - 1));

    }
}
