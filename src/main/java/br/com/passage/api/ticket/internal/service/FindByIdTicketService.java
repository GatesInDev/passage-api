package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class FindByIdTicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse execute(UUID id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Não foi possível encontrar uma passagem com o identificador: " + id));

        return TicketResponse.from(ticket);
    }
}