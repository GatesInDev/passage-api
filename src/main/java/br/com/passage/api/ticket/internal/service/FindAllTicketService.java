package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class FindAllTicketService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<TicketResponse> execute() {

        return ticketRepository.findAll().stream()
                .filter(ticket -> !ticket.isDeleted())
                .map(TicketResponse::from)
                .toList();
    }
}
