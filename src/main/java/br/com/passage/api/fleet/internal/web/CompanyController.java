package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.company.*;
import br.com.passage.api.fleet.internal.service.company.*;
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

import java.util.UUID;
import java.util.List;

/**
 * Controlador REST responsável pela gestão de empresas transportadoras.
 *
 * <p>O cadastro e a exclusão são restritos ao perfil ADMIN. O gestor de frota pode consultar e
 * atualizar apenas a própria empresa.</p>
 */
@Tag(name = "Empresas", description = "Empresas transportadoras, donas da frota e das rotas")
@RestController
@RequestMapping("api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CreateCompanyService createCompanyService;
    private final FindByIdCompanyService findByIdCompanyService;
    private final UpdateCompanyService updateCompanyService;
    private final FindAllCompanyService findAllCompanyService;
    private final DeleteCompanyService deleteCompanyService;

    @Operation(
            summary = "Listar empresas",
            description = "Lista as empresas não excluídas. O gestor vê apenas a própria."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de empresas",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CompanyResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> findAll() {
        return ResponseEntity.ok(findAllCompanyService.execute());
    }

    @Operation(
            summary = "Excluir empresa",
            description = "Exclusão lógica: a empresa e a frota dela somem da API e continuam no banco."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Empresa excluída"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou excluída",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da empresa", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id
    ) {
        deleteCompanyService.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cadastrar empresa")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empresa cadastrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyResponse.class))
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
                    description = "CNPJ já cadastrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = createCompanyService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar empresa")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou empresa diferente da do gestor",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou excluída",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> findById(
            @Parameter(description = "Identificador da empresa", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findByIdCompanyService.execute(id));
    }

    @Operation(
            summary = "Atualizar empresa",
            description = "Substitui os dados cadastrais. O gestor só altera a própria empresa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa atualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão ou empresa diferente da do gestor",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empresa não encontrada ou excluída",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CNPJ já cadastrado",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @PatchMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(
            @Parameter(description = "Identificador da empresa", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        return ResponseEntity.ok(updateCompanyService.execute(id, request));
    }
}
