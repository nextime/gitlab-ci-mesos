package org.gitlab.runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author deric
 */
public class GitlabConfigImplTest {

    private static final String gitlabCI = "http://gitlab.example.org";
    private static final String token = "df45s4fsd4dsfab8ec751def";
    private static final String apiVersion = "v1";
    private static GitlabConfigImpl subject;

    public GitlabConfigImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        subject = new GitlabConfigImpl(gitlabCI, token, apiVersion, "~/.ssh/id_rsa");
    }

    @After
    public void tearDown() {
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test of keyExists method, of class GitlabCI.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testKeyExists() throws IOException {
        final File file1 = folder.newFile("key.pub");
        subject.setKey(file1.getAbsolutePath());
        assertEquals(true, subject.keyExists());
    }

    /**
     * Test of apiUrl method, of class GitlabCI.
     */
    @Test
    public void testApiUrl() {
        assertEquals(gitlabCI + "/api/" + apiVersion, subject.apiUrl());
    }

    /**
     * Test of getKey method, of class GitlabCI.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetPublicKey() throws Exception {
        final File file1 = folder.newFile("somekey.pub");
        String key = "ssh-rsa AAAAB3NzaC1yc2E foo@bar";

        PrintWriter writer = new PrintWriter(file1, "UTF-8");
        writer.print(key);
        writer.close();

        subject.setKey(file1.getAbsolutePath());
        assertEquals(key, subject.getPublicKey());
    }

}
