package br.com.passage.api.shared.domain.exceptions;

/**
 * Violação de uma regra de negócio, respondida como 409.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}