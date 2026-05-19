package fhdw.de.einkauf_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiSmokeTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void apiDocsEndpoint_returnsValidOpenApiSpec() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Einkauf Service API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    void apiDocs_containsAllControllerTags() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/articles']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/shelves']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/shelf-placements']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/suppliers']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/contingents']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/orders']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cart']").exists());
    }

    @Test
    void swaggerUi_isAccessible() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
