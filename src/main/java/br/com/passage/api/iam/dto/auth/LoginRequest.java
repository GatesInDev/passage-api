package br.com.passage.api.iam.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de acesso para autenticação no sistema")
public record LoginRequest(

        @Schema(
                description = "E-mail cadastrado do usuário",
                example = "admin@passage.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @Schema(
                description = "Senha de acesso",
                example = "Admin@123456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "A senha é obrigatória.")
        String password
) {}