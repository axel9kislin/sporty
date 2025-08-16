package com.axel.sporty.sporty_kislin.service;

import com.axel.sporty.sporty_kislin.api.dto.AssignTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketRequest;
import com.axel.sporty.sporty_kislin.api.dto.CreateTicketResponse;
import com.axel.sporty.sporty_kislin.api.dto.UpdateTicketRequest;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.sporty_kislin.messaging.dto.TicketUpdateMessage;
import com.axel.sporty.sporty_kislin.messaging.sender.Sender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceE2ETest {

    @Autowired
    private TicketService ticketService;

    @MockBean
    private Sender sender;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateTicketAndSendMessage() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/create-ticket-request.json");
        CreateTicketRequest request = objectMapper.readValue(resource.getInputStream(), CreateTicketRequest.class);

        CreateTicketResponse response = ticketService.createTicket(request);

        assertThat(response).isNotNull();
        assertThat(response.getTicketId()).isNotNull();

        ArgumentCaptor<TicketMessage> messageCaptor = ArgumentCaptor.forClass(TicketMessage.class);
        verify(sender).sendSupportTicketMessage(messageCaptor.capture());

        TicketMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTicketId()).isEqualTo(response.getTicketId());
        assertThat(sentMessage.getSubject()).isEqualTo(request.getSubject());
        assertThat(sentMessage.getDescription()).isEqualTo(request.getDescription());
        assertThat(sentMessage.getStatus()).isEqualTo("open");
        assertThat(sentMessage.getUserId()).isEqualTo(request.getUserId());
        assertThat(sentMessage.getAssigneeId()).isNull();
        assertThat(sentMessage.getCreatedAt()).isNotNull();
        assertThat(sentMessage.getUpdatedAt()).isNull();
    }

    @Test
    void shouldAssignTicketAndSendMessage() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/assign-ticket-request.json");
        AssignTicketRequest request = objectMapper.readValue(resource.getInputStream(), AssignTicketRequest.class);

        ticketService.assignTicket(request);

        ArgumentCaptor<TicketAssignmentMessage> messageCaptor = ArgumentCaptor.forClass(TicketAssignmentMessage.class);
        verify(sender).sendTicketAssignmentMessage(messageCaptor.capture());

        TicketAssignmentMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTicketId()).isEqualTo(UUID.fromString(request.getTicketId()));
        assertThat(sentMessage.getAssigneeId()).isEqualTo(request.getAssigneeId());
        assertThat(sentMessage.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTicketAndSendMessage() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/update-ticket-request.json");
        UpdateTicketRequest request = objectMapper.readValue(resource.getInputStream(), UpdateTicketRequest.class);

        ticketService.updateTicket(request);

        ArgumentCaptor<TicketUpdateMessage> messageCaptor = ArgumentCaptor.forClass(TicketUpdateMessage.class);
        verify(sender).sendTicketUpdateMessage(messageCaptor.capture());

        TicketUpdateMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTicketId()).isEqualTo(UUID.fromString(request.getTicketId()));
        assertThat(sentMessage.getStatus()).isEqualTo(request.getStatus());
        assertThat(sentMessage.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldHandleCompleteTicketWorkflow() throws IOException {
        ClassPathResource createResource = new ClassPathResource("test-data/create-ticket-request.json");
        CreateTicketRequest createRequest = objectMapper.readValue(createResource.getInputStream(), CreateTicketRequest.class);

        CreateTicketResponse createResponse = ticketService.createTicket(createRequest);
        String ticketId = createResponse.getTicketId().toString();

        AssignTicketRequest assignRequest = new AssignTicketRequest();
        assignRequest.setTicketId(ticketId);
        assignRequest.setAssigneeId("agent456");

        ticketService.assignTicket(assignRequest);

        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setTicketId(ticketId);
        updateRequest.setStatus("RESOLVED");

        ticketService.updateTicket(updateRequest);

        ArgumentCaptor<TicketMessage> createCaptor = ArgumentCaptor.forClass(TicketMessage.class);
        ArgumentCaptor<TicketAssignmentMessage> assignCaptor = ArgumentCaptor.forClass(TicketAssignmentMessage.class);
        ArgumentCaptor<TicketUpdateMessage> updateCaptor = ArgumentCaptor.forClass(TicketUpdateMessage.class);

        verify(sender).sendSupportTicketMessage(createCaptor.capture());
        verify(sender).sendTicketAssignmentMessage(assignCaptor.capture());
        verify(sender).sendTicketUpdateMessage(updateCaptor.capture());

        TicketMessage createMessage = createCaptor.getValue();
        TicketAssignmentMessage assignMessage = assignCaptor.getValue();
        TicketUpdateMessage updateMessage = updateCaptor.getValue();

        assertThat(createMessage.getTicketId()).isEqualTo(assignMessage.getTicketId());
        assertThat(assignMessage.getTicketId()).isEqualTo(updateMessage.getTicketId());
        assertThat(assignMessage.getAssigneeId()).isEqualTo("agent456");
        assertThat(updateMessage.getStatus()).isEqualTo("RESOLVED");
    }

    @Test
    void shouldGenerateUniqueTicketIds() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/create-ticket-request.json");
        CreateTicketRequest request = objectMapper.readValue(resource.getInputStream(), CreateTicketRequest.class);

        CreateTicketResponse response1 = ticketService.createTicket(request);
        CreateTicketResponse response2 = ticketService.createTicket(request);

        assertThat(response1.getTicketId()).isNotEqualTo(response2.getTicketId());
    }
}
