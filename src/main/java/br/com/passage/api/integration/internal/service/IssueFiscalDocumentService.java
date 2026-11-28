package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.domain.enums.FiscalEnvironment;
import br.com.passage.api.integration.internal.gateway.SefazGateway;
import br.com.passage.api.integration.internal.gateway.SefazResponse;
import br.com.passage.api.integration.internal.repository.FiscalDocumentRepository;
import br.com.passage.api.ticket.TicketApi;
import br.com.passage.api.ticket.TicketInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Emite o BP-e de uma passagem vendida.
 *
 * <p>O documento é gravado com a situação {@code PENDING} antes do envio à
 * SEFAZ e atualizado com o resultado da autorização.</p>
 */
@Service
@RequiredArgsConstructor
public class IssueFiscalDocumentService {

    /**
     * Série única enquanto não há configuração por empresa.
     */
    private static final int DEFAULT_SERIES = 1;

    private static final String BPE_MODEL = "63";

    private final FiscalDocumentRepository fiscalDocumentRepository;
    private final SefazGateway sefazGateway;
    private final TicketApi ticketApi;

    /**
     * Emite o documento fiscal da passagem.
     *
     * <p>Se a passagem já possuir documento, o existente é retornado sem nova emissão.</p>
     *
     * @param ticketId identificador da passagem
     * @return documento fiscal da passagem
     * @throws ResourceNotFoundException se a passagem não existir ou estiver excluída
     */
    @Transactional
    public FiscalDocument execute(UUID ticketId) {

        var existing = fiscalDocumentRepository.findByTicketId(ticketId);
        if (existing.isPresent()) {
            return existing.get();
        }

        TicketInfo ticket = ticketApi.findTicket(ticketId);

        FiscalDocument document = fiscalDocumentRepository.save(FiscalDocument.builder()
                .model(BPE_MODEL)
                .series(DEFAULT_SERIES)
                .number(fiscalDocumentRepository.lastNumberOfSeries(DEFAULT_SERIES) + 1)
                .status(FiscalDocumentStatus.PENDING)
                .environment(FiscalEnvironment.HOMOLOGATION)
                .ticketId(ticket.id())
                .companyUuid(ticket.companyUuid())
                .passengerName(ticket.passengerName())
                .passengerDocument(ticket.passengerDocument())
                .amount(ticket.price())
                .isActive(true)
                .build());

        return submit(document);
    }

    /**
     * Envia o documento à SEFAZ e registra o desfecho.
     *
     * @param document documento a ser autorizado
     * @return documento autorizado ou recusado
     */
    @Transactional
    public FiscalDocument submit(FiscalDocument document) {
        SefazResponse response = sefazGateway.authorize(document);

        if (response.authorized()) {
            document.authorize(response.accessKey(), response.protocol(), response.authorizedAt());
        } else {
            document.reject(response.rejectionReason());
        }

        return fiscalDocumentRepository.save(document);
    }
}
