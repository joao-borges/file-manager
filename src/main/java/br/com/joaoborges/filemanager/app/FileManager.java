package br.com.joaoborges.filemanager.app;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import br.com.joaoborges.filemanager.cli.OneshotRunner;
import br.com.joaoborges.filemanager.operations.common.SpringUtils;

/**
 * File Manager - Spring Boot application entry point.
 *
 * Two modes:
 *   - Web mode (default): starts the embedded server, serves the React UI and REST API.
 *   - Oneshot mode: runs a single operation from a JSON payload and exits.
 *     Triggered by --oneshot=<json> or --oneshot-file=<path>. See docs/oneshot-cli.md.
 */
@SpringBootApplication
@ComponentScan("br.com.joaoborges.filemanager")
public class FileManager {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        if (OneshotRunner.isOneshot(args)) {
            // Reserve the original stdout exclusively for the JSON result, and
            // route Spring/Logback console output to stderr. Logback's
            // ConsoleAppender captures the current System.out at startup, so the
            // swap must happen before app.run() bootstraps logging.
            PrintStream originalStdout = System.out;
            System.setOut(System.err);
            OneshotRunner.captureStdout(originalStdout);

            // Force non-web mode and silence the banner. Passed as command-line
            // args so they win over application.yml (which pins web mode to
            // servlet for the normal run).
            args = withOneshotOverrides(args);
        }

        SpringApplication app = new SpringApplication(FileManager.class);
        app.setHeadless(true);

        SpringUtils.setApplicationContext(app.run(args));
    }

    private static String[] withOneshotOverrides(String[] args) {
        List<String> merged = new ArrayList<>(args.length + 3);
        for (String a : args) {
            merged.add(a);
        }
        merged.add("--spring.main.web-application-type=none");
        merged.add("--spring.main.banner-mode=off");
        merged.add("--logging.level.root=WARN");
        return merged.toArray(new String[0]);
    }
}
