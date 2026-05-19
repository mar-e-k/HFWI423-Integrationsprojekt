package fhdw.de.einkauf_service.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundException_returnsProblemDetailWith404() throws Exception {
        mockMvc.perform(get("/__test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Ressource nicht gefunden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void noSuchElementException_returnsProblemDetailWith404() throws Exception {
        mockMvc.perform(get("/__test/no-such-element"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void outOfBoundsException_returnsProblemDetailWith400() throws Exception {
        mockMvc.perform(get("/__test/out-of-bounds"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Platzierung außerhalb der Regalgrenzen"));
    }

    @Test
    void overlapException_returnsProblemDetailWith409() throws Exception {
        mockMvc.perform(get("/__test/overlap"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Platzierung überschneidet sich"));
    }

    @Test
    void illegalState_returnsProblemDetailWith409() throws Exception {
        mockMvc.perform(get("/__test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void illegalArgument_returnsProblemDetailWith400() throws Exception {
        mockMvc.perform(get("/__test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownException_returnsProblemDetailWith500AndTraceId() throws Exception {
        mockMvc.perform(get("/__test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @RestController
    @RequestMapping("/__test")
    static class StubController {
        @GetMapping("/not-found")
        public void notFound() { throw new EntityNotFoundException("missing"); }

        @GetMapping("/no-such-element")
        public void noSuchElement() { throw new NoSuchElementException("missing"); }

        @GetMapping("/out-of-bounds")
        public void outOfBounds() { throw new OutOfBoundsException("too far"); }

        @GetMapping("/overlap")
        public void overlap() { throw new OverlapException("clash"); }

        @GetMapping("/illegal-state")
        public void illegalState() { throw new IllegalStateException("not now"); }

        @GetMapping("/illegal-argument")
        public void illegalArgument() { throw new IllegalArgumentException("bad arg"); }

        @GetMapping("/boom")
        public void boom() { throw new RuntimeException("kaboom"); }
    }
}
