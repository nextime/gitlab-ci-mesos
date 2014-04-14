package org.gitlab.runner;

import com.beust.jcommander.JCommander;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.GitlabCI;
import org.gitlab.api.GitlabConfig;
import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author deric
 */
public class Exec {

    private static final Logger logger = Logger.getLogger(Exec.class.getName());
    protected final static JsonFactory factory = new JacksonFactory();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GitlabExecParams params = parseArguments(args);
        GitlabConfig config;
        GitlabCI ci;
        logger.log(Level.INFO, "task from CI: {0}", params.gitlab);
        try {
            config = new GitlabConfigImpl(params.gitlab);
            config.setBuildDir(params.tmpDir);
            config.setToken(params.token);
            config.setUser(params.user);
            ci = new GitlabCIv1(config);
            ci.init();

            execute(ci, params.task);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(1);
        }
    }

    /**
     * Java separates arguments by space, we have to encoded spaces, therefore
     * URL encoding for passing JSON objects
     *
     * @param task
     * @return
     * @throws IOException
     */
    protected static BuildInfo parseTask(String task) throws IOException {
        String decode = URLDecoder.decode(task, "UTF-8");
        JsonObjectParser parser = factory.createJsonObjectParser();
        return parser.parseAndClose(new StringReader(decode), BuildInfo.class);
    }

    protected static void execute(GitlabCI ci, String task) throws IOException {
        BuildInfo buildInfo = parseTask(task);
        logger.log(Level.INFO, "parsed task {0}", buildInfo.toPrettyString());
        new Thread(new Run(ci, buildInfo)).start();
    }

    private static GitlabExecParams parseArguments(String[] args) {
        GitlabExecParams params = new GitlabExecParams();
        JCommander cmd = new JCommander(params);
        Utils.printUsage(args, cmd);
        return params;
    }
}
