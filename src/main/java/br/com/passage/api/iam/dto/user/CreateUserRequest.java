package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.internal.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Payload para criação de um novo operador ou usuário no sistema")
public record CreateUserRequest(

        @Schema(
                description = "Nome completo do usuário",
                example = "Carlos Eduardo Souza",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 150
        )
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name,

        @Schema(
                description = "E-mail corporativo ou pessoal para login",
                example = "carlos.souza@viacaoexpressa.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 150
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Schema(
                description = "Senha inicial de acesso (mínimo de 6 caracteres)",
                example = "SenhaForte@2026",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 6
        )
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres.")
        String password,

        @Schema(
                description = "Papel ou perfil de permissão do usuário",
                example = "FLEET_MANAGER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O papel (Role) do usuário é obrigatório.")
        UserRole role,

        @Schema(
                description = "UUID público da empresa transportadora vinculada (opcional para ADMIN ou passageiros avulsos)",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid
) {}