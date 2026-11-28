package br.com.passage.api.integration.internal.web;

import br.com.passage.api.integration.internal.dto.FiscalDocumentResponse;
import br.com.passage.api.integration.internal.service.CancelFiscalDocumentService;
import br.com.passage.api.integration.internal.service.FindFiscalDocumentService;
import br.com.passage.api.integration.internal.service.RetryFiscalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Documentos fiscais",
        description = "Consulta e eventos do BP-e emitido para as passagens")
@RestController
@RequestMapping("api/v1/fiscal-documents")
@RequiredArgsConstructor
public class FiscalDocumentController {

    private final FindFiscalDocumentService findFiscalDocumentService;
    private final CancelFiscalDocumentService cancelFiscalDocumentService;
    private final RetryFiscalDocumentService retryFiscalDocumentService;

    @Operation(
            summary = "Listar documentos fiscais",
            description = "Recupera os BP-e da empresa do usuário. O administrador recebe os de todas as empresas."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Listagem recuperada com sucesso"))
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @GetMapping
    public ResponseEntity<List<FiscalDocumentResponse>> findAll() {
        return ResponseEntity.ok(findFiscalDocumentService.executeAll());
    }

    @Operation(
            summary = "Buscar documento fiscal por UUID",
            description = "Recupera um BP-e pelo seu identificador público."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FiscalDocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum documento encontrado para o UUID informado",
                    content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER', 'INSPECTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<FiscalDocumentResponse> findById(
            @Parameter(description = "UUID público do documento fiscal", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findFiscalDocumentService.execute(id));
    }

    @Operation(
            summary = "Cancelar documento fiscal",
            description = "Registra o cancelamento de um BP-e autorizado. O documento permanece "
                    + "armazenado com sua chave de acesso e protocolo originais."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento cancelado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FiscalDocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum documento encontrado para o UUID informado",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Documento não está autorizado",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<FiscalDocumentResponse> cancel(
            @Parameter(description = "UUID público do documento fiscal", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(cancelFiscalDocumentService.execute(id));
    }

    @Operation(
            summary = "Reenviar documento à SEFAZ",
            description = "Submete novamente um documento pendente ou recusado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento reenviado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FiscalDocumentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum documento encontrado para o UUID informado",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Documento já autorizado",
                    content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PostMapping("/{id}/retry")
    public ResponseEntity<FiscalDocumentResponse> retry(
            @Parameter(description = "UUID público do documento fiscal", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(retryFiscalDocumentService.execute(id));
    }
}
