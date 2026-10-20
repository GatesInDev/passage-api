package br.com.passage.api.shared.domain.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {
    @Test
    void mapsConflictsAndMissingResourcesToHttpStatus() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ErrorController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/business-conflict")).andExpect(status().isConflict());
        mvc.perform(get("/database-conflict")).andExpect(status().isConflict());
        mvc.perform(get("/not-found")).andExpect(status().isNotFound());
    }

    @RestController
    static class ErrorController {
        @GetMapping("/business-conflict")
        void businessConflict() { throw new BusinessException("CNPJ já cadastrado"); }

        @GetMapping("/database-conflict")
        void databaseConflict() { throw new DataIntegrityViolationException("duplicate key"); }

        @GetMapping("/not-found")
        void notFound() { throw new ResourceNotFoundException("Usuário não encontrado"); }
    }
}
