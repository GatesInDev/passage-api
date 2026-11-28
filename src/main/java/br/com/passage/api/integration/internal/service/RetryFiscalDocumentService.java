package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Reenvia à SEFAZ um documento ainda não autorizado. */
@Service
@RequiredArgsConstructor
public class RetryFiscalDocumentService {

    private final FindFiscalDocumentService findFiscalDocumentService;
    private final IssueFiscalDocumentService issueFiscalDocumentService;

    @Transactional
    public FiscalDocumentResponse execute(UUID id) {
        FiscalDocument document = findFiscalDocumentService.findEntity(id);

        if (document.isAuthorized()) {
            throw new BusinessException("O documento já está autorizado.");
        }

        return FiscalDocumentResponse.from(issueFiscalDocumentService.submit(document));
    }
}
