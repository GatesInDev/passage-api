package br.com.passage.api.fleet.internal.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CNPJ;

@Schema(description = "Dados para cadastrar uma empresa")
public record CreateCompanyRequest
        (
                @Schema(
                        description = "Razão social",
                        example = "Viação Expressa Santa Catarina S.A.",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        minLength = 3,
                        maxLength = 255
                )
                @NotBlank(message = "A razão social é obrigatória.")
                @Size(min = 3, max = 255, message = "A razão social deve ter entre 3 e 255 caracteres.")
                String corporateName,

                @Schema(
                        description = "Nome fantasia, opcional",
                        example = "Expressa Transportes",
                        minLength = 3,
                        maxLength = 255
                )
                @Size(min = 3, max = 255, message = "O nome fantasia deve ter entre 3 e 255 caracteres.")
                String tradeName,

                @Schema(
                        description = "CNPJ com 14 caracteres, sem pontuação e único no sistema",
                        example = "12345678000195",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        minLength = 14,
                        maxLength = 14
                )
                @NotBlank(message = "O CNPJ é obrigatório.")
                @CNPJ(format = CNPJ.Format.ALPHANUMERIC, message = "O CNPJ é inválido.")
                @Size(min = 14, max = 14, message = "O CNPJ deve ter 14 caracteres.")
                String documentId,

                @Schema(
                        description = "Inscrição estadual: somente números, ou ISENTO",
                        example = "251234567",
                        maxLength = 15
                )
                @NotBlank(message = "A inscrição estadual é obrigatória.")
                @Size(max = 15, message = "A inscrição estadual deve ter no máximo 15 caracteres.")
                @Pattern(regexp = "(?i)^[0-9]{2,14}$|^ISENTO$", message = "A inscrição estadual é inválida.")
                String stateRegistration,

                @Schema(
                        description = "Órgão regulador estadual (ex.: DETER-SC, ARTESP)",
                        example = "DETER-SC",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        minLength = 3,
                        maxLength = 255
                )
                @NotBlank(message = "O órgão regulador é obrigatório.")
                @Size(min = 3, max = 255, message = "O órgão regulador deve ter entre 3 e 255 caracteres.")
                String stateRegulatoryAgency
        )
{}