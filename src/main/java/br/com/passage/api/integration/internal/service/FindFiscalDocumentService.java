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

/**
 * Serviço responsável pela consulta de documentos fiscais.
 *
 * <p>Documentos excluídos não são retornados.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link FiscalDocumentAccessService}: o administrador
 * acessa os documentos de todas as empresas e os demais perfis apenas os da própria empresa.</p>
 */
@Service
@RequiredArgsConstructor
public class FindFiscalDocumentService {

    private final FiscalDocumentRepository fiscalDocumentRepository;
    private final FiscalDocumentAccessService accessService;

    /**
     * Busca um documento fiscal visível ao usuário autenticado.
     *
     * @param id identificador do documento
     * @return documento encontrado
     * @see #findEntity(UUID)
     */
    @Transactional(readOnly = true)
    public FiscalDocumentResponse execute(UUID id) {
        return FiscalDocumentResponse.from(findEntity(id));
    }

    /**
     * Lista os documentos fiscais visíveis ao usuário autenticado.
     *
     * @return documentos encontrados
     */
    @Transactional(readOnly = true)
    public List<FiscalDocumentResponse> executeAll() {
        return fiscalDocumentRepository.findAllVisible(accessService.currentCompanyId()).stream()
                .map(FiscalDocumentResponse::from)
                .toList();
    }

    /**
     * Busca a entidade de um documento fiscal visível ao usuário autenticado.
     *
     * @param id identificador do documento
     * @return documento encontrado
     * @throws ResourceNotFoundException se o documento não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public FiscalDocument findEntity(UUID id) {
        return fiscalDocumentRepository.findVisibleById(id, accessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento fiscal não encontrado com o identificador: " + id));
    }
}
