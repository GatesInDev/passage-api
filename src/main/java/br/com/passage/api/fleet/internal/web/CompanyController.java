package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.dto.company.CreateCompanyRequest;
import br.com.passage.api.fleet.internal.service.company.CreateCompanyService;
import br.com.passage.api.fleet.internal.service.company.FindByIdCompanyService;
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

@Tag(name = "Empresas", description = "Endpoints para gestão de empresas transportadoras no módulo de Frota")
@RestController
@RequestMapping("api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CreateCompanyService createCompanyService;
    private final FindByIdCompanyService findByIdCompanyService;

    @Operation(
            summary = "Cadastrar nova empresa",
            description = "Cria um novo registro de empresa transportadora com suas regras de identificação e órgãos reguladores."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empresa cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CompanyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (erro de validação dos campos do payload)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de dados (ex: CNPJ já cadastrado no sistema)",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = createCompanyService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar empresa por UUID",
            description = "Recupera os detalhes cadastrais de uma empresa transportadora a partir do seu identificador público (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Empresa encontrada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhuma empresa encontrada para o UUID informado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de UUID inválido na URL",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> findById(
            @Parameter(
                    description = "UUID público da empresa transportadora",
                    example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                    required = true
            )
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(findByIdCompanyService.execute(id));
    }
}