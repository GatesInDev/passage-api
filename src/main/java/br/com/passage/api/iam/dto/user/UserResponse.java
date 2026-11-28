package br.com.passage.api.iam.dto.user;

import br.com.passage.api.iam.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa os dados de um usuário retornados pela API.
 */
@Schema(description = "Dados de um usuário")
public record UserResponse(

        @Schema(
                description = "Identificador público (UUID) do usuário",
                example = "7c9e6679-7425-40de-944b-e07fc1f90ae7"
        )
        UUID id,

        @Schema(
                description = "Data e hora de criação (UTC)",
                example = "2026-08-13T20:00:00Z"
        )
        Instant createdAt,

        @Schema(
                description = "Data e hora da última alteração (UTC)",
                example = "2026-08-13T20:10:00Z"
        )
        Instant updatedAt,

        @Schema(
                description = "Se o usuário está ativo",
                example = "true"
        )
        boolean isActive,

        @Schema(
                description = "Nome completo",
                example = "Carlos Eduardo Souza"
        )
        String name,

        @Schema(
                description = "E-mail de login",
                example = "carlos.souza@viacaoexpressa.com.br"
        )
        String email,

        @Schema(
                description = "Perfil de acesso",
                example = "FLEET_MANAGER"
        )
        UserRole role,

        @Schema(
                description = "Empresa vinculada, se houver",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        )
        UUID companyUuid
) {
}
