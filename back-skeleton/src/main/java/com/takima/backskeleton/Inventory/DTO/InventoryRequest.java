package com.takima.backskeleton.Inventory.DTO;

import com.takima.backskeleton.Inventory.models.Unit;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryRequest(
    @NotBlank @Size(max = 120)
    String name,

    @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 9, fraction = 3)
    BigDecimal quantity,

    @NotNull
    Unit unit,

    LocalDate expiryDate
) {}