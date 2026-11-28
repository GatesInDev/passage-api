package br.com.passage.api.iam.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa as credenciais informadas no login.
 *
 * <p>O e-mail é utilizado como identificador de acesso do usuário.</p>
 */
@Schema(description = "Credenciais para autenticação")
public record LoginRequest(

        @Schema(
                description = "E-mail do usuário",
                example = "admin@passage.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @Schema(
                description = "Senha do usuário",
                example = "Admin@123456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A senha é obrigatória.")
        String password
) {}