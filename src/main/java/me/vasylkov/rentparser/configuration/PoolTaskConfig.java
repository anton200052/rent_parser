package me.vasylkov.rentparser.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PoolTaskConfig {
    @Bean
    public ThreadPoolTaskExecutor tasksExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);   // до 4 вечных задач параллельно
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(0);  // очередь не нужна для вечных задач
        ex.setThreadNamePrefix("loop-");
        ex.initialize();
        return ex;
    }
}