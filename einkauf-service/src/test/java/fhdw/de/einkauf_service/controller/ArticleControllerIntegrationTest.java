package fhdw.de.einkauf_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*; // Für JSON-Prüfungen

@SpringBootTest
@AutoConfigureMockMvc // Konfiguriert MockMvc für die Controller-Tests
public class ArticleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Zum Simulieren von HTTP-Anfragen

    @Autowired
    private ObjectMapper objectMapper; // Zum Konvertieren von Java-Objekten in JSON

    @Autowired
    private ArticleRepository articleRepository; // Zum Bereinigen der DB zwischen Tests

    private ArticleRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        // DB leeren, um Testisolation zu gewährleisten
        articleRepository.deleteAll();

        validRequest = new ArticleRequestDTO(
                "4008400403337", "Integration Test Artikel", "STK",
                5.00, 19.0, "Hersteller Test", "Lieferant Test", 50, "Beschreibung"
        );
    }

    // ==================================================================================
    // A. READ ALL (GET) Tests
    // ==================================================================================

    @Test
    void shouldReturnEmptyListForGetAllWhenNoArticlesExist() throws Exception {
        mockMvc.perform(get("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Prüft auf ein leeres JSON-Array
    }

    // ==================================================================================
    // B. VALIDIERUNG (POST) Tests
    // ==================================================================================

    @Test
    void shouldReturnBadRequestWhenArticleNameIsMissing() throws Exception {
        ArticleRequestDTO invalidRequest = validRequest;
        invalidRequest.setName(""); // Fehlende Validierung

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Erwartet 400 Bad Request
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateArticleNumber() throws Exception {
        // 1. Ersten Artikel anlegen (Sollte 201 zurückgeben)
        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        // 2. Zweiten Artikel mit gleicher GTIN versuchen anzulegen
        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict()); // Erwartet 409 Conflict
    }

    // ==================================================================================
    // C. UPDATE und DELETE Tests
    // ==================================================================================

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingArticle() throws Exception {
        // Versucht, Artikel mit ID 99 zu aktualisieren, der nicht existiert
        mockMvc.perform(put("/api/v1/articles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound()); // Erwartet 404 Not Found
    }

    @Test
    void shouldUpdateArticleSuccessfully() throws Exception {
        // 1. Artikel anlegen, um eine ID zu erhalten
        String creationResponse = mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(creationResponse).get("id").asLong();

        // 2. Update-Request vorbereiten (Preis ändern)
        ArticleRequestDTO updateRequest = validRequest;
        updateRequest.setPurchasePrice(10.00); // Neuer Preis

        // 3. PUT-Request ausführen
        mockMvc.perform(put("/api/v1/articles/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchasePrice", is(10.00)))
                .andExpect(jsonPath("$.sellingPrice", closeTo(11.90, 0.001))); // 10.00 * 1.19
    }
}