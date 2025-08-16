package com.axel.sporty.consumer.messaging.listener;

import com.axel.sporty.consumer.dao.entity.Ticket;
import com.axel.sporty.consumer.dao.entity.TicketStatus;
import com.axel.sporty.consumer.dao.repository.TicketRepository;
import com.axel.sporty.consumer.messaging.dto.TicketMessage;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketMessageListenerE2ETest {

    @Autowired
    private TicketMessageListener ticketMessageListener;

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
    void shouldProcessTicketMessageAndSaveToDatabase() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-message.json");
        TicketMessage ticketMessage = objectMapper.readValue(resource.getInputStream(), TicketMessage.class);

        ticketMessageListener.handleTicketMessage(ticketMessage);

        Optional<Ticket> savedTicket = ticketRepository.findById(ticketMessage.getTicketId());
        assertThat(savedTicket).isPresent();
        
        Ticket ticket = savedTicket.get();
        assertThat(ticket.getTicketId()).isEqualTo(ticketMessage.getTicketId());
        assertThat(ticket.getSubject()).isEqualTo(ticketMessage.getSubject());
        assertThat(ticket.getDescription()).isEqualTo(ticketMessage.getDescription());
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getUserId()).isEqualTo(ticketMessage.getUserId());
        assertThat(ticket.getAssigneeId()).isNull();
        assertThat(ticket.getCreatedAt()).isEqualTo(ticketMessage.getCreatedAt());
        assertThat(ticket.getUpdatedAt()).isNull();
    }

    @Test
    void shouldHandleMultipleTicketMessages() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-data/ticket-message.json");
        TicketMessage ticketMessage1 = objectMapper.readValue(resource.getInputStream(), TicketMessage.class);
        
        TicketMessage ticketMessage2 = TicketMessage.builder()
                .ticketId(UUID.randomUUID())
                .subject("Second test ticket")
                .description("Second test description")
                .status("OPEN")
                .userId("user456")
                .assigneeId(null)
                .createdAt(ticketMessage1.getCreatedAt().plusMinutes(10))
                .updatedAt(null)
                .build();

        ticketMessageListener.handleTicketMessage(ticketMessage1);
        ticketMessageListener.handleTicketMessage(ticketMessage2);

        assertThat(ticketRepository.count()).isEqualTo(2);
        assertThat(ticketRepository.findById(ticketMessage1.getTicketId())).isPresent();
        assertThat(ticketRepository.findById(ticketMessage2.getTicketId())).isPresent();
    }
}
