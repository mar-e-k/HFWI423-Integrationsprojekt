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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @Transactional behebt das ID-Sequenz-Problem in der Neon-DB.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ArticleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository; // Beibehalten, um DB-Interaktionen zu prüfen

    private ArticleRequestDTO validRequest;
    // Basis-URL ohne Slash am Ende, um ihn später gezielt hinzufügen zu können
    private static final String API_URL = "/api/v1/articles";

    @BeforeEach
    void setUp() {
        // Initialisiere ein gültiges Request-Objekt
        validRequest = new ArticleRequestDTO(
                "4008400403337", "Integration Test Artikel", "STK",
                5.00, 19.0, "Hersteller Test", "Lieferant Test", 50, "Beschreibung", true
        );
    }

    /**
     * Helferfunktion zum einfachen Erstellen eines Artikels und zum Abrufen der ID.
     */
    private Long createTestArticle(ArticleRequestDTO request) throws Exception {
        String creationResponse = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Rückgabe der automatisch generierten ID
        return objectMapper.readTree(creationResponse).get("id").asLong();
    }


    // ==================================================================================
    // READ ALL (GET) Tests
    // ==================================================================================
    @Test
    void shouldReadAllArticles() throws Exception {
        // 1. Zwei Artikel anlegen, um sicherzustellen, dass die DB nicht leer ist
        ArticleRequestDTO articleA = new ArticleRequestDTO("11111111", "Artikel A", "STK", 10.0, 19.0, "ManuA", "SuppA", 1, "Desc", true);
        ArticleRequestDTO articleB = new ArticleRequestDTO("22222222", "Artikel B", "STK", 20.0, 19.0, "ManuB", "SuppB", 1, "Desc", true);

        createTestArticle(articleA);
        createTestArticle(articleB);

        // 2. GET-Anfrage ohne Filter ausführen
        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Es wird erwartet, dass mindestens zwei Artikel zurückkommen (oder mehr,
                // falls andere Tests zuvor Artikel erstellt haben und die Transaktion
                // nicht sauber zurückgerollt wurde, was aber bei @Transactional nicht der Fall sein sollte)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(articleA.getName())))
                .andExpect(jsonPath("$[1].name", is(articleB.getName())));
    }

    // ==================================================================================
    // DYNAMISCHE SUCH- UND FILTER-TESTS
    // ==================================================================================

    @Test
    void shouldFilterByManufacturerAndStatus() throws Exception {
        // 1. Testdaten anlegen
        ArticleRequestDTO article1 = new ArticleRequestDTO("1111111111", "TV-Set", "STK", 100.0, 19.0, "HerstellerX", "LieferantA", 10, "Desc", true);
        createTestArticle(article1);

        ArticleRequestDTO article2 = new ArticleRequestDTO("2222222222", "Kühlschrank", "STK", 50.0, 19.0, "HerstellerY", "LieferantA", 5, "Desc", true);
        createTestArticle(article2);

        ArticleRequestDTO article3 = new ArticleRequestDTO("3333333333", "Lampe", "STK", 10.0, 19.0, "HerstellerX", "LieferantA", 0, "Desc", false);
        createTestArticle(article3);


        // 2. Query-Parameter erstellen
        String filterUrl = API_URL + "?manufacturer=HerstellerX&isAvailable=true";

        // 3. Perform Test: Erwartet nur Artikel 1
        mockMvc.perform(get(filterUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].articleNumber", is("1111111111")))
                .andExpect(jsonPath("$[0].manufacturer", is("HerstellerX")))
                // ACHTUNG: Der DTO-Feldname muss hier exakt mit der JSON-Antwort übereinstimmen!
                .andExpect(jsonPath("$[0].isAvailable", is(true)));
    }

    @Test
    void shouldSearchByNameAndArticleNumber() throws Exception {
        // 1. Testdaten anlegen
        ArticleRequestDTO article1 = new ArticleRequestDTO("XYZ456AAAA", "Super-Monitor", "STK", 100.0, 19.0, "Manu", "Supp", 1, "Desc", true);
        createTestArticle(article1);

        ArticleRequestDTO article2 = new ArticleRequestDTO("ABC123AAAA", "Einfacher Monitor", "STK", 50.0, 19.0, "Manu", "Supp", 1, "Desc", true);
        createTestArticle(article2);

        // 2. Query-Parameter (sucht nach Teilwort 'Mon' und Nummer '456')
        String filterUrl = API_URL + "?name=Mon&articleNumber=456";

        // 3. Perform Test: Erwartet nur Artikel 1 (wegen "456")
        mockMvc.perform(get(filterUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString("Monitor")));
    }

    // ==================================================================================
    // UPDATE und DELETE Tests
    // ==================================================================================

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingArticle() throws Exception {
        // Korrigierte URL: Fügt /9999 an API_URL an.
        mockMvc.perform(put(API_URL + "/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldUpdateArticleSuccessfully() throws Exception {

        // 1. Artikel anlegen, um eine ID zu erhalten (Verwendung des Helfers)
        Long id = createTestArticle(validRequest);


        // 2. Update-Request vorbereiten (WICHTIG: Erzeuge ein NEUES DTO!)
        // Du musst ein neues Objekt erstellen, um das 'validRequest'-Feld nicht für andere Tests zu manipulieren.
        ArticleRequestDTO updateRequest = new ArticleRequestDTO(
                validRequest.getArticleNumber(),
                validRequest.getName(),
                validRequest.getUnit(),
                10.00, // NEUER PREIS
                validRequest.getTaxRatePercent(),
                validRequest.getManufacturer(),
                validRequest.getSupplier(),
                validRequest.getStockLevel(),
                validRequest.getDescription(),
                validRequest.getIsAvailable()
        );


        // 3. PUT-Request ausführen
        // HIER ist die kritische Korrektur: put(API_URL + "/" + id)
        mockMvc.perform(put(API_URL + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchasePrice", is(10.00)))
                .andExpect(jsonPath("$.sellingPrice", closeTo(11.90, 0.001))); // 10.00 * 1.19
    }

    @Test
    void shouldDeleteArticleSuccessfully() throws Exception {
        // 1. Artikel anlegen, um eine gültige ID zu erhalten
        Long idToDelete = createTestArticle(validRequest);

        // Sanity Check: Prüfen, ob der Artikel existiert
        mockMvc.perform(get(API_URL + "/" + idToDelete)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 2. DELETE-Request ausführen
        mockMvc.perform(delete(API_URL + "/" + idToDelete)
                        .contentType(MediaType.APPLICATION_JSON))
                // Erwarte Status 204 No Content, da DELETE typischerweise keinen Body zurückgibt
                .andExpect(status().isNoContent());

        // 3. Verifizierung: Prüfen, ob der Artikel wirklich gelöscht wurde (sollte 404 zurückgeben)
        mockMvc.perform(get(API_URL + "/" + idToDelete)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}