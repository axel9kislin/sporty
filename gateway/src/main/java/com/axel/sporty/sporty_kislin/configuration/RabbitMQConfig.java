package com.axel.sporty.sporty_kislin.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String SUPPORT_TICKET_QUEUE = "support-tickets)";
    public static final String TICKET_ASSIGNMENTS_QUEUE = "ticket-assignments";
    public static final String TICKET_UPDATES_QUEUE = "ticket-updates";
    
    @Bean
    public Queue supportTicketQueue() {
        return new Queue(SUPPORT_TICKET_QUEUE, true);
    }
    
    @Bean
    public Queue ticketAssignmentsQueue() {
        return new Queue(TICKET_ASSIGNMENTS_QUEUE, true);
    }
    
    @Bean
    public Queue ticketUpdatesQueue() {
        return new Queue(TICKET_UPDATES_QUEUE, true);
    }
    
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
