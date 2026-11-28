package br.com.passage.api.integration.internal.service;

import br.com.passage.api.ticket.TicketSold;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Emite o documento fiscal quando uma passagem é vendida.
 *
 * <p>{@code @ApplicationModuleListener} roda depois do commit da venda, em transação própria, e o
 * Spring Modulith registra a entrega: uma falha aqui não desfaz a venda e o evento pode ser
 * reprocessado.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketSoldListener {

    private final IssueFiscalDocumentService issueFiscalDocumentService;

    @ApplicationModuleListener
    public void on(TicketSold sale) {
        log.info("Emitindo BP-e para a passagem {}", sale.ticketId());

        var document = issueFiscalDocumentService.execute(sale.ticketId());

        log.info("BP-e da passagem {} ficou com a situação {}",
                sale.ticketId(), document.getStatus());
    }
}
