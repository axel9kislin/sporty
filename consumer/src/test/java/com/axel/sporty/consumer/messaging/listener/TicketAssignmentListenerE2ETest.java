package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.dao.entity.Ticket;
import com.axel.sporty.consumer.dao.entity.TicketStatus;
import com.axel.sporty.consumer.dao.repository.TicketRepository;
import com.axel.sporty.consumer.messaging.dto.TicketAssignmentMessage;
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
class TicketAssignmentListenerE2ETest {

    @Autowired
    private TicketAssignmentListener ticketAssignmentListener;

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
    void shouldProcessTicketAssignmentAndUpdateDatabase() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Test ticket",
                "Test description",
                TicketStatus.OPEN,
                "user123",
                null,
                LocalDateTime.of(2024, 1, 15, 10, 30),
                null
        );
        ticketRepository.save(existingTicket);

        ClassPathResource resource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage = objectMapper.readValue(resource.getInputStream(), TicketAssignmentMessage.class);

        ticketAssignmentListener.handleTicketAssignmentMessage(assignmentMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketId);
        assertThat(updatedTicket).isPresent();
        
        Ticket ticket = updatedTicket.get();
        assertThat(ticket.getAssigneeId()).isEqualTo(assignmentMessage.getAssigneeId());
        assertThat(ticket.getUpdatedAt()).isEqualTo(assignmentMessage.getUpdatedAt());
        assertThat(ticket.getSubject()).isEqualTo("Test ticket");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void shouldHandleNonExistentTicketGracefully() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage = objectMapper.readValue(resource.getInputStream(), TicketAssignmentMessage.class);

        ticketAssignmentListener.handleTicketAssignmentMessage(assignmentMessage);

        Optional<Ticket> ticket = ticketRepository.findById(assignmentMessage.getTicketId());
        assertThat(ticket).isEmpty();
        assertThat(ticketRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldUpdateMultipleTicketAssignments() throws IOException {
        UUID ticketId1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID ticketId2 = UUID.randomUUID();
        
        Ticket ticket1 = new Ticket(ticketId1, "Ticket 1", "Description 1", TicketStatus.OPEN, "user1", null, LocalDateTime.now(), null);
        Ticket ticket2 = new Ticket(ticketId2, "Ticket 2", "Description 2", TicketStatus.OPEN, "user2", null, LocalDateTime.now(), null);
        
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        ClassPathResource resource = new ClassPathResource("test-data/ticket-assignment-message.json");
        TicketAssignmentMessage assignmentMessage1 = objectMapper.readValue(resource.getInputStream(), TicketAssignmentMessage.class);
        
        TicketAssignmentMessage assignmentMessage2 = TicketAssignmentMessage.builder()
                .ticketId(ticketId2)
                .assigneeId("agent789")
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();

        ticketAssignmentListener.handleTicketAssignmentMessage(assignmentMessage1);
        ticketAssignmentListener.handleTicketAssignmentMessage(assignmentMessage2);

        Optional<Ticket> updatedTicket1 = ticketRepository.findById(ticketId1);
        Optional<Ticket> updatedTicket2 = ticketRepository.findById(ticketId2);
        
        assertThat(updatedTicket1).isPresent();
        assertThat(updatedTicket1.get().getAssigneeId()).isEqualTo("agent456");
        
        assertThat(updatedTicket2).isPresent();
        assertThat(updatedTicket2.get().getAssigneeId()).isEqualTo("agent789");
    }
}
