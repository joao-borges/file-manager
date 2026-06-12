package ca.joaoborges.filemanager.app;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import ca.joaoborges.filemanager.cli.OneshotRunner;
import ca.joaoborges.filemanager.operations.common.SpringUtils;

/**
 * File Manager - Spring Boot application entry point.
 *
 * Two modes:
 *   - Web mode (default): starts the embedded server, serves the React UI and REST API.
 *   - Oneshot mode: runs a single operation from a JSON payload and exits.
 *     Triggered by --oneshot=<json> or --oneshot-file=<path>. See docs/oneshot-cli.md.
 */
@SpringBootApplication
@ComponentScan("ca.joaoborges.filemanager")
public class FileManager {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((final Thread thread, final Throwable failure) -> {
            failure.printStackTrace();
        });

        if (OneshotRunner.isOneshot(args)) {
            // Reserve the original stdout exclusively for the JSON result, and
            // route Spring/Logback console output to stderr. Logback's
            // ConsoleAppender captures the current System.out at startup, so the
            // swap must happen before app.run() bootstraps logging.
            final PrintStream originalStdout = System.out;
            System.setOut(System.err);
            OneshotRunner.captureStdout(originalStdout);

            // Force non-web mode and silence the banner. Passed as command-line
            // args so they win over application.yml (which pins web mode to
            // servlet for the normal run).
            args = withOneshotOverrides(args);
        }

        final SpringApplication app = new SpringApplication(FileManager.class);
        app.setHeadless(true);

        SpringUtils.setApplicationContext(app.run(args));
    }

    private static String[] withOneshotOverrides(final String[] args) {
        final List<String> merged = new ArrayList<>(Arrays.asList(args));
        merged.add("--spring.main.web-application-type=none");
        merged.add("--spring.main.banner-mode=off");
        merged.add("--logging.level.root=WARN");
        return merged.toArray(new String[0]);
    }

}
