package org.gitlab.runner;

import org.gitlab.api.json.BuildInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author deric
 */
public class ExecTest {

    public ExecTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class Exec.
     */
    @Test
    public void testMain() {
    }

    /**
     * Test of parseTask method, of class Exec.
     */
    @Test
    public void testParseTask() throws Exception {
        //BuildInfo info = Exec.parseTask("{\"allow_git_fetch\":false,\"commands\":\"ls -laF\\ndu -h\",\"id\":0,\"project_id\":5,\"timeout\":15}");

        BuildInfo info = Exec.parseTask("%7B%22allow_git_fetch%22%3Afalse%2C%22commands%22%3A%22ls+-laF%5Cndu+-h%22%2C%22id%22%3A0%2C%22project_id%22%3A5%2C%22timeout%22%3A15%7D");
        assertEquals(info.projectId, 5);
        assertEquals(info.timeout, 15);
        assertEquals(info.commands, "ls -laF\ndu -h");

    }

    /**
     * Test of execute method, of class Exec.
     */
    @Test
    public void testExecute() throws Exception {
    }

}
