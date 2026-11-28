package br.com.passage.api.integration.internal.web;

import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.integration.internal.service.CancelFiscalDocumentService;
import br.com.passage.api.integration.internal.service.FindFiscalDocumentService;
import br.com.passage.api.integration.internal.service.RetryFiscalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST responsável pela consulta e pelos eventos dos documentos fiscais.
 *
 * <p>Os documentos são emitidos automaticamente na venda da passagem. Este controlador permite
 * consultar, cancelar e reenviar os documentos à SEFAZ.</p>
 *
 * <h2>Permissões</h2>
 *
 * <ul>
 *     <li>listagem e reenvio – ADMIN e TICKET_SELLER;</li>
 *     <li>consulta por identificador – ADMIN, TICKET_SELLER e INSPECTOR;</li>
 *     <li>cancelamento – ADMIN.</li>
 * </ul>
 *
 * <p>O escopo de empresa é definido pelo {@link br.com.passage.api.integration.internal.service.FiscalDocumentAccessService}: o administrador
 * acessa os documentos de todas as empresas e os demais perfis apenas os da própria empresa.</p>
 */
@Tag(name = "Documentos fiscais", description = "Consulta, cancelamento e reenvio do BP-e das passagens")
@RestController
@RequestMapping("api/v1/fiscal-documents")
@RequiredArgsConstructor
public class FiscalDocumentController {

    private final FindFiscalDocumentService findFiscalDocumentService;
    private final CancelFiscalDocumentService cancelFiscalDocumentService;
    private final RetryFiscalDocumentService retryFiscalDocumentService;

    @Operation(
            summary = "Listar documentos fiscais",
            description = "Lista os BP-e não excluídos. Quem não é ADMIN vê apenas os da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de documentos",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = FiscalDocumentResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @GetMapping
    public ResponseEntity<List<FiscalDocumentResponse>> findAll() {
        return ResponseEntity.ok(findFiscalDocumentService.executeAll());
    }

    @Operation(summary = "Buscar documento fiscal")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FiscalDocumentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER', 'INSPECTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<FiscalDocumentResponse> findById(
            @Parameter(description = "Identificador do documento fiscal", example = "d4c3b2a1-6f5e-4d7c-8b9a-0e1f2a3b4c5d", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findFiscalDocumentService.execute(id));
    }

    @Operation(
            summary = "Cancelar documento fiscal",
            description = "Registra o cancelamento de um BP-e autorizado; a chave de acesso e o protocolo são mantidos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento cancelado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FiscalDocumentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento não encontrado, excluído ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Documento não autorizado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<FiscalDocumentResponse> cancel(
            @Parameter(description = "Identificador do documento fiscal", example = "d4c3b2a1-6f5e-4d7c-8b9a-0e1f2a3b4c5d", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(cancelFiscalDocumentService.execute(id));
    }

    @Operation(
            summary = "Reenviar documento à SEFAZ",
            description = "Envia novamente um documento pendente ou recusado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Documento reenviado; a situação mostra o resultado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FiscalDocumentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Documento não encontrado, excluído ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Documento já autorizado",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PostMapping("/{id}/retry")
    public ResponseEntity<FiscalDocumentResponse> retry(
            @Parameter(description = "Identificador do documento fiscal", example = "d4c3b2a1-6f5e-4d7c-8b9a-0e1f2a3b4c5d", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(retryFiscalDocumentService.execute(id));
    }
}
