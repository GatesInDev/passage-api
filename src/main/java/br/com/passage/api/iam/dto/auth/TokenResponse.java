package br.com.passage.api.iam.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de autenticação JWT e metadados de sessão")
public record TokenResponse(

        @Schema(
                description = "Token de acesso no padrão JWT",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @Schema(
                description = "Tipo do token de autorização",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Tempo de validade do token em segundos",
                example = "86400"
        )
        Long expiresInSeconds
) {}