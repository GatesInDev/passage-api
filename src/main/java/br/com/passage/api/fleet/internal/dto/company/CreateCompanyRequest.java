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
                @NotBlank(message = "A Razão Social é obrigatória.")
                @Size(min = 3, max = 255, message = "A Razão Social deve ter entre 3 e 255 caracteres.")
                String corporateName,

                @Schema(
                        description = "Nome fantasia, opcional",
                        example = "Expressa Transportes",
                        minLength = 3,
                        maxLength = 255
                )
                @Size(min = 3, max = 255, message = "O Nome Fantasia deve ter entre 3 e 255 caracteres.")
                String tradeName,

                @Schema(
                        description = "CNPJ com 14 caracteres, sem pontuação e único no sistema",
                        example = "12345678000195",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        minLength = 14,
                        maxLength = 14
                )
                @NotBlank(message = "O Registro de Identificação Fiscal(CNPJ) é obrigatória.")
                @CNPJ(format = CNPJ.Format.ALPHANUMERIC, message = "O Registro de Identificação Fiscal(CNPJ) é inválido.")
                @Size(min = 14, max = 14, message = "A Identificação Fiscal(CNPJ) deve ter 14 caracteres.")
                String documentId,

                @Schema(
                        description = "Inscrição estadual: somente números, ou ISENTO",
                        example = "251234567",
                        maxLength = 15
                )
                @NotBlank(message = "State registration is required.")
                @Size(max = 15, message = "A Inscrição Estadual deve ter no máximo 15 dígitos.")
                @Pattern(regexp = "(?i)^[0-9]{2,14}$|^ISENTO$", message = "Inscrição Estadual inválida.")
                String stateRegistration,

                @Schema(
                        description = "Órgão regulador estadual (ex.: DETER-SC, ARTESP)",
                        example = "DETER-SC",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        minLength = 3,
                        maxLength = 255
                )
                @NotBlank(message = "O Órgão Regulamentador do Estado é obrigatório.")
                @Size(min = 3, max = 255, message = "O Órgão Regulamentador do Estado deve ter entre 3 e 255 caracteres.")
                String stateRegulatoryAgency
        )
{}