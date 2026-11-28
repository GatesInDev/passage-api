package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.driver.CreateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.UpdateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.service.driver.CreateDriverService;
import br.com.passage.api.fleet.internal.service.driver.DeleteDriverService;
import br.com.passage.api.fleet.internal.service.driver.FindDriverService;
import br.com.passage.api.fleet.internal.service.driver.UpdateDriverService;
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
 * Endpoints de motoristas.
 *
 * <p>Restritos a ADMIN e FLEET_MANAGER; o gestor só enxerga e altera os motoristas da própria empresa.</p>
 */
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Motoristas", description = "Motoristas das empresas")
public class DriverController {

    private final CreateDriverService createDriverService;
    private final FindDriverService findDriverService;
    private final UpdateDriverService updateDriverService;
    private final DeleteDriverService deleteDriverService;

    @Operation(
            summary = "Cadastrar motorista",
            description = "O gestor cadastra sempre na própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Motorista cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponse.class))
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
                    description = "CNH já cadastrada ou empresa não informada pelo ADMIN",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createDriverService.execute(request));
    }

    @Operation(
            summary = "Listar motoristas",
            description = "Lista os motoristas não excluídos. O gestor vê apenas os da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de motoristas",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DriverResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<List<DriverResponse>> findAll() {
        return ResponseEntity.ok(findDriverService.executeAll());
    }

    @Operation(summary = "Buscar motorista")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Motorista encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motorista não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<DriverResponse> findById(
            @Parameter(description = "Identificador do motorista", example = "5b2f8c1e-7a4d-4e3b-9f6a-2d1c0b9a8e7f", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findDriverService.execute(id));
    }

    @Operation(
            summary = "Atualizar motorista",
            description = "Atualiza os dados do motorista; a empresa não muda."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Motorista atualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DriverResponse.class))
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
                    description = "Motorista não encontrado, excluído ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CNH já cadastrada",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<DriverResponse> update(
            @Parameter(description = "Identificador do motorista", example = "5b2f8c1e-7a4d-4e3b-9f6a-2d1c0b9a8e7f", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request
    ) {
        return ResponseEntity.ok(updateDriverService.execute(id, request));
    }

    @Operation(
            summary = "Excluir motorista",
            description = "Exclusão lógica: o motorista some da API e continua no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Motorista excluído"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motorista não encontrado, excluído ou de outra empresa",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do motorista", example = "5b2f8c1e-7a4d-4e3b-9f6a-2d1c0b9a8e7f", required = true)
            @PathVariable UUID id
    ) {
        deleteDriverService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
