package br.com.joaoborges.filemanager.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import br.com.joaoborges.filemanager.operations.common.SpringUtils;

@SpringBootApplication
@ComponentScan("br.com.joaoborges.filemanager")
public class FileManager {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> e.printStackTrace());

        SpringApplication app = new SpringApplication(FileManager.class);
        app.setHeadless(false);
        SpringUtils.setApplicationContext(app.run(args));
    }
}
