package org.gitlab.runner;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author deric
 */
public class UtilsTest {

    public UtilsTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readFile method, of class Utils.
     */
    @Test
    public void testReadFile() throws Exception {
        File file = File.createTempFile("test", ".tmp");
        FileWriter wr = new FileWriter(file);
        String content = "Hello World!!!";
        wr.write(content);
        wr.flush();
        wr.close();

        String read = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
        assertEquals(content, read);

        file.deleteOnExit();

    }

    /**
     * Test of normalizePath method, of class Utils.
     */
    @Test
    public void testNormalizePath() {
        String path = "~/tmp";
        String norm = Utils.normalizePath(path);
        assertEquals(false, norm.startsWith("~"));
    }

    /**
     * Test of printUsage method, of class Utils.
     */
    @Test
    public void testPrintUsage() {
    }

}
