package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.integration.internal.repository.FiscalDocumentRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Cancela um documento fiscal junto à SEFAZ.
 *
 * <p>O cancelamento é um evento fiscal, não uma exclusão: o documento
 * permanece com sua chave de acesso e protocolo originais.</p>
 */
@Service
@RequiredArgsConstructor
public class CancelFiscalDocumentService {

    private final FiscalDocumentRepository fiscalDocumentRepository;
    private final FindFiscalDocumentService findFiscalDocumentService;

    @Transactional
    public FiscalDocumentResponse execute(UUID id) {
        FiscalDocument document = findFiscalDocumentService.findEntity(id);

        if (!document.isAuthorized()) {
            throw new BusinessException(
                    "Somente documentos autorizados podem ser cancelados. Situação atual: "
                            + document.getStatus() + ".");
        }

        document.cancel(Instant.now());

        return FiscalDocumentResponse.from(fiscalDocumentRepository.save(document));
    }
}
