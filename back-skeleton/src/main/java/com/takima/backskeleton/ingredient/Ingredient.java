package com.takima.backskeleton.ingredient;

import com.takima.backskeleton.common.unit.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_unit", nullable = false, length = 10)
    private Unit defaultUnit;

    protected Ingredient() {
    }

    public Ingredient(String name, Unit defaultUnit) {
        this.name = name;
        this.defaultUnit = defaultUnit;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Unit getDefaultUnit() {
        return defaultUnit;
    }
}
