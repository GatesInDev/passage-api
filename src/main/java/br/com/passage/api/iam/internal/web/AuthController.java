package br.com.passage.api.iam.internal.web;

import br.com.passage.api.iam.dto.auth.LoginRequest;
import br.com.passage.api.iam.dto.auth.TokenResponse;
import br.com.passage.api.iam.internal.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST responsável pela autenticação.
 *
 * <p>Expõe o endpoint público de login, que devolve o token JWT utilizado nas demais
 * requisições no cabeçalho {@code Authorization: Bearer}.</p>
 *
 * @see AuthService
 */
@Tag(name = "Autenticação", description = "Login e emissão do token de acesso")
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final AuthService authService;

    @Operation(
            summary = "Autenticar",
            description = "Valida e-mail e senha e devolve o token JWT, a ser enviado como `Authorization: Bearer <token>`."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário inativo ou excluído",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail ou senha incorretos",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request)
    {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}