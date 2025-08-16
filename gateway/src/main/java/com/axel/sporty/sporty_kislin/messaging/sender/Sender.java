package com.axel.sporty.sporty_kislin.messaging.sender;

import com.axel.sporty.sporty_kislin.messaging.dto.TicketMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketUpdateMessage;
import com.axel.sporty.sporty_kislin.configuration.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Sender {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void sendSupportTicketMessage(TicketMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SUPPORT_TICKET_QUEUE, message);
    }
    
    public void sendTicketAssignmentMessage(TicketAssignmentMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_ASSIGNMENTS_QUEUE, message);
    }
    
    public void sendTicketUpdateMessage(TicketUpdateMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TICKET_UPDATES_QUEUE, message);
    }
}
