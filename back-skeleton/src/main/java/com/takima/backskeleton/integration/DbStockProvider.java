package com.takima.backskeleton.integration;

import com.takima.backskeleton.Inventory.DAO.InventoryRepository;
import com.takima.backskeleton.Inventory.models.Inventory;
import com.takima.backskeleton.common.unit.Unit;
import com.takima.backskeleton.ingredient.IngredientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class DbStockProvider implements StockProvider {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public DbStockProvider(InventoryRepository inventoryRepository,
                           IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getAvailableStock(UUID userId) {
        Map<String, Long> idByName = new HashMap<>();
        ingredientRepository.findAll().forEach(i -> idByName.put(normalize(i.getName()), i.getId()));

        LocalDate today = LocalDate.now();
        Map<Long, BigDecimal> stock = new HashMap<>();
        for (Inventory item : inventoryRepository.findAllByUserIdOrderByExpiryDateAsc(userId)) {
            if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(today)) {
                continue; // les aliments périmés ne comptent pas
            }
            Long ingredientId = idByName.get(normalize(item.getName()));
            if (ingredientId == null) {
                continue; // nom absent du catalogue
            }
            Unit unit;
            try {
                unit = Unit.valueOf(item.getUnit().name());
            } catch (IllegalArgumentException e) {
                continue;
            }
            stock.merge(ingredientId, unit.toBaseUnit(item.getQuantity()), BigDecimal::add);
        }
        return stock;
    }

    private static String normalize(String s) {
        String lower = s.trim().toLowerCase(Locale.ROOT).replace("œ", "oe");
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}