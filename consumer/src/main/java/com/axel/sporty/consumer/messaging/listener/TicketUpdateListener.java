package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.configuration.RabbitMQConfig;
import com.axel.sporty.consumer.messaging.dto.TicketUpdateMessage;
import com.axel.sporty.consumer.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketUpdateListener {

    private final TicketService ticketService;

    @RabbitListener(queues = RabbitMQConfig.TICKET_UPDATES_QUEUE)
    public void handleTicketUpdateMessage(TicketUpdateMessage updateMessage) {
        log.info("Received ticket update message: {}", updateMessage);
        ticketService.processTicketUpdate(updateMessage);
    }
}
