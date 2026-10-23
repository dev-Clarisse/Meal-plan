package com.takima.backskeleton.recipe;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchesRecipesByTitle() throws Exception {
        mockMvc.perform(get("/api/v1/recipes").with(jwt()).param("title", "ome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Omelette nature"))
                .andExpect(jsonPath("$.content[0].ingredients.length()").value(2));
    }

    @Test
    void returnsRecipeDetail() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Omelette nature"));
    }

    @Test
    void returnsNotFoundForUnknownRecipe() throws Exception {
        mockMvc.perform(get("/api/v1/recipes/9999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsAndDeletesRecipe() throws Exception {
        String body = "{\"title\":\"Test Pasta\",\"servings\":2,\"instructions\":\"Boil\","
                + "\"ingredients\":[{\"ingredientId\":3,\"quantity\":200,\"unit\":\"G\"}]}";

        String response = mockMvc.perform(post("/api/v1/recipes").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ingredients[0].ingredientName").value("Pâtes"))
                .andReturn().getResponse().getContentAsString();

        String id = response.replaceAll(".*?\"id\":(\\d+).*", "$1");

        mockMvc.perform(delete("/api/v1/recipes/" + id).with(jwt())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/recipes/" + id).with(jwt())).andExpect(status().isNotFound());
    }

    @Test
    void rejectsRecipeWithoutIngredients() throws Exception {
        mockMvc.perform(post("/api/v1/recipes").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Empty\",\"servings\":1,\"ingredients\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
