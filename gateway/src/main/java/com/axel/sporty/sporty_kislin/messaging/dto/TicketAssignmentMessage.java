package com.axel.sporty.sporty_kislin.messaging.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketAssignmentMessage {
    private UUID ticketId;
    private String assigneeId;
    private LocalDateTime updatedAt;
}
