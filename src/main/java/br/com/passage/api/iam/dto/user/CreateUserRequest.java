package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Representa os dados de entrada para o cadastro de um usuário.
 */
@Schema(description = "Dados para cadastrar um usuário")
public record CreateUserRequest(

        @Schema(
                description = "Nome completo",
                example = "Carlos Eduardo Souza",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 150
        )
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String name,

        @Schema(
                description = "E-mail de login, único no sistema",
                example = "carlos.souza@viacaoexpressa.com.br",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 150
        )
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Schema(
                description = "Senha inicial",
                example = "SenhaForte@2026",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 6
        )
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres.")
        String password,

        @Schema(
                description = "Perfil de acesso",
                example = "FLEET_MANAGER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O papel (Role) do usuário é obrigatório.")
        UserRole role,

        @Schema(
                description = "Empresa vinculada; não se aplica a administradores e passageiros",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid
) {}