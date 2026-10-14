package com.takima.backskeleton.common.unit;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Unit {
    G(UnitFamily.MASS, "1"),
    KG(UnitFamily.MASS, "1000"),
    ML(UnitFamily.VOLUME, "1"),
    L(UnitFamily.VOLUME, "1000"),
    TSP(UnitFamily.VOLUME, "5"),
    TBSP(UnitFamily.VOLUME, "15"),
    PIECE(UnitFamily.COUNT, "1");

    private static final int SCALE = 6;

    private final UnitFamily family;
    private final BigDecimal factorToBase;

    Unit(UnitFamily family, String factorToBase) {
        this.family = family;
        this.factorToBase = new BigDecimal(factorToBase);
    }

    public UnitFamily getFamily() {
        return family;
    }

    public BigDecimal toBaseUnit(BigDecimal quantity) {
        return quantity.multiply(factorToBase);
    }

    public BigDecimal convertTo(BigDecimal quantity, Unit target) {
        if (family != target.family) {
            throw new IncompatibleUnitsException(this, target);
        }
        return toBaseUnit(quantity).divide(target.factorToBase, SCALE, RoundingMode.HALF_UP);
    }
}
