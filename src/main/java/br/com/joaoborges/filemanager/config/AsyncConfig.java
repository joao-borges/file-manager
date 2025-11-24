package br.com.joaoborges.filemanager.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Async Configuration
 *
 * Configures Spring's async execution support for long-running file operations.
 * This allows file operations to run in separate threads, preventing blocking
 * of HTTP request threads and improving application responsiveness.
 *
 * Configuration includes:
 * - Thread pool sizing for concurrent operations
 * - Queue capacity for pending tasks
 * - Thread naming for easier debugging
 * - Exception handling for uncaught async errors
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configure the async executor
     *
     * Thread pool configuration:
     * - Core pool size: 5 threads (always running)
     * - Max pool size: 10 threads (can grow to this limit)
     * - Queue capacity: 100 tasks (pending tasks before rejection)
     * - Thread name prefix: "FileOps-" (for log identification)
     *
     * @return Configured executor for async operations
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - minimum number of threads
        executor.setCorePoolSize(5);

        // Maximum pool size - threads can grow to this number
        executor.setMaxPoolSize(10);

        // Queue capacity - how many tasks can wait
        executor.setQueueCapacity(100);

        // Thread name prefix for logging
        executor.setThreadNamePrefix("FileOps-");

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Timeout for shutdown (30 seconds)
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("Async executor configured: core={}, max={}, queue={}",
            executor.getCorePoolSize(),
            executor.getMaxPoolSize(),
            executor.getQueueCapacity());

        return executor;
    }

    /**
     * Handle uncaught exceptions in async methods
     *
     * @return Exception handler for async operations
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Uncaught async exception in method: {}", method.getName());
            log.error("Exception message: {}", throwable.getMessage());
            log.error("Method parameters: {}", (Object[]) params);
            log.error("Exception: ", throwable);
        };
    }
}
