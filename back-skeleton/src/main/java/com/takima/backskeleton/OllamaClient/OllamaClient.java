package com.takima.backskeleton.OllamaClient;

import java.util.List;
import java.util.Map;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takima.backskeleton.recipe.dtos.AiRecipe;
import com.takima.backskeleton.recipe.exception.RecipeGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class OllamaClient {

    private final RestClient http;
    private final String model;
    private final ObjectMapper mapper;

    

    public OllamaClient(@Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model,
            @Value("${ollama.timeout-seconds}") int timeoutSeconds,
            ObjectMapper mapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.model = model;
        this.mapper = mapper;
    }

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    public AiRecipe generate(String prompt, Integer servings, List<String> knownIngredients) {
        String user = prompt + (servings != null ? " pour " + servings + " personne" + (servings > 1 ? "s" : "") + "." : "");
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "format", SCHEMA,
                "keep_alive", "30m",
                "options", Map.of("temperature", 0.3,"num_ctx", 2048,"num_predict", 1024),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt(knownIngredients)),
                        Map.of("role", "user", "content", user)));
        try {
            JsonNode res = http.post().uri("/api/chat").body(body).retrieve().body(JsonNode.class);
            String content = res.path("message").path("content").asText();
            return mapper.readValue(content, AiRecipe.class);
        } catch (RestClientException e) {
            throw new RecipeGenerationException("Ollama injoignable ou en erreur", e);
        } catch (JsonProcessingException e) {
            throw new RecipeGenerationException("Réponse du modèle invalide", e);
        }
    }

    private static String systemPrompt(List<String> known) {
        return """
                Tu es un assistant culinaire. Réponds uniquement avec le JSON demandé.
                - Le champ "instructions" DOIT être une unique chaîne de caractères (string) contenant toutes les étapes numérotées séparées par des retours à la ligne (\\n). Ne mets JAMAIS de tableau pour les instructions. Instructions concises : 5 à 7 étapes, une phrase courte par étape, sans introduction ni conclusion. 
                - Écris le titre et les instructions en français.
                - Donne des quantités réalistes pour le nombre de portions demandé.
                - Unités autorisées : G, KG, ML, L, TSP, TBSP, PIECE.
                - Pour les ingrédients, utilise EXACTEMENT un nom de cette liste en ANGLAIS quand l'ingrédient correspond : %s.
                - Si aucun ne correspond, donne son nom en ANGLAIS, au singulier.
                """
                .formatted(String.join(", ", known));
    }

    private static final Map<String, Object> SCHEMA = Map.of(
            "type", "object",
            "required", List.of("title", "servings", "instructions", "ingredients"),
            "properties", Map.of(
                    "title", Map.of("type", "string"),
                    "servings", Map.of("type", "integer"),
                    "instructions", Map.of("type", "string"),
                    "ingredients", Map.of(
                            "type", "array",
                            "items", Map.of(
                                    "type", "object",
                                    "required", List.of("name", "quantity", "unit"),
                                    "properties", Map.of(
                                            "name", Map.of("type", "string"),
                                            "quantity", Map.of("type", "number"),
                                            "unit", Map.of("type", "string",
                                                    "enum", List.of("G", "KG", "ML", "L", "TSP", "TBSP", "PIECE")))))));
}
