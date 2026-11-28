package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pelo reenvio de documentos fiscais à SEFAZ.
 *
 * <p>Somente documentos pendentes ou recusados podem ser reenviados. Documentos autorizados ou
 * cancelados não são reenviados.</p>
 */
@Service
@RequiredArgsConstructor
public class RetryFiscalDocumentService {

    private final FindFiscalDocumentService findFiscalDocumentService;
    private final IssueFiscalDocumentService issueFiscalDocumentService;

    /**
     * Reenvia um documento fiscal à SEFAZ.
     *
     * @param id identificador do documento
     * @return documento com o novo resultado da autorização
     * @throws ResourceNotFoundException se o documento não existir, estiver excluído ou pertencer a outra empresa
     * @throws BusinessException         se o documento não estiver pendente nem recusado
     */
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
