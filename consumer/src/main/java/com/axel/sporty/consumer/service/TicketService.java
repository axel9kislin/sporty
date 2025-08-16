package com.axel.sporty.consumer.service;

import com.axel.sporty.consumer.dao.entity.Ticket;
import com.axel.sporty.consumer.dao.entity.TicketStatus;
import com.axel.sporty.consumer.dao.repository.TicketRepository;
import com.axel.sporty.consumer.messaging.dto.TicketMessage;
import com.axel.sporty.consumer.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.consumer.messaging.dto.TicketUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;

    public void processTicketMessage(TicketMessage ticketMessage) {
        log.info("Processing ticket message: {}", ticketMessage.getTicketId());
        
        TicketStatus status = TicketStatus.valueOf(ticketMessage.getStatus().toUpperCase());
        
        Ticket ticket = new Ticket(
                ticketMessage.getTicketId(),
                ticketMessage.getSubject(),
                ticketMessage.getDescription(),
                status,
                ticketMessage.getUserId(),
                ticketMessage.getAssigneeId(),
                ticketMessage.getCreatedAt(),
                ticketMessage.getUpdatedAt()
        );
        
        ticketRepository.save(ticket);
        log.info("Ticket saved to database: {}", ticket.getTicketId());
    }

    public void processTicketAssignment(TicketAssignmentMessage assignmentMessage) {
        log.info("Processing ticket assignment: {}", assignmentMessage.getTicketId());
        
        ticketRepository.findById(assignmentMessage.getTicketId())
                .ifPresentOrElse(
                        ticket -> {
                            ticket.setAssigneeId(assignmentMessage.getAssigneeId());
                            ticket.setUpdatedAt(assignmentMessage.getUpdatedAt());
                            ticketRepository.save(ticket);
                            log.info("Ticket assignment updated: {}", ticket.getTicketId());
                        },
                        () -> log.error("Ticket not found for assignment: {}", assignmentMessage.getTicketId())
                );
    }

    public void processTicketUpdate(TicketUpdateMessage updateMessage) {
        log.info("Processing ticket update: {}", updateMessage.getTicketId());
        
        ticketRepository.findById(updateMessage.getTicketId())
                .ifPresentOrElse(
                        ticket -> {
                            TicketStatus status = TicketStatus.valueOf(updateMessage.getStatus().toUpperCase());
                            ticket.setStatus(status);
                            ticket.setUpdatedAt(updateMessage.getUpdatedAt());
                            ticketRepository.save(ticket);
                            log.info("Ticket status updated: {}", ticket.getTicketId());
                        },
                        () -> log.error("Ticket not found for update: {}", updateMessage.getTicketId())
                );
    }
}
