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

/**
 * Controlador REST responsável pela gestão de viagens.
 *
 * <p>A consulta é permitida a qualquer usuário autenticado. O agendamento, a atualização e a
 * exclusão são restritos aos perfis ADMIN e FLEET_MANAGER.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link br.com.passage.api.trip.internal.service.TripAccessService}: o gestor de frota acessa
 * apenas as viagens da própria empresa e os demais perfis acessam as viagens de todas as empresas.</p>
 */
@Tag(name = "Viagens", description = "Saídas agendadas das rotas, com ônibus, motorista e horários")
@RestController
@RequestMapping("api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final CreateTripService createTripService;
    private final FindTripService findTripService;
    private final DeleteTripService deleteTripService;
    private final UpdateTripService updateTripService;

    @Operation(
            summary = "Agendar viagem",
            description = "A viagem herda a empresa da rota e nasce com a situação SCHEDULED."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Viagem agendada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou partida no passado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou rota de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rota, ônibus ou motorista não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Código já cadastrado, rota inativa, ônibus ou motorista de outra empresa ou inativo, "
                            + "chegada antes da partida ou conflito de agenda",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody CreateTripRequest request) {
        TripResponse response = createTripService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar viagem")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Gestor sem empresa vinculada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> findById(
            @Parameter(description = "Identificador da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findTripService.execute(id));
    }

    @Operation(
            summary = "Listar viagens",
            description = "Lista as viagens não excluídas. O gestor vê apenas as da própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de viagens",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TripResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Gestor sem empresa vinculada",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<TripResponse>> findAll() {
        return ResponseEntity.ok(findTripService.executeAll());
    }

    @Operation(
            summary = "Atualizar viagem",
            description = "Atualiza ônibus, motorista, horários, preço e situação. Para cancelar, basta enviar "
                    + "status CANCELED: os demais campos são ignorados. Viagens concluídas ou canceladas não mudam."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Viagem atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TripResponse.class))
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
                    description = "Viagem, ônibus ou motorista não encontrado, ou viagem de outra empresa",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Viagem finalizada, transição de situação inválida, ônibus ou motorista de outra "
                            + "empresa ou inativo, chegada antes da partida ou conflito de agenda",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> update(
            @Parameter(description = "Identificador da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        return ResponseEntity.ok(updateTripService.execute(id, request));
    }

    @Operation(
            summary = "Excluir viagem",
            description = "Exclusão lógica: a viagem some da API, libera a agenda e continua no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Viagem excluída"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Viagem não encontrada, excluída ou de outra empresa",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da viagem", example = "e9235aa8-262f-446f-b6fb-e8cce4c8e8f0", required = true)
            @PathVariable UUID id
    ) {
        deleteTripService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
