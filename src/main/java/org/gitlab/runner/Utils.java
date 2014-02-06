package org.gitlab.runner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Tomas Barton
 */
public class Utils {

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

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
