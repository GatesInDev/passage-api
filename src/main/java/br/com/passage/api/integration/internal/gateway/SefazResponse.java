package br.com.passage.api.integration.internal.gateway;

import java.time.Instant;

/** Resposta da SEFAZ ao pedido de autorização. */
public record SefazResponse(
        boolean authorized,
        String accessKey,
        String protocol,
        Instant authorizedAt,
        String rejectionReason
) {

    public static SefazResponse authorized(String accessKey, String protocol, Instant authorizedAt) {
        return new SefazResponse(true, accessKey, protocol, authorizedAt, null);
    }

    public static SefazResponse rejected(String reason) {
        return new SefazResponse(false, null, null, null, reason);
    }
}
