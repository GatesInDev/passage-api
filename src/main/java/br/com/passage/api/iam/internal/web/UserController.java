package br.com.passage.api.iam.internal.web;

import br.com.passage.api.iam.dto.user.CreateUserRequest;
import br.com.passage.api.iam.dto.user.UpdateUserRequest;
import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.service.user.CreateUserService;
import br.com.passage.api.iam.internal.service.user.DeleteUserService;
import br.com.passage.api.iam.internal.service.user.FindUserService;
import br.com.passage.api.iam.internal.service.user.UpdateUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Usuários", description = "Endpoints de gerenciamento e controle de acesso de usuários e operadores")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final CreateUserService createUserService;
    private final FindUserService findUserService;
    private final UpdateUserService updateUserService;
    private final DeleteUserService deleteUserService;

    @Operation(
            summary = "Cadastrar usuário",
            description = "Cria um novo usuário ou operador no sistema com seu respectivo papel e vínculo corporativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Payload com campos inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já existente na base de dados", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request)
    {
        UserResponse response = createUserService.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Buscar usuário por UUID",
            description = "Recupera os detalhes de um usuário pelo seu identificador público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário localizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @Parameter(description = "UUID público do usuário", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @PathVariable UUID id
    )
    {
        return ResponseEntity.ok(findUserService.execute(id));
    }

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna a listagem completa dos operadores e passageiros registrados."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários recuperada com sucesso",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll()
    {
        return ResponseEntity.ok(findUserService.executeAll());
    }

    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza o nome, papel funcional, status de ativação ou transportadora de um usuário existente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Payload inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "UUID público do usuário", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    )
    {
        return ResponseEntity.ok(updateUserService.execute(id, request));
    }

    @Operation(
            summary = "Remover usuário (Exclusão Lógica)",
            description = "Inativa o acesso do usuário no sistema via soft delete."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID público do usuário", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @PathVariable UUID id
    )
    {
        deleteUserService.execute(id);
        return ResponseEntity.noContent().build();
    }
}