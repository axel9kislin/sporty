package com.axel.sporty.consumer.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SUPPORT_TICKET_QUEUE = "support-tickets";
    public static final String TICKET_ASSIGNMENTS_QUEUE = "ticket-assignments";
    public static final String TICKET_UPDATES_QUEUE = "ticket-updates";

    @Bean
    public Queue supportTicketsQueue() {
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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }


}
