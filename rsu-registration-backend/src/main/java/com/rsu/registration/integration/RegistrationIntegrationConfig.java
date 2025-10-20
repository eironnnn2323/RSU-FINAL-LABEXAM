package com.rsu.registration.integration;

import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.service.StudentRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * Spring Integration Configuration for Student Registration
 * Sets up message channels and integration flows using RabbitMQ
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RegistrationIntegrationConfig {

    public static final String REGISTRATION_QUEUE = "student.registration.queue";
    public static final String REGISTRATION_EXCHANGE = "student.registration.exchange";
    public static final String REGISTRATION_ROUTING_KEY = "student.registration.*";
    public static final String REGISTRATION_INPUT_CHANNEL = "registrationInputChannel";
    public static final String REGISTRATION_SERVICE_CHANNEL = "registrationServiceChannel";

    private final StudentRegistrationService registrationService;

    /**
     * Declare the queue for student registrations
     */
    @Bean
    public Queue registrationQueue() {
        log.info("Creating registration queue: {}", REGISTRATION_QUEUE);
        return new Queue(REGISTRATION_QUEUE, true); // durable queue
    }

    /**
     * Declare the topic exchange
     */
    @Bean
    public TopicExchange registrationExchange() {
        log.info("Creating registration exchange: {}", REGISTRATION_EXCHANGE);
        return new TopicExchange(REGISTRATION_EXCHANGE, true, false);
    }

    /**
     * Bind the queue to the exchange
     */
    @Bean
    public Binding registrationBinding(Queue registrationQueue, TopicExchange registrationExchange) {
        log.info("Binding queue {} to exchange {}", REGISTRATION_QUEUE, REGISTRATION_EXCHANGE);
        return BindingBuilder.bind(registrationQueue)
                .to(registrationExchange)
                .with(REGISTRATION_ROUTING_KEY);
    }

    /**
     * Input channel for receiving messages from RabbitMQ
     */
    @Bean(name = REGISTRATION_INPUT_CHANNEL)
    public MessageChannel registrationInputChannel() {
        log.info("Creating registration input channel: {}", REGISTRATION_INPUT_CHANNEL);
        return new DirectChannel();
    }

    /**
     * Service channel for processing registrations
     */
    @Bean(name = REGISTRATION_SERVICE_CHANNEL)
    public MessageChannel registrationServiceChannel() {
        log.info("Creating registration service channel: {}", REGISTRATION_SERVICE_CHANNEL);
        return new DirectChannel();
    }

    /**
     * Inbound adapter that listens to RabbitMQ queue
     */
    @Bean
    public AmqpInboundChannelAdapter inboundAdapter(ConnectionFactory connectionFactory) {
        log.info("Creating AMQP inbound channel adapter");
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(
                new org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer(connectionFactory)
        );
        adapter.setQueue(REGISTRATION_QUEUE);
        adapter.setOutputChannel(registrationInputChannel());
        return adapter;
    }

    /**
     * Service activator that processes registration messages
     * This is the core of the integration pattern - receives messages and processes them
     */
    @ServiceActivator(inputChannel = REGISTRATION_INPUT_CHANNEL, outputChannel = REGISTRATION_SERVICE_CHANNEL)
    public void processRegistration(@Payload StudentRegistrationDTO registrationDTO) {
        try {
            log.info("Processing registration for student: {}", registrationDTO.getStudentId());

            // Save to database (EIP Pattern: Message Handler)
            var savedRegistration = registrationService.saveRegistration(registrationDTO);

            log.info("Successfully processed registration with ID: {}", savedRegistration.getId());
        } catch (Exception e) {
            log.error("Error processing registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process registration", e);
        }
    }
}
