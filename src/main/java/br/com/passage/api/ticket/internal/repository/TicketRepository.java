package br.com.passage.api.ticket.internal.repository;

import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    boolean existsByCode(String code);
}