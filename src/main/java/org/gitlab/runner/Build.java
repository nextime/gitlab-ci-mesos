package org.gitlab.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gitlab.api.BuildListener;
import org.gitlab.api.State;
import org.gitlab.api.json.BuildInfo;

/**
 *
 * @author Tomas Barton
 */
public class Build implements Runnable {

    private final BuildInfo info;
    private final transient LinkedList<BuildListener> listeners = new LinkedList<BuildListener>();
    private static final Logger logger = Logger.getLogger(Build.class.getName());
    private State state = State.waiting;
    private File tmpDir;
    private File prjDir;
    private File repoDir;
    private String projName;
    private Long timeout = 7200l;

    public Build(BuildInfo info, File buildDir) {
        this.info = info;
        this.tmpDir = buildDir;
    }

    @Override
    public void run() {
        state = State.running;
        try {
            logger.log(Level.INFO, "starting build {0}", info.id);
            StringBuilder output = new StringBuilder();

            repoDir = new File(projectDir(), safeProjectName());
            File script = File.createTempFile("build", ".sh", projectDir());
            FileWriter wr = new FileWriter(script);
            String cmd = getGitCmd() + " && " + info.commands.replaceAll("\r|\n|\r\n", "\n");
            wr.write(cmd);
            wr.flush();
            wr.close();
            logger.log(Level.INFO, "executing: {0}", cmd);
            exec(script, output);
            state = State.success;
            logger.log(Level.INFO, "finished build {0}", info.id);
            fireFinished(output.toString());
            script.deleteOnExit();
        } catch (IOException ex) {
            state = State.failed;
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private int exec(File script, StringBuilder output) throws IOException, InterruptedException {
        ProcessBuilder buildProc = new ProcessBuilder("sh", "-x", "-e", script.getAbsolutePath());
        buildProc.directory(projectDir());
        buildProc.redirectErrorStream(true);

        Map<String, String> env = buildProc.environment();
        env.put("CI_SERVER", "yes");
        env.put("CI_BUILD_REF", info.sha);
        env.put("CI_BUILD_REF_NAME", info.ref);
        env.put("CI_BUILD_ID", String.valueOf(info.id));

        final Process proc = buildProc.start();
        Long start = System.currentTimeMillis();

        BufferedReader buf = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                proc.destroy();
            }
        }, (timeout * 1000));
        String s;
        while ((s = buf.readLine()) != null) {
            logger.log(Level.INFO, s);
            output.append(s).append('\n');

        }
        proc.waitFor();
        int ret = proc.exitValue();
        t.cancel();
        logger.log(Level.INFO, "done...{0}", (System.currentTimeMillis() - start));
        logger.log(Level.INFO, "result : {0}", ret);
        logger.log(Level.INFO, "Really Done...{0}", (System.currentTimeMillis() - start));

        buf.close();
        double end = (System.currentTimeMillis() - start) / 1000.0;
        logger.log(Level.INFO, "build {0} took {1} s. exited with {2}", new Object[]{info.id, end, ret});
        return ret;
    }

    private void fireFinished(String trace) {
        for (BuildListener l : listeners) {
            l.buildFinished(info, state, trace);
        }
    }

    public void addListener(BuildListener listener) {
        listeners.add(listener);
    }

    private File projectDir() {
        if (prjDir == null) {
            prjDir = new File(tmpDir, "project-" + info.id);
            if (!prjDir.exists()) {
                if (!prjDir.mkdir()) {
                    throw new RuntimeException("failed to create directory: " + prjDir.getAbsolutePath());
                }
            }
        }
        return prjDir;
    }

    public String getGitCmd() {
        if (repoDir.exists() && info.allowGitFetch) {
            return fetchCmd();
        }
        return cloneCmd();
    }

    public String cloneCmd() {
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(projectDir().getAbsolutePath())
                .append(" && git clone ").append(info.repoUrl).append(" ")
                .append(safeProjectName())
                .append(" && cd ").append(safeProjectName())
                .append(" && git checkout ").append(info.ref);
        return sb.toString();
    }

    public String fetchCmd() {
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(projectDir().getAbsolutePath())
                .append(" && cd ").append(safeProjectName())
                .append(" && git reset --hard")
                .append(" && git clean -f")
                .append(" && git fetch");
        return sb.toString();
    }

    /**
     *
     * @return project name that could be used as directory name
     */
    public String safeProjectName() {
        if (projName == null) {
            int slashPos = info.repoUrl.lastIndexOf('/');
            projName = info.repoUrl.substring(slashPos + 1, info.repoUrl.lastIndexOf('.'));
        }
        return projName;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
