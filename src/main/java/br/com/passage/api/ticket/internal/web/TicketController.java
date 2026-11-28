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
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

/**
 * Controlador REST responsável pela venda e gestão de passagens.
 *
 * <h2>Permissões</h2>
 *
 * <ul>
 *     <li>venda, listagem e atualização – ADMIN e TICKET_SELLER;</li>
 *     <li>consulta por identificador – ADMIN, TICKET_SELLER e INSPECTOR;</li>
 *     <li>cancelamento – ADMIN.</li>
 * </ul>
 *
 * <p>O escopo de empresa é definido pelo {@link br.com.passage.api.ticket.internal.service.TicketAccessService}: o administrador acessa
 * as passagens de todas as empresas e os demais perfis apenas as da própria empresa.</p>
 */
@Tag(name = "Passagens", description = "Venda e consulta de passagens")
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
            summary = "Vender passagem",
            description = "Preço, partida e empresa vêm da viagem, que precisa estar SCHEDULED ou BOARDING. Após a venda, o BP-e é emitido de forma assíncrona."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Passagem vendida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão, usuário sem empresa vinculada ou viagem de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Código já cadastrado, viagem fora de venda, assento de outro ônibus ou já vendido",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = createTicketService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar passagem")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Passagem encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passagem não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER', 'INSPECTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(
            @Parameter(description = "Identificador da passagem", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findByIdTicketService.execute(id));
    }

    @Operation(
            summary = "Listar passagens",
            description = "Lista as passagens não excluídas. Quem não é ADMIN vê apenas as da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de passagens",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TicketResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll() {
        return ResponseEntity.ok(findAllTicketService.execute());
    }

    @Operation(
            summary = "Atualizar passagem",
            description = "O código e a empresa não mudam."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Passagem atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou usuário sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passagem não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TICKET_SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(
            @Parameter(description = "Identificador da passagem", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request
    ) {
        return ResponseEntity.ok(updateTicketService.execute(id, request));
    }

    @Operation(
            summary = "Cancelar passagem",
            description = "Exclusão lógica: a passagem some da API, libera o assento e continua no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Passagem cancelada"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passagem não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da passagem", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id
    ) {
        deleteTicketService.execute(id);
        return ResponseEntity.noContent().build();
    }
}