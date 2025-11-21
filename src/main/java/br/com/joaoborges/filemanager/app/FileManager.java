package br.com.joaoborges.filemanager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import br.com.joaoborges.filemanager.operations.common.SpringUtils;

/**
 * File Manager - Spring Boot Web Application
 *
 * Modern full-stack web application for file management operations.
 * Provides REST API endpoints and serves a React + TypeScript frontend.
 *
 * Access the application at http://localhost:8080 after starting.
 */
@SpringBootApplication
@ComponentScan("br.com.joaoborges.filemanager")
public class FileManager {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        SpringApplication app = new SpringApplication(FileManager.class);

        // Always run as web application (headless mode)
        app.setHeadless(true);

        SpringUtils.setApplicationContext(app.run(args));
    }
}
