package org.gitlab.runner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.api.client.util.Charsets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 * @author Tomas Barton
 */
public class Utils {

    public static String readFile(String path, Charset encoding) throws IOException {
        return readFile(new File(path), encoding);
    }

    public static String readFile(File file, Charset encoding) throws IOException {
        InputStream in = new FileInputStream(file);
        byte[] b = new byte[(int) file.length()];
        int len = b.length;
        int total = 0;

        while (total < len) {
            int result = in.read(b, total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }

        return new String(b, Charsets.UTF_8);
    }

    /**
     * Replace Unix "~" with real path to user's home
     *
     * @param path
     * @return absolute path
     */
    public static String normalizePath(String path) {
        return path.replace("~", System.getProperty("user.home"));
    }

    public static void printUsage(String[] args, JCommander cmd) {
        if (args.length == 0) {
            StringBuilder sb = new StringBuilder();
            cmd.usage(sb);
            sb.append("\n").append("attributes marked with * are mandatory");
            System.out.println(sb);
            System.err.println("missing mandatory arguments");
            System.exit(0);
        }
        try {
            cmd.parse(args);
            /**
             * TODO validate values of parameters
             */
        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
            cmd.usage();
            System.exit(0);
        }
    }

}
