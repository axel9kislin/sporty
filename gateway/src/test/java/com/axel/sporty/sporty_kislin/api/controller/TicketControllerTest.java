package com.axel.sporty.sporty_kislin.api.controller;

import com.axel.sporty.sporty_kislin.messaging.sender.Sender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class TicketControllerTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private Sender sender;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private String loadJsonFromFile(String fileName) throws Exception {
        return Files.readString(Paths.get("src/test/resources/tickets/" + fileName));
    }

    @Test
    void createTicket_ShouldReturnCreatedWithTicketId() throws Exception {
        String requestJson = loadJsonFromFile("create-ticket-request.json");

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").exists());

        verify(sender).sendSupportTicketMessage(any());
    }

    @Test
    void assignTicket_ShouldReturnOk() throws Exception {
        String requestJson = loadJsonFromFile("assign-ticket-request.json");

        mockMvc.perform(post("/api/v1/tickets/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(sender).sendTicketAssignmentMessage(any());
    }

    @Test
    void updateTicket_ShouldReturnOk() throws Exception {
        String requestJson = loadJsonFromFile("update-ticket-request.json");

        mockMvc.perform(put("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(sender).sendTicketUpdateMessage(any());
    }
}
