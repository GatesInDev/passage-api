package br.com.passage.api.iam.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso gerado no login")
public record TokenResponse(

        @Schema(
                description = "Token JWT, enviado no cabeçalho Authorization das demais requisições",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @Schema(
                description = "Tipo do token",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Validade do token, em segundos",
                example = "86400"
        )
        Long expiresInSeconds
) {}