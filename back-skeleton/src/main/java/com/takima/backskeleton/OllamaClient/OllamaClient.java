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
    private static final int MAX_TOKENS = 512;

    public AiRecipe generate(String prompt, Integer servings) {
        String user = prompt + (servings != null ? "\nNombre de portions : " + servings + "." : "");
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "format", SCHEMA,
                "keep_alive", "30m",
                "options", Map.of("temperature", 0.3,"num_ctx", 2048,"num_predict", MAX_TOKENS),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt()),
                        Map.of("role", "user", "content", user)));
        try {
            return ask(body);
        } catch (JsonProcessingException firstError) {
            // Le modèle part parfois en boucle et sa réponse est coupée : un second essai suffit le plus souvent.
            log.warn("Réponse du modèle invalide, nouvel essai");
            try {
                return ask(body);
            } catch (JsonProcessingException e) {
                throw new RecipeGenerationException("Réponse du modèle invalide", e);
            }
        }
    }

    private AiRecipe ask(Map<String, Object> body) throws JsonProcessingException {
        try {
            JsonNode res = http.post().uri("/api/chat").body(body).retrieve().body(JsonNode.class);
            String content = res.path("message").path("content").asText();
            return mapper.readValue(content, AiRecipe.class);
        } catch (RestClientException e) {
            throw new RecipeGenerationException("Ollama injoignable ou en erreur", e);
        }
    }

    private static String systemPrompt() {
        return """
                Tu es un assistant culinaire. Réponds uniquement avec le JSON demandé.
                - Le champ "instructions" DOIT être une unique chaîne de caractères (string) contenant toutes les étapes numérotées séparées par des retours à la ligne (\\n). Ne mets JAMAIS de tableau pour les instructions. Instructions concises : 4 à 6 étapes, une phrase courte par étape, sans introduction ni conclusion. 
                - Écris le titre et les instructions en français. Le titre donne seulement le nom du plat, sans nombre de personnes.
                - Le champ "servings" reprend le nombre de portions demandé. Les quantités sont les totaux pour toutes ces portions.
                - Liste les ingrédients habituels de ce plat, et seulement ceux-là. Chaque ingrédient est utilisé dans les instructions.
                - Nom d'ingrédient en français, simple et au singulier : Oeuf, Poulet, Fromage, Crème.
                - Unités : G pour le solide, ML pour le liquide, PIECE pour ce qui se compte.
                - Quantités réalistes pour une vraie cuisine, jamais symboliques.
                Exemple pour "salade de tomates pour 2 personnes" :
                {"title":"Salade de tomates","servings":2,"instructions":"1. Couper les tomates.\\n2. Ajouter l'huile et le sel.\\n3. Mélanger et servir.","ingredients":[{"name":"Tomate","quantity":4,"unit":"PIECE"},{"name":"Huile d'olive","quantity":20,"unit":"ML"},{"name":"Sel","quantity":2,"unit":"G"}]}
                """;
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
