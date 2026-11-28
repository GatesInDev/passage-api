package br.com.passage.api.integration.internal.service;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.integration.internal.repository.FiscalDocumentRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Consulta documentos fiscais no escopo do usuário logado (ver {@link FiscalDocumentAccessService}). */
@Service
@RequiredArgsConstructor
public class FindFiscalDocumentService {

    private final FiscalDocumentRepository fiscalDocumentRepository;
    private final FiscalDocumentAccessService accessService;

    @Transactional(readOnly = true)
    public FiscalDocumentResponse execute(UUID id) {
        return FiscalDocumentResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<FiscalDocumentResponse> executeAll() {
        return fiscalDocumentRepository.findAllVisible(accessService.currentCompanyId()).stream()
                .map(FiscalDocumentResponse::from)
                .toList();
    }

    /** @throws ResourceNotFoundException se o documento não existir, estiver excluído ou for de outra empresa */
    @Transactional(readOnly = true)
    public FiscalDocument findEntity(UUID id) {
        return fiscalDocumentRepository.findVisibleById(id, accessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento fiscal não encontrado com o identificador: " + id));
    }
}
