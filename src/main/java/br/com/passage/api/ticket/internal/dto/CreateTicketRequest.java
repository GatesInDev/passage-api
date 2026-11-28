package br.com.passage.api.ticket.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

/**
 * Dados informados na venda de uma passagem.
 *
 * <p>Preço, horário de partida e empresa não constam aqui: são obtidos da
 * viagem no momento da venda, para que não possam ser escolhidos por quem
 * emite o bilhete.</p>
 */
@Schema(description = "Payload com os dados necessários para o cadastro de uma nova passagem")
public record CreateTicketRequest(

        @Schema(
                description = "Código único da passagem",
                example = "PASS-2026-001",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 64
        )
        @NotBlank(message = "O código da passagem é obrigatório.")
        @Size(max = 64, message = "O código da passagem deve ter no máximo 64 caracteres.")
        String code,

        @Schema(
                description = "Nome completo do passageiro",
                example = "João da Silva",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 3,
                maxLength = 100
        )
        @NotBlank(message = "O nome do passageiro é obrigatório.")
        @Size(min = 3, max = 100, message = "O nome do passageiro deve ter entre 3 e 100 caracteres.")
        String passengerName,

        @Schema(
                description = "CPF do passageiro (somente números, 11 dígitos)",
                example = "12345678909",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O CPF do passageiro é obrigatório.")
        @CPF(message = "O CPF do passageiro é inválido.")
        String passengerDocument,

        @Schema(
                description = "Identificador UUID do assento reservado",
                example = "b1ffcd88-8d1c-4fa7-aa5e-5cc8ac291b22",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O identificador do assento é obrigatório.")
        UUID seatId,

        @Schema(
                description = "Identificador UUID da viagem da passagem",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O identificador da viagem é obrigatório.")
        UUID tripId
) {}
