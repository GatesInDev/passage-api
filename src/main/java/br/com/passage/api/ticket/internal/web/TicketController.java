package br.com.passage.api.ticket.internal.web;

import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.dto.UpdateTicketRequest;
import br.com.passage.api.ticket.internal.service.CreateTicketService;
import br.com.passage.api.ticket.internal.service.DeleteTicketService;
import br.com.passage.api.ticket.internal.service.FindAllTicketService;
import br.com.passage.api.ticket.internal.service.FindByIdTicketService;
import br.com.passage.api.ticket.internal.service.UpdateTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Passagens", description = "Endpoints para gestão de passagens no módulo de Tickets")
@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final CreateTicketService createTicketService;
    private final FindByIdTicketService findByIdTicketService;
    private final FindAllTicketService findAllTicketService;
    private final UpdateTicketService updateTicketService;
    private final DeleteTicketService deleteTicketService;

    @Operation(
            summary = "Cadastrar nova passagem",
            description = "Cria um novo registro de passagem com os dados do passageiro, assento, rota e classe do serviço."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Passagem cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de dados (ex: código de passagem já cadastrado no sistema)",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = createTicketService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar passagem por UUID",
            description = "Recupera os detalhes de uma passagem a partir do seu identificador público (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Passagem encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma passagem encontrada para o UUID informado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de UUID inválido na URL",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER', 'INSPECTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(
            @Parameter(
                    description = "UUID público da passagem",
                    example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                    required = true
            )
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findByIdTicketService.execute(id));
    }

    @Operation(
            summary = "Listar passagens",
            description = "Recupera todas as passagens cadastradas que não passaram por exclusão lógica."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listagem recuperada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll() {
        return ResponseEntity.ok(findAllTicketService.execute());
    }

    @Operation(
            summary = "Atualizar passagem",
            description = "Atualiza os dados de uma passagem existente. O código da passagem não é alterável."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Passagem atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma passagem encontrada para o UUID informado",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(
            @Parameter(
                    description = "UUID público da passagem",
                    example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                    required = true
            )
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ResponseEntity.ok(updateTicketService.execute(id, request));
    }

    @Operation(
            summary = "Cancelar passagem",
            description = "Realiza a exclusão lógica de uma passagem, mantendo o registro no banco para fins de auditoria."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Passagem cancelada com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma passagem encontrada para o UUID informado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "UUID público da passagem",
                    example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                    required = true
            )
            @PathVariable UUID id
    ) {
        deleteTicketService.execute(id);
        return ResponseEntity.noContent().build();
    }
}