package com.axel.sporty.sporty_kislin.service;

import com.axel.sporty.sporty_kislin.api.dto.AssignTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketResponse;
import com.axel.sporty.sporty_kislin.api.dto.UpdateTicketRequest;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketUpdateMessage;
import com.axel.sporty.sporty_kislin.messaging.sender.Sender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final Sender sender;
    
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        UUID ticketId = UUID.randomUUID();
        
        TicketMessage message = TicketMessage.builder()
                .ticketId(ticketId)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status("open")
                .userId(request.getUserId())
                .assigneeId(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();
        
        sender.sendSupportTicketMessage(message);
        
        return new CreateTicketResponse(ticketId);
    }
    
    public void assignTicket(AssignTicketRequest request) {
        TicketAssignmentMessage message = TicketAssignmentMessage.builder()
                .ticketId(UUID.fromString(request.getTicketId()))
                .assigneeId(request.getAssigneeId())
                .updatedAt(LocalDateTime.now())
                .build();
        
        sender.sendTicketAssignmentMessage(message);
    }
    
    public void updateTicket(UpdateTicketRequest request) {
        TicketUpdateMessage message = TicketUpdateMessage.builder()
                .ticketId(UUID.fromString(request.getTicketId()))
                .status(request.getStatus())
                .updatedAt(LocalDateTime.now())
                .build();
        
        sender.sendTicketUpdateMessage(message);
    }
}
