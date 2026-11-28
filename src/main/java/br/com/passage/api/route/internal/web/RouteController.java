package br.com.passage.api.route.internal.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.passage.api.route.internal.dto.CreateRouteRequest;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.dto.UpdateRouteRequest;
import br.com.passage.api.route.internal.service.CreateRouteService;
import br.com.passage.api.route.internal.service.DeleteRouteService;
import br.com.passage.api.route.internal.service.FindRouteService;
import br.com.passage.api.route.internal.service.UpdateRouteService;
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

/**
 * Controlador REST responsável pela gestão de rotas.
 *
 * <p>A consulta é permitida a qualquer usuário autenticado. O cadastro, a atualização e a
 * exclusão são restritos aos perfis ADMIN e FLEET_MANAGER.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link br.com.passage.api.route.internal.service.RouteAccessService}: o gestor de frota acessa
 * apenas as rotas da própria empresa e os demais perfis acessam as rotas de todas as empresas.</p>
 */
@Tag(name = "Rotas", description = "Linhas operadas pelas empresas")
@RestController
@RequestMapping("api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final CreateRouteService createRouteService;
    private final FindRouteService findRouteService;
    private final DeleteRouteService deleteRouteService;
    private final UpdateRouteService updateRouteService;

    @Operation(
            summary = "Cadastrar rota",
            description = "O gestor cadastra na própria empresa; o ADMIN informa a empresa no payload."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rota cadastrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
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
                    responseCode = "409",
                    description = "Código já cadastrado, empresa não informada ou origem, destino e abrangência incoerentes",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody CreateRouteRequest request) {
        RouteResponse response = createRouteService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar rota")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rota encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Gestor sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> findById(
            @Parameter(description = "Identificador da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findRouteService.execute(id));
    }

    @Operation(
            summary = "Listar rotas",
            description = "Lista as rotas não excluídas. O gestor vê apenas as da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de rotas",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RouteResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Gestor sem empresa vinculada",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<RouteResponse>> findAll() {
        return ResponseEntity.ok(findRouteService.executeAll());
    }

    @Operation(
            summary = "Atualizar rota",
            description = "Atualiza todos os dados da rota, exceto o código e a empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rota atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
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
                    description = "Rota não encontrada, excluída ou de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Origem, destino e abrangência incoerentes",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @Parameter(description = "Identificador da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request
    ) {
        return ResponseEntity.ok(updateRouteService.execute(id, request));
    }

    @Operation(
            summary = "Excluir rota",
            description = "Exclusão lógica: a rota some da API e continua no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rota excluída"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        deleteRouteService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
