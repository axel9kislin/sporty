package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.dao.entity.Ticket;
import com.axel.sporty.consumer.dao.entity.TicketStatus;
import com.axel.sporty.consumer.dao.repository.TicketRepository;
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
class TicketUpdateListenerE2ETest {

    @Autowired
    private TicketUpdateListener ticketUpdateListener;

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
    void shouldProcessTicketUpdateAndUpdateDatabase() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Test ticket",
                "Test description",
                TicketStatus.OPEN,
                "user123",
                "agent456",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                LocalDateTime.of(2024, 1, 15, 11, 0)
        );
        ticketRepository.save(existingTicket);

        ClassPathResource resource = new ClassPathResource("test-data/ticket-update-message.json");
        TicketUpdateMessage updateMessage = objectMapper.readValue(resource.getInputStream(), TicketUpdateMessage.class);

        ticketUpdateListener.handleTicketUpdateMessage(updateMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketId);
        assertThat(updatedTicket).isPresent();
        
        Ticket ticket = updatedTicket.get();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(ticket.getUpdatedAt()).isEqualTo(updateMessage.getUpdatedAt());
        assertThat(ticket.getSubject()).isEqualTo("Test ticket");
        assertThat(ticket.getAssigneeId()).isEqualTo("agent456");
    }

    @Test
    void shouldHandleNonExistentTicketGracefully() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-update-message.json");
        TicketUpdateMessage updateMessage = objectMapper.readValue(resource.getInputStream(), TicketUpdateMessage.class);

        ticketUpdateListener.handleTicketUpdateMessage(updateMessage);

        Optional<Ticket> ticket = ticketRepository.findById(updateMessage.getTicketId());
        assertThat(ticket).isEmpty();
        assertThat(ticketRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldUpdateTicketStatusFromOpenToResolved() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Ticket to resolve",
                "Description",
                TicketStatus.OPEN,
                "user123",
                "agent456",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                null
        );
        ticketRepository.save(existingTicket);

        TicketUpdateMessage resolveMessage = TicketUpdateMessage.builder()
                .ticketId(ticketId)
                .status("RESOLVED")
                .updatedAt(LocalDateTime.of(2024, 1, 15, 15, 0))
                .build();

        ticketUpdateListener.handleTicketUpdateMessage(resolveMessage);

        Optional<Ticket> updatedTicket = ticketRepository.findById(ticketId);
        assertThat(updatedTicket).isPresent();
        assertThat(updatedTicket.get().getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(updatedTicket.get().getUpdatedAt()).isEqualTo(resolveMessage.getUpdatedAt());
    }

    @Test
    void shouldHandleMultipleStatusUpdates() throws IOException {
        UUID ticketId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Ticket existingTicket = new Ticket(
                ticketId,
                "Multi-update ticket",
                "Description",
                TicketStatus.OPEN,
                "user123",
                "agent456",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                null
        );
        ticketRepository.save(existingTicket);

        TicketUpdateMessage update1 = TicketUpdateMessage.builder()
                .ticketId(ticketId)
                .status("IN_PROGRESS")
                .updatedAt(LocalDateTime.of(2024, 1, 15, 11, 0))
                .build();

        TicketUpdateMessage update2 = TicketUpdateMessage.builder()
                .ticketId(ticketId)
                .status("RESOLVED")
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();

        ticketUpdateListener.handleTicketUpdateMessage(update1);
        Optional<Ticket> afterFirstUpdate = ticketRepository.findById(ticketId);
        assertThat(afterFirstUpdate.get().getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);

        ticketUpdateListener.handleTicketUpdateMessage(update2);
        Optional<Ticket> afterSecondUpdate = ticketRepository.findById(ticketId);
        assertThat(afterSecondUpdate.get().getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(afterSecondUpdate.get().getUpdatedAt()).isEqualTo(update2.getUpdatedAt());
    }
}
