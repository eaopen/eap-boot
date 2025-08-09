package org.openea.eap.module.message.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(MessageProperties.class)
@EnableScheduling
public class MessageConfiguration {
}
