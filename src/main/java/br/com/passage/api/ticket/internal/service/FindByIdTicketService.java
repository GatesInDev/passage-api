package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindByIdTicketService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public TicketResponse execute(UUID id) {

        Ticket ticket = ticketRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        return TicketResponse.from(ticket);
    }
}