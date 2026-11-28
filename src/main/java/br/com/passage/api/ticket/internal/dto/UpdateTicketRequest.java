package br.com.passage.api.ticket.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

/**
 * Representa os dados de entrada para a atualização de uma passagem.
 *
 * <p>O código não é alterado. O preço, a data de partida e a empresa não
 * constam aqui: são obtidos da viagem, como na venda.</p>
 */
@Schema(description = "Dados para atualizar uma passagem; preço, partida e empresa vêm da viagem")
public record UpdateTicketRequest(

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
                description = "CPF do passageiro, sem pontuação",
                example = "12345678909",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "O CPF do passageiro é obrigatório.")
        @CPF(message = "O CPF do passageiro é inválido.")
        String passengerDocument,

        @Schema(
                description = "Assento",
                example = "b1ffcd88-8d1c-4fa7-aa5e-5cc8ac291b22",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O identificador do assento é obrigatório.")
        UUID seatId,

        @Schema(
                description = "Viagem",
                example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O identificador da viagem é obrigatório.")
        UUID tripId,

        @Schema(
                description = "Se a passagem está ativa",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "O status de ativação é obrigatório.")
        Boolean isActive
) {}
