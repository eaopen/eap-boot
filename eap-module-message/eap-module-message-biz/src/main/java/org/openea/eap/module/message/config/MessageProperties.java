package org.openea.eap.module.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "message")
public class MessageProperties {
    private QueueProperties queue = new QueueProperties();
    private RetryProperties retry = new RetryProperties();

    @Data
    public static class QueueProperties {
        private String type = "db"; // db 或 rabbitmq
    }

    @Data
    public static class RetryProperties {
        private int maxAttempts = 5;
        private String backoffStrategy = "EXPONENTIAL";
        private int backoffBaseSeconds = 60;
    }
}
