package com.takima.backskeleton.integration;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Contrat avec Clarisse. Clé = id de l'ingrédient, valeur = quantité totale en unité de base,
 * sans les aliments périmés.
 */
public interface StockProvider {
    Map<Long, BigDecimal> getAvailableStock(UUID userId);
}
