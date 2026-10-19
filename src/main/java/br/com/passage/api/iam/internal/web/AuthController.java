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

@Tag(name = "Autenticação", description = "Endpoints de login e obtenção de token de acesso")
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final AuthService authService;

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida as credenciais de e-mail e senha do usuário e devolve um token JWT válido."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de requisição inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas (e-mail ou senha incorretos)",
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