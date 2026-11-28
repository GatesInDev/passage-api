package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reenvia à SEFAZ um documento pendente ou recusado.
 *
 * <p>Documentos autorizados ou cancelados não são reenviados: o
 * cancelamento é definitivo e não deve ser desfeito por um reenvio.</p>
 */
@Service
@RequiredArgsConstructor
public class RetryFiscalDocumentService {

    private final FindFiscalDocumentService findFiscalDocumentService;
    private final IssueFiscalDocumentService issueFiscalDocumentService;

    @Transactional
    public FiscalDocumentResponse execute(UUID id) {
        FiscalDocument document = findFiscalDocumentService.findEntity(id);

        if (!document.canBeResubmitted()) {
            throw new BusinessException(
                    "Somente documentos pendentes ou recusados podem ser reenviados. Situação atual: "
                            + document.getStatus() + ".");
        }

        return FiscalDocumentResponse.from(issueFiscalDocumentService.submit(document));
    }
}
