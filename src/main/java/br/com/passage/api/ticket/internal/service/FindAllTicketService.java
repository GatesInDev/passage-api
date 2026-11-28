package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    @Transactional(readOnly = true)
    public List<TicketResponse> execute() {

        return ticketRepository.findAllVisible(ticketAccessService.currentCompanyId()).stream()
                .map(TicketResponse::from)
                .toList();
    }
}
