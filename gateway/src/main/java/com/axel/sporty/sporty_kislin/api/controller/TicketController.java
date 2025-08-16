package com.axel.sporty.sporty_kislin.api.controller;

import com.axel.sporty.sporty_kislin.api.dto.AssignTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketResponse;
import com.axel.sporty.sporty_kislin.api.dto.UpdateTicketRequest;
import com.axel.sporty.sporty_kislin.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    
    private final TicketService ticketService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }
    
    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.OK)
    public void assignTicket(@Valid @RequestBody AssignTicketRequest request) {
        ticketService.assignTicket(request);
    }
    
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateTicket(@Valid @RequestBody UpdateTicketRequest request) {
        ticketService.updateTicket(request);
    }
}
