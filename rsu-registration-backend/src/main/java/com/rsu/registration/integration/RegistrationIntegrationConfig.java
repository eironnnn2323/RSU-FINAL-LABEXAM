package com.rsu.registration.integration;

import com.rsu.registration.dto.StudentRegistrationDTO;
import com.rsu.registration.dto.AggregatedStudentProfile;
import com.rsu.registration.service.StudentRegistrationService;
import com.rsu.registration.service.ContentBasedRouterService;
import com.rsu.registration.service.StudentProfileAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
 * Implements Content-Based Routing and Aggregator EIP Patterns
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
    private final ContentBasedRouterService contentBasedRouterService;
    private final StudentProfileAggregatorService aggregatorService;

    /**
     * Configure JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON converter
     */
    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        org.springframework.amqp.rabbit.core.RabbitTemplate template = 
                new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

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
        org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer container = 
                new org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(REGISTRATION_QUEUE);
        
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
        adapter.setOutputChannel(registrationInputChannel());
        return adapter;
    }

    /**
     * Service activator that processes registration messages
     * Implements Content-Based Router and Aggregator EIP Patterns
     * 
     * Flow:
     * 1. Route to appropriate systems (Content-Based Router)
     * 2. Aggregate responses from all systems (Aggregator)
     * 3. Save complete profile to database
     */
    @ServiceActivator(inputChannel = REGISTRATION_INPUT_CHANNEL, outputChannel = REGISTRATION_SERVICE_CHANNEL)
    public void processRegistration(@Payload StudentRegistrationDTO registrationDTO) {
        try {
            log.info("📨 Processing registration for student: {}", registrationDTO.getStudentId());

            // Step 1: Route to appropriate systems based on content (year level)
            // EIP Pattern: Content-Based Router
            log.info("🔀 Applying Content-Based Routing for year level: {}", registrationDTO.getYearLevel());
            ContentBasedRouterService.RoutingResult routingResult = 
                    contentBasedRouterService.routeRegistration(registrationDTO);

            // Step 2: Aggregate responses from all systems
            // EIP Pattern: Aggregator - Combine multiple system responses
            log.info("🔄 Starting aggregation of system responses...");
            AggregatedStudentProfile aggregatedProfile = 
                    aggregatorService.aggregateStudentProfile(registrationDTO);

            // Step 3: Save to database with routing and aggregation information
            log.info("💾 Saving registration to database with complete profile");
            var savedRegistration = registrationService.saveRegistration(registrationDTO);

            // Step 4: Update status with complete profile information
            String profileMessage = buildProfileMessage(routingResult, aggregatedProfile);
            String status = aggregatedProfile.getAggregationStatus().equals("COMPLETE") ? 
                           "PROFILE_COMPLETE" : "PROFILE_PARTIAL";
            
            registrationService.updateRegistrationStatus(
                    savedRegistration.getId(), 
                    status, 
                    profileMessage
            );

            log.info("✅ Successfully processed registration with ID: {}", savedRegistration.getId());
            log.info("📊 Aggregation Summary: {} - Status: {}, Time: {}ms, Responses: {}/{}",
                    aggregatedProfile.getStudentName(),
                    aggregatedProfile.getAggregationStatus(),
                    aggregatedProfile.getAggregationTimeMs(),
                    aggregatedProfile.getResponsesReceived(),
                    aggregatedProfile.getResponsesExpected());

        } catch (Exception e) {
            log.error("❌ Error processing registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process registration", e);
        }
    }

    /**
     * Build a comprehensive profile message from routing and aggregation results
     */
    private String buildProfileMessage(ContentBasedRouterService.RoutingResult routingResult,
                                       AggregatedStudentProfile profile) {
        StringBuilder message = new StringBuilder();
        
        // Routing info
        message.append("Routed to: ").append(String.join(", ", routingResult.getRoutedTo()));
        message.append(" | ");
        
        // Aggregation info
        message.append("Profile Status: ").append(profile.getAggregationStatus());
        message.append(" (").append(profile.getResponsesReceived()).append("/3 systems responded)");
        message.append(" | ");
        
        // Academic info
        if (profile.getAcademicRecords() != null) {
            message.append("Academic: Enrolled in ")
                   .append(profile.getAcademicRecords().getProgram())
                   .append(", Advisor: ")
                   .append(profile.getAcademicRecords().getAdvisorName());
            message.append(" | ");
        }
        
        // Housing or Billing info
        if (profile.getHousing() != null) {
            message.append("Housing: ")
                   .append(profile.getHousing().getDormitoryBuilding())
                   .append(", Room: ")
                   .append(profile.getHousing().getRoomAssignment());
            message.append(" | ");
        } else if (profile.getBilling() != null) {
            message.append("Billing: Total ₱")
                   .append(profile.getBilling().getTotalFeeAmount())
                   .append(", Due: ")
                   .append(profile.getBilling().getPaymentDeadline());
            message.append(" | ");
        }
        
        // Library info
        if (profile.getLibrary() != null) {
            message.append("Library: Card #")
                   .append(profile.getLibrary().getLibraryCardNumber())
                   .append(", Max Books: ")
                   .append(profile.getLibrary().getMaxBooksAllowed());
        }
        
        return message.toString();
    }
}
