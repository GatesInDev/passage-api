package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela consulta de uma passagem.
 *
 * <p>O escopo de empresa é definido pelo {@link TicketAccessService}: o administrador acessa
 * as passagens de todas as empresas e os demais perfis apenas as da própria empresa.</p>
 */
@Service
@RequiredArgsConstructor
public class FindByIdTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    /**
     * Busca uma passagem visível ao usuário autenticado.
     *
     * @param id identificador da passagem
     * @return passagem encontrada
     * @throws ResourceNotFoundException se a passagem não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public TicketResponse execute(UUID id) {

        Ticket ticket = ticketRepository.findVisibleById(id, ticketAccessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        return TicketResponse.from(ticket);
    }
}