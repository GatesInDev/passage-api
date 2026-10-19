package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.internal.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para atualização de dados cadastrais e permissões do usuário")
public record UpdateUserRequest(

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
                description = "Papel ou perfil de permissão atualizado",
                example = "FLEET_MANAGER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O papel (Role) do usuário é obrigatório.")
        UserRole role,

        @Schema(
                description = "UUID público da empresa transportadora vinculada",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid,

        @Schema(
                description = "Define se a conta do usuário está ativa para operar",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O status de ativação é obrigatório.")
        Boolean isActive
) {}