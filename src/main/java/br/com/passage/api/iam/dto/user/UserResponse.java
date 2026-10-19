package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.internal.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Representação detalhada dos dados de um usuário")
public record UserResponse(

        @Schema(
                description = "Identificador único (UUID) público do usuário",
                example = "7c9e6679-7425-40de-944b-e07fc1f90ae7"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação do registro (UTC)",
                example = "2026-08-13T20:00:00Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última atualização (UTC)",
                example = "2026-08-13T20:10:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Indica se o usuário está ativo no sistema",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Nome completo do usuário",
                example = "Carlos Eduardo Souza"
        )
        String name,

        @Schema(
                description = "Endereço de e-mail cadastrado",
                example = "carlos.souza@viacaoexpressa.com.br"
        )
        String email,

        @Schema(
                description = "Perfil de acesso atribuído",
                example = "FLEET_MANAGER"
        )
        UserRole role,

        @Schema(
                description = "UUID da transportadora vinculada (se houver)",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid
) {
}
