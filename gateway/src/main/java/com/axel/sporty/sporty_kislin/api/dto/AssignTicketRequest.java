package com.axel.sporty.sporty_kislin.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignTicketRequest {
    @NotBlank
    private String ticketId;
    
    @NotBlank
    private String assigneeId;
}
