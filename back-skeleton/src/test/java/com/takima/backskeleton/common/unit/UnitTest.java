package com.takima.backskeleton.common.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnitTest {

    @Test
    void convertsKilogramsToGrams() {
        BigDecimal result = Unit.KG.convertTo(new BigDecimal("1"), Unit.G);

        assertThat(result).isEqualByComparingTo("1000");
    }

    @Test
    void convertsGramsToKilograms() {
        BigDecimal result = Unit.G.convertTo(new BigDecimal("500"), Unit.KG);

        assertThat(result).isEqualByComparingTo("0.5");
    }

    @Test
    void convertsLitersToMilliliters() {
        BigDecimal result = Unit.L.convertTo(new BigDecimal("2"), Unit.ML);

        assertThat(result).isEqualByComparingTo("2000");
    }

    @Test
    void convertsTablespoonToMilliliters() {
        BigDecimal result = Unit.TBSP.convertTo(new BigDecimal("2"), Unit.ML);

        assertThat(result).isEqualByComparingTo("30");
    }

    @Test
    void keepsQuantityWhenUnitIsTheSame() {
        BigDecimal result = Unit.PIECE.convertTo(new BigDecimal("6"), Unit.PIECE);

        assertThat(result).isEqualByComparingTo("6");
    }

    @Test
    void convertsToBaseUnit() {
        assertThat(Unit.KG.toBaseUnit(new BigDecimal("0.5"))).isEqualByComparingTo("500");
    }

    @Test
    void rejectsConversionBetweenDifferentFamilies() {
        assertThatThrownBy(() -> Unit.G.convertTo(new BigDecimal("100"), Unit.ML))
                .isInstanceOf(IncompatibleUnitsException.class);
    }
}
