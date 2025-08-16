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
public class TicketMessage {
    private UUID ticketId;
    private String subject;
    private String description;
    private String status;
    private String userId;
    private String assigneeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
