package com.axel.sporty.consumer.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentMessage {
    private UUID ticketId;
    private String assigneeId;
    private LocalDateTime updatedAt;
}
