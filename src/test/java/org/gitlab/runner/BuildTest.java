package org.gitlab.runner;

import java.io.File;
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
     */
    @Test
    public void testSafeProjectName() {
        BuildInfo info = new BuildInfo();
        info.repoUrl = "http://gitlab-ci-token:fb3c220c40ec5f9bafa16ce4cbbf95@example.com/root/build-test.git";
        subject = new Build(info, new File("/tmp/test"));
        assertEquals("build-test", subject.safeProjectName());
    }

}
