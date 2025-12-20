package dev.lhs.charity_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình async processing cho Evidence Verification
 * Hỗ trợ xử lý song song nhiều request (200-300 requests)
 * Cũng enable scheduling cho các scheduled tasks (AuctionActivationScheduler)
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Bean(name = "evidenceVerificationExecutor")
    public Executor evidenceVerificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Số thread cố định
        executor.setMaxPoolSize(50); // Số thread tối đa khi có nhiều request
        executor.setQueueCapacity(200); // Hàng đợi tối đa
        executor.setThreadNamePrefix("evidence-verification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

