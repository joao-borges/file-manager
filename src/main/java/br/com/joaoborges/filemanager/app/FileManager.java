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

        // Check if running in web mode (default) or Swing mode
        boolean webMode = true;
        for (String arg : args) {
            if ("--swing".equals(arg)) {
                webMode = false;
                break;
            }
        }

        app.setHeadless(webMode);
        SpringUtils.setApplicationContext(app.run(args));
    }
}
