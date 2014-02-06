package org.gitlab.mesos;

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
public class GitlabMesosTest {

    public GitlabMesosTest() {
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
     * Test of main method, of class GitlabMesos.
     */
    @Test
    public void testMain() {
    }

    /**
     * Test of parseArguments method, of class GitlabMesos.
     */
    @Test
    public void testParseArguments() {
        String[] params = new String[]{
            "--master", "zk://foo:2171/mesos", "--token", "asfwe4gf64reav65vbavd", "--gitlab", "http://example.com/"
        };


        GitlabMesosCLIParams parse = GitlabMesos.parseArguments(params);
        assertEquals("zk://foo:2171/mesos", parse.master);
        assertEquals("asfwe4gf64reav65vbavd", parse.token);
        assertEquals("http://example.com/", parse.gitlab);

    }

}
