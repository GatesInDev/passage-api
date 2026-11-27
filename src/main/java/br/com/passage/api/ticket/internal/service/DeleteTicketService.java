package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public void execute(UUID id) {

        Ticket ticket = ticketRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        ticket.setDeleted(true);
        ticket.setActive(false);
        ticketRepository.save(ticket);
    }
}
