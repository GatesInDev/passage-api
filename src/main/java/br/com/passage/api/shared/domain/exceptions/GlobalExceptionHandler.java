package br.com.passage.api.shared.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.net.URI;
import java.time.Instant;

/**
 * Tratador global das exceções lançadas pelos controllers.
 *
 * <p>Converte as exceções da aplicação em respostas {@link ProblemDetail} (RFC 7807), mantendo
 * o mesmo formato de erro em todos os módulos.</p>
 *
 * <h2>Mapeamento</h2>
 *
 * <ul>
 *     <li>{@link ResourceNotFoundException} – {@code 404 Not Found};</li>
 *     <li>{@link BusinessException} – {@code 409 Conflict};</li>
 *     <li>{@code DataIntegrityViolationException} – {@code 409 Conflict};</li>
 *     <li>{@code MethodArgumentNotValidException} – {@code 400 Bad Request}.</li>
 * </ul>
 *
 * @see BusinessException
 * @see ResourceNotFoundException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata a ausência de um recurso.
     *
     * @param e exceção lançada
     * @return resposta {@code 404} com a mensagem da exceção
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Trata a violação de uma restrição do banco de dados, como um valor único repetido.
     *
     * <p>A mensagem técnica do banco não é exposta na resposta.</p>
     *
     * @param e exceção lançada
     * @return resposta {@code 409} com uma mensagem genérica
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Os dados informados conflitam com um registro existente ou violam uma restrição de integridade.");
    }

    /**
     * Trata a violação de uma regra de negócio.
     *
     * @param e exceção lançada
     * @return resposta {@code 409} com a mensagem da exceção
     */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problemDetail.setTitle("Conflito de Regra de Negócio");
        problemDetail.setType(URI.create("https://api.passage.com.br/errors/business-conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    /**
     * Trata os erros de validação do corpo da requisição.
     *
     * <p>Os campos rejeitados são listados na propriedade {@code invalidFields}, no formato
     * {@code campo: mensagem}.</p>
     *
     * @param e exceção lançada
     * @return resposta {@code 400} com os campos inválidos
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos.");
        problemDetail.setTitle("Erro de Validação");

        var fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        problemDetail.setProperty("invalidFields", fieldErrors);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
