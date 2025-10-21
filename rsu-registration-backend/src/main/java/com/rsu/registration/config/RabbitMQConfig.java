package com.rsu.registration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * RabbitMQ Configuration for proper error handling
 * Configures connection factory to throw exceptions when connection fails
 */
@Configuration
@Slf4j
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    /**
     * Configure ConnectionFactory with proper error handling
     */
    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        
        // Important: Don't publish when connection is not confirmed
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        connectionFactory.setPublisherReturns(true);
        
        // Connection timeout
        connectionFactory.setConnectionTimeout(5000); // 5 seconds
        
        log.info("✅ RabbitMQ ConnectionFactory configured for {}:{}", host, port);
        
        return connectionFactory;
    }

    /**
     * Configure RabbitTemplate with mandatory flag to ensure errors are thrown
     */
    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        
        // CRITICAL: Set mandatory flag to true so exceptions are thrown on failure
        template.setMandatory(true);
        
        // Set return callback to log when message is returned (failed to route)
        template.setReturnsCallback(returned -> {
            log.error("❌ Message returned: {} - Reply: {}", 
                     returned.getMessage(), 
                     returned.getReplyText());
        });
        
        // Set confirm callback to detect connection issues
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("❌ Message not confirmed: {}", cause);
            }
        });
        
        // Configure retry template with exponential backoff
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Don't use retry template - we want immediate failure to trigger error handling
        // template.setRetryTemplate(retryTemplate);
        
        log.info("✅ RabbitTemplate configured with mandatory=true and publisher confirms");
        
        return template;
    }

    /**
     * JSON Message Converter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
