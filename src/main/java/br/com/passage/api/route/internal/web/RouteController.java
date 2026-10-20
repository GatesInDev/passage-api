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

@Tag(name = "Rotas", description = "Endpoints para gestão de rotas no módulo de Transporte")
@RestController
@RequestMapping("api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final CreateRouteService createRouteService;
    private final FindRouteService findRouteService;
    private final DeleteRouteService deleteRouteService;
    private final UpdateRouteService updateRouteService;

    @Operation(
            summary = "Cadastrar nova rota",
            description = "Cria um novo registro de rota com suas regras de identificação e órgãos reguladores."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rota cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de dados (ex: código da rota já cadastrado no sistema)",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<RouteResponse> create(@Valid @RequestBody CreateRouteRequest request) {
        RouteResponse response = createRouteService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar rota por UUID",
            description = "Busca uma rota específica no sistema utilizando seu identificador único (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rota encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota não encontrada com o identificador fornecido",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> findById(
            @Parameter(description = "UUID público da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findRouteService.execute(id));
    }

    @Operation(
            summary = "Listar todas as rotas",
            description = "Retorna a listagem completa das rotas cadastradas no sistema, excluindo as que foram marcadas como deletadas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de rotas recuperada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RouteResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<RouteResponse>> findAll() {
        return ResponseEntity.ok(findRouteService.executeAll());
    }

    @Operation(
            summary = "Atualizar rota",
            description = "Atualiza os dados de uma rota existente, incluindo nome, cidades de origem e destino, estados, distância, duração estimada, escopo de serviço e órgão regulador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rota atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota não encontrada com o identificador fornecido",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @Parameter(description = "UUID público da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request
    ) {
        return ResponseEntity.ok(updateRouteService.execute(id, request));
    }

    @Operation(
            summary = "Remover Rota (exclusão Lógica)",
            description = "Marca uma rota como deletada, removendo-a da listagem de rotas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rota removida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota não encontrada com o identificador fornecido",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID público da rota", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        deleteRouteService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
