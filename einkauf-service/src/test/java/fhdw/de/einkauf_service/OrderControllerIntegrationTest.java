package fhdw.de.einkauf_service;

import fhdw.de.einkauf_service.controller.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private OrderController orderController;

    @Test
    public void testGetOrderHistory() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Teste den GET /api/v1/orders Endpunkt ohne Parameter
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrderHistoryWithFilters() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Teste mit Filtern
        mockMvc.perform(get("/api/v1/orders")
                .param("status", "PLACED")
                .param("supplierId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrderDetails() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Annahme: Es gibt eine Bestellung mit ID 1 (muss in DB vorhanden sein oder Testdaten erstellen)
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
    }

    // Weitere Tests können hier hinzugefügt werden, z.B. für submitOrderFromCart (benötigt Warenkorb-Daten)
}
