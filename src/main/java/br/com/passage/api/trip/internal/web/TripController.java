package br.com.passage.api.trip.internal.web;

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

import br.com.passage.api.trip.internal.dto.CreateTripRequest;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.dto.UpdateTripRequest;
import br.com.passage.api.trip.internal.service.CreateTripService;
import br.com.passage.api.trip.internal.service.DeleteTripService;
import br.com.passage.api.trip.internal.service.FindTripService;
import br.com.passage.api.trip.internal.service.UpdateTripService;
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

@Tag(name = "Viagens", description = "Endpoints para gestão de viagens no módulo de Transporte")
@RestController
@RequestMapping("api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final CreateTripService createTripService;
    private final FindTripService findTripService;
    private final DeleteTripService deleteTripService;
    private final UpdateTripService updateTripService;

    @Operation(
            summary = "Cadastrar nova viagem",
            description = "Agenda uma nova viagem para uma rota, com ônibus, motorista e horários. A empresa da viagem é a mesma da rota."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Viagem cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso não autorizado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota informada não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de dados (ex: código da viagem já cadastrado no sistema)",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody CreateTripRequest request) {
        TripResponse response = createTripService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar viagem por UUID",
            description = "Busca uma viagem específica no sistema utilizando seu identificador único (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada com o identificador fornecido",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> findById(
            @Parameter(description = "UUID público da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findTripService.execute(id));
    }

    @Operation(
            summary = "Listar todas as viagens",
            description = "Retorna a listagem completa das viagens cadastradas no sistema, excluindo as que foram marcadas como deletadas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de viagens recuperada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TripResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<TripResponse>> findAll() {
        return ResponseEntity.ok(findTripService.executeAll());
    }

    @Operation(
            summary = "Atualizar viagem",
            description = "Atualiza os dados de uma viagem existente, incluindo ônibus, motorista, horários de partida e chegada, preço e situação"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário sem permissão ou viagem pertencente a outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada com o identificador fornecido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de regra de negócio (ex: horário de chegada anterior ou igual ao de partida)",
                    content = @Content
            ),
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> update(
            @Parameter(description = "UUID público da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        return ResponseEntity.ok(updateTripService.execute(id, request));
    }

    @Operation(
            summary = "Remover Viagem (exclusão Lógica)",
            description = "Marca uma viagem como deletada, removendo-a da listagem de viagens."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Viagem removida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada com o identificador fornecido",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID público da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        deleteTripService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
