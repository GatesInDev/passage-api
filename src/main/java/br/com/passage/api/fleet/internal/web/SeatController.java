package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.seat.CreateSeatRequest;
import br.com.passage.api.fleet.internal.dto.seat.SeatResponse;
import br.com.passage.api.fleet.internal.dto.seat.UpdateSeatRequest;
import br.com.passage.api.fleet.internal.service.seat.CreateSeatService;
import br.com.passage.api.fleet.internal.service.seat.DeleteSeatService;
import br.com.passage.api.fleet.internal.service.seat.FindSeatService;
import br.com.passage.api.fleet.internal.service.seat.UpdateSeatService;
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
 * Controlador REST responsável pela gestão dos assentos de um ônibus.
 *
 * <p>Os endpoints são restritos aos perfis ADMIN e FLEET_MANAGER. O gestor de frota acessa
 * apenas os assentos dos ônibus da própria empresa.</p>
 *
 * @see br.com.passage.api.fleet.internal.service.FleetAccessService
 */
@RestController
@RequestMapping("/api/v1/buses/{busId}/seats")
@RequiredArgsConstructor
@Tag(name = "Assentos", description = "Configuração física dos assentos de cada ônibus")
public class SeatController {

    private final CreateSeatService createSeatService;
    private final FindSeatService findSeatService;
    private final UpdateSeatService updateSeatService;
    private final DeleteSeatService deleteSeatService;

    @Operation(
            summary = "Cadastrar assento",
            description = "O número e a posição (fileira e coluna) são únicos no ônibus, mesmo entre assentos excluídos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Assento cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus não encontrado, excluído ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Número ou posição já usados no ônibus",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<SeatResponse> create(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID busId,
            @Valid @RequestBody CreateSeatRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createSeatService.execute(busId, request));
    }

    @Operation(
            summary = "Listar assentos",
            description = "Lista os assentos não excluídos do ônibus."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de assentos",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SeatResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<List<SeatResponse>> findAll(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID busId
    ) {
        return ResponseEntity.ok(findSeatService.executeAll(busId));
    }

    @Operation(summary = "Buscar assento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assento encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus ou assento não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<SeatResponse> findById(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID busId,
            @Parameter(description = "Identificador do assento", example = "7d9e2f10-3a4b-4c5d-9e6f-1a2b3c4d5e6f", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findSeatService.execute(busId, id));
    }

    @Operation(summary = "Atualizar assento")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Assento atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus ou assento não encontrado, excluído ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Número ou posição já usados no ônibus",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<SeatResponse> update(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID busId,
            @Parameter(description = "Identificador do assento", example = "7d9e2f10-3a4b-4c5d-9e6f-1a2b3c4d5e6f", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSeatRequest request
    ) {
        return ResponseEntity.ok(updateSeatService.execute(busId, id, request));
    }

    @Operation(
            summary = "Excluir assento",
            description = "Exclusão lógica: o assento sai da capacidade, mas o número e a posição continuam reservados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Assento excluído"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ônibus ou assento não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID busId,
            @Parameter(description = "Identificador do assento", example = "7d9e2f10-3a4b-4c5d-9e6f-1a2b3c4d5e6f", required = true)
            @PathVariable UUID id
    ) {
        deleteSeatService.execute(busId, id);
        return ResponseEntity.noContent().build();
    }
}
