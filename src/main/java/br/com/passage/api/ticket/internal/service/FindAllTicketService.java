package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela listagem de passagens.
 *
 * <p>Passagens excluídas não são retornadas.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link TicketAccessService}: o administrador acessa
 * as passagens de todas as empresas e os demais perfis apenas as da própria empresa.</p>
 */
@Service
@RequiredArgsConstructor
public class FindAllTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    /**
     * Lista as passagens visíveis ao usuário autenticado.
     *
     * @return passagens encontradas
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> execute() {

        return ticketRepository.findAllVisible(ticketAccessService.currentCompanyId()).stream()
                .map(TicketResponse::from)
                .toList();
    }
}
