package com.axel.sporty.sporty_kislin.api.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateTicketResponse {
    private UUID ticketId;
    
    public CreateTicketResponse(UUID ticketId) {
        this.ticketId = ticketId;
    }
}
