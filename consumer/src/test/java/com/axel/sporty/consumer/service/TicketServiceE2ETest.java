package com.axel.sporty.consumer.service;

import com.axel.sporty.consumer.dao.entity.Ticket;
import com.axel.sporty.consumer.dao.entity.TicketStatus;
import com.axel.sporty.consumer.dao.repository.TicketRepository;
import com.axel.sporty.consumer.messaging.dto.TicketMessage;
import com.axel.sporty.consumer.messaging.dto.TicketAssignmentMessage;
import com.axel.sporty.consumer.messaging.dto.TicketUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketServiceE2ETest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ticketRepository.deleteAll();
    }

    @Test
    void shouldProcessTicketMessageAndPersistToDatabase() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-message.json");
        TicketMessage ticketMessage = objectMapper.readValue(resource.getInputStream(), TicketMessage.class);

        ticketService.processTicketMessage(ticketMessage);

        Optional<Ticket> savedTicket = ticketRepository.findById(ticketMessage.getTicketId());
        assertThat(savedTicket).isPresent();
        
        Ticket ticket = savedTicket.get();
        assertThat(ticket.getTicketId()).isEqualTo(ticketMessage.getTicketId());
        assertThat(ticket.getSubject()).isEqualTo(ticketMessage.getSubject());
        assertThat(ticket.getDescription()).isEqualTo(ticketMessage.getDescription());
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getUserId()).isEqualTo(ticketMessage.getUserId());
        assertThat(ticket.getAssigneeId()).isNull();
    }

    @Test
    void shouldProcessTicketAssignmentAndUpdateExistingTicket() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Existing ticket",
                "Description",
                TicketStatus.OPEN,
                "user123",
                null,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                null
        );
        ticketRepository.save(existingTicket);

        ClassPathResource resource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage = objectMapper.readValue(resource.getInputStream(), TicketAssignmentMessage.class);

        ticketService.processTicketAssignment(assignmentMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketId);
        assertThat(updatedTicket).isPresent();
        assertThat(updatedTicket.get().getAssigneeId()).isEqualTo(assignmentMessage.getAssigneeId());
        assertThat(updatedTicket.get().getUpdatedAt()).isEqualTo(assignmentMessage.getUpdatedAt());
    }

    @Test
    void shouldProcessTicketUpdateAndChangeStatus() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Ticket to update",
                "Description",
                TicketStatus.OPEN,
                "user123",
                "agent456",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                null
        );
        ticketRepository.save(existingTicket);

        ClassPathResource resource = new ClassPathResource("test-data/ticket-update-message.json");
        TicketUpdateMessage updateMessage = objectMapper.readValue(resource.getInputStream(), TicketUpdateMessage.class);

        ticketService.processTicketUpdate(updateMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketId);
        assertThat(updatedTicket).isPresent();
        assertThat(updatedTicket.get().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(updatedTicket.get().getUpdatedAt()).isEqualTo(updateMessage.getUpdatedAt());
    }

    @Test
    void shouldHandleCompleteTicketLifecycle() throws IOException {
        ClassPathResource ticketResource = new ClassPathResource("test-data/ticket-message.json");
        TicketMessage ticketMessage = objectMapper.readValue(ticketResource.getInputStream(), TicketMessage.class);

        ticketService.processTicketMessage(ticketMessage);

        Optional<Ticket> createdTicket = ticketRepository.findById(ticketMessage.getTicketId());
        assertThat(createdTicket).isPresent();
        assertThat(createdTicket.get().getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(createdTicket.get().getAssigneeId()).isNull();

        ClassPathResource assignmentResource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage = objectMapper.readValue(assignmentResource.getInputStream(), TicketAssignmentMessage.class);

        ticketService.processTicketAssignment(assignmentMessage);

        Optional<Ticket> assignedTicket = ticketRepository.findById(ticketMessage.getTicketId());
        assertThat(assignedTicket).isPresent();
        assertThat(assignedTicket.get().getAssigneeId()).isEqualTo("agent456");

        ClassPathResource updateResource = new ClassPathResource("test-data/ticket-update-message.json");
        TicketUpdateMessage updateMessage = objectMapper.readValue(updateResource.getInputStream(), TicketUpdateMessage.class);

        ticketService.processTicketUpdate(updateMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketMessage.getTicketId());
        assertThat(updatedTicket).isPresent();
        assertThat(updatedTicket.get().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(updatedTicket.get().getAssigneeId()).isEqualTo("agent456");
    }

    @Test
    void shouldHandleNonExistentTicketForAssignmentGracefully() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage = objectMapper.readValue(resource.getInputStream(), TicketAssignmentMessage.class);

        ticketService.processTicketAssignment(assignmentMessage);

        assertThat(ticketRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldHandleNonExistentTicketForUpdateGracefully() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-update-message.json");
        TicketUpdateMessage updateMessage = objectMapper.readValue(resource.getInputStream(), TicketUpdateMessage.class);

        ticketService.processTicketUpdate(updateMessage);

        assertThat(ticketRepository.count()).isEqualTo(0);
    }
}
