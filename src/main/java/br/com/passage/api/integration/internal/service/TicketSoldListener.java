package br.com.passage.api.integration.internal.service;

import br.com.passage.api.ticket.TicketSold;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Emite o documento fiscal quando uma passagem é vendida.
 *
 * <p>{@code @ApplicationModuleListener} entrega o evento em uma transação
 * própria e apenas após a venda ser confirmada. A entrega fica registrada
 * pelo Spring Modulith, então uma falha aqui não desfaz a venda e o evento
 * pode ser reprocessado depois.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketSoldListener {

    private final IssueFiscalDocumentService issueFiscalDocumentService;

    /**
     * Emite o documento fiscal da passagem vendida.
     *
     * @param sale evento da venda
     */
    @ApplicationModuleListener
    public void on(TicketSold sale) {
        log.info("Emitindo BP-e para a passagem {}", sale.ticketId());

        var document = issueFiscalDocumentService.execute(sale.ticketId());

        log.info("BP-e da passagem {} ficou com a situação {}",
                sale.ticketId(), document.getStatus());
    }
}
