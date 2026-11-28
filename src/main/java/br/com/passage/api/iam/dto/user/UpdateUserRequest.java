package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizar um usuário; o e-mail e a senha não são alterados por aqui")
public record UpdateUserRequest(

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
        UUID companyUuid,

        @Schema(
                description = "Se o usuário está ativo; usuários inativos não conseguem autenticar",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O status de ativação é obrigatório.")
        Boolean isActive
) {}