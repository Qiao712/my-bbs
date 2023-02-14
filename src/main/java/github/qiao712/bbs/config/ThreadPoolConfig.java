package github.qiao712.bbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置线程池
 */
@Configuration
public class ThreadPoolConfig{
    @Bean
    public Executor globalThreadPool() {
        int processors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                processors,
                processors * 2,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(processors * 100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
