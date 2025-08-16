package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.configuration.RabbitMQConfig;
import com.axel.sporty.consumer.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.consumer.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketAssignmentListener {

    private final TicketService ticketService;

    @RabbitListener(queues = RabbitMQConfig.TICKET_ASSIGNMENTS_QUEUE)
    public void handleTicketAssignmentMessage(TicketAssignmentMessage assignmentMessage) {
        log.info("Received ticket assignment message: {}", assignmentMessage);
        ticketService.processTicketAssignment(assignmentMessage);
    }
}
