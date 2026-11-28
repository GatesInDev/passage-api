package br.com.passage.api.integration.internal.gateway;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Emissor simulado da SEFAZ, sem tráfego externo.
 *
 * <p>Gera chave de acesso (44 dígitos) e protocolo (15 dígitos) no formato real e recusa documentos
 * sem valor positivo. A integração real exige certificado digital, assinatura do XML e os
 * webservices da UF.</p>
 */
@Component
public class MockSefazGateway implements SefazGateway {

    /** Código da unidade federativa de Santa Catarina. */
    private static final String UF_CODE = "42";

    /** Código numérico do BP-e na tabela de documentos fiscais. */
    private static final String MODEL_CODE = "63";

    private static final DateTimeFormatter PERIOD =
            DateTimeFormatter.ofPattern("yyMM").withZone(ZoneOffset.UTC);

    private final SecureRandom random = new SecureRandom();

    @Override
    public SefazResponse authorize(FiscalDocument document) {
        if (document.getAmount() == null
                || document.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return SefazResponse.rejected(
                    "Rejeicao: valor total do documento deve ser maior que zero.");
        }

        return SefazResponse.authorized(
                generateAccessKey(document),
                generateProtocol(),
                Instant.now());
    }

    /**
     * Chave de acesso: UF, período, CNPJ do emitente, modelo, série, número, forma de emissão,
     * código numérico e dígito verificador.
     */
    private String generateAccessKey(FiscalDocument document) {
        String base = UF_CODE
                + PERIOD.format(Instant.now())
                + digitsFrom(document.getCompanyUuid().toString(), 14)
                + MODEL_CODE
                + "%03d".formatted(document.getSeries())
                + "%09d".formatted(document.getNumber())
                + "1"
                + "%08d".formatted(random.nextInt(100_000_000));

        return base + checkDigit(base);
    }

    private String generateProtocol() {
        return "%015d".formatted(Math.abs(random.nextLong()) % 1_000_000_000_000_000L);
    }

    /** Extrai os dígitos do texto, completando com zeros à esquerda. */
    private String digitsFrom(String value, int length) {
        String digits = value.replaceAll("\\D", "");
        return digits.length() >= length
                ? digits.substring(0, length)
                : "0".repeat(length - digits.length()) + digits;
    }

    /** Dígito verificador em módulo 11, padrão dos documentos fiscais. */
    private int checkDigit(String key) {
        int weight = 2;
        int sum = 0;

        for (int i = key.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(key.charAt(i)) * weight;
            weight = weight == 9 ? 2 : weight + 1;
        }

        int remainder = sum % 11;
        return remainder <= 1 ? 0 : 11 - remainder;
    }
}
