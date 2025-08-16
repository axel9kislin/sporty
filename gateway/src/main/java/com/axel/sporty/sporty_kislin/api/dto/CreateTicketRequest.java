package com.axel.sporty.sporty_kislin.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String subject;
    
    @NotBlank
    private String description;
}
