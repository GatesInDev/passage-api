package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.bus.CreateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.UpdateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.service.bus.CreateBusService;
import br.com.passage.api.fleet.internal.service.bus.DeleteBusService;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.fleet.internal.service.bus.UpdateBusService;
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
 * Controlador REST responsável pela gestão de ônibus.
 *
 * <p>Os endpoints são restritos aos perfis ADMIN e FLEET_MANAGER. O gestor de frota acessa
 * apenas os ônibus da própria empresa.</p>
 *
 * @see br.com.passage.api.fleet.internal.service.FleetAccessService
 */
@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
@Tag(name = "Ônibus", description = "Ônibus das empresas e a capacidade calculada pelos assentos")
public class BusController {

    private final CreateBusService createBusService;
    private final FindBusService findBusService;
    private final UpdateBusService updateBusService;
    private final DeleteBusService deleteBusService;

    @Operation(
            summary = "Cadastrar ônibus",
            description = "O ônibus nasce sem assentos e com capacidade 0. O gestor cadastra sempre na própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ônibus cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusResponse.class))
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
                    description = "Empresa informada não encontrada ou excluída",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Placa já cadastrada ou empresa não informada pelo ADMIN",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<BusResponse> create(@Valid @RequestBody CreateBusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createBusService.execute(request));
    }

    @Operation(
            summary = "Listar ônibus",
            description = "Lista os ônibus não excluídos. O gestor vê apenas os da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ônibus",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BusResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<List<BusResponse>> findAll() {
        return ResponseEntity.ok(findBusService.executeAll());
    }

    @Operation(summary = "Buscar ônibus")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ônibus encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusResponse.class))
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
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<BusResponse> findById(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findBusService.execute(id));
    }

    @Operation(
            summary = "Atualizar ônibus",
            description = "Atualiza os dados do ônibus; a empresa não muda."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ônibus atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusResponse.class))
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
                    description = "Placa já cadastrada",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<BusResponse> update(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusRequest request
    ) {
        return ResponseEntity.ok(updateBusService.execute(id, request));
    }

    @Operation(
            summary = "Excluir ônibus",
            description = "Exclusão lógica: o ônibus some da API e continua no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Ônibus excluído"
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
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do ônibus", example = "c943e4df-618e-4194-8be6-8a43b4d83e17", required = true)
            @PathVariable UUID id
    ) {
        deleteBusService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
