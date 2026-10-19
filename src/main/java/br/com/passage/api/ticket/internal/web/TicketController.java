package br.com.passage.api.ticket.internal.web;

import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.service.CreateTicketService;
import br.com.passage.api.ticket.internal.service.FindByIdTicketService;
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

import java.util.UUID;

@Tag(name = "Passagens", description = "Endpoints para gestão de passagens no módulo de Tickets")
@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final CreateTicketService createTicketService;
    private final FindByIdTicketService findByIdTicketService;

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
}