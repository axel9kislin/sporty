package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.configuration.RabbitMQConfig;
import com.axel.sporty.consumer.messaging.dto.TicketMessage;
import com.axel.sporty.consumer.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketMessageListener {

    private final TicketService ticketService;

    @RabbitListener(queues = RabbitMQConfig.SUPPORT_TICKET_QUEUE)
    public void handleTicketMessage(TicketMessage ticketMessage) {
        log.info("Received ticket message: {}", ticketMessage);
        ticketService.processTicketMessage(ticketMessage);
    }
}
