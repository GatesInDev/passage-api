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
    private final TicketAccessService ticketAccessService;

    @Transactional
    public void execute(UUID id) {

        Ticket ticket = ticketRepository.findVisibleById(id, ticketAccessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        ticket.setDeleted(true);
        ticket.setActive(false);
        ticketRepository.save(ticket);
    }
}
