package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelo cancelamento de passagens.
 *
 * <p>O cancelamento é uma exclusão lógica: a passagem é marcada como excluída e inativa,
 * permanece no banco e o assento volta a ficar disponível na viagem.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    /**
     * Cancela uma passagem visível ao usuário autenticado.
     *
     * @param id identificador da passagem
     * @throws ResourceNotFoundException se a passagem não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional
    public void execute(UUID id) {

        Ticket ticket = ticketRepository.findVisibleById(id, ticketAccessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        ticket.setDeleted(true);
        ticket.setActive(false);
        ticketRepository.save(ticket);
    }
}
