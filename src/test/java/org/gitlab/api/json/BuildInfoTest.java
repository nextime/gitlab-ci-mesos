package org.gitlab.api.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author deric
 */
public class BuildInfoTest {

    public BuildInfoTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of encode method, of class BuildInfo.
     */
    @Test
    public void testEncode() throws Exception {
        BuildInfo subject = new BuildInfo();
        subject.projectId = 5;
        subject.timeout = 15;
        subject.commands = "ls -laF\ndu -h";
        System.out.println("enc: " + subject.encode());
    }

}
