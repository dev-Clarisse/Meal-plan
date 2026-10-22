package com.takima.backskeleton.ingredient;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngredientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededIngredients() throws Exception {
        mockMvc.perform(get("/api/v1/ingredients").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Egg')]").exists());
    }

    @Test
    void searchesIngredientsByName() throws Exception {
        mockMvc.perform(get("/api/v1/ingredients").with(jwt()).param("query", "tom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tomato"));
    }

    @Test
    void createsIngredientAndRejectsDuplicate() throws Exception {
        String name = "Test-" + java.util.UUID.randomUUID();
        String body = "{\"name\":\"" + name + "\",\"defaultUnit\":\"G\"}";

        mockMvc.perform(post("/api/v1/ingredients").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));

        mockMvc.perform(post("/api/v1/ingredients").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/ingredients").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"defaultUnit\":\"G\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateOfSeededIngredient() throws Exception {
        mockMvc.perform(post("/api/v1/ingredients").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Sugar\",\"defaultUnit\":\"G\"}"))
                .andExpect(status().isConflict());
    }
}
