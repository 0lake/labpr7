package com.general.models;

import com.general.models.base.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Класс, представляющий сущность "продукт".
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product extends Element implements Comparable<Product> {
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate = LocalDate.now();
    private Integer price;
    private UnitOfMeasure unitOfMeasure;
    private Organization organization;

    public Product(long id, String name, Coordinates coordinates, int price, UnitOfMeasure unitOfMeasure, Organization organization) {
        setId(id);
        this.name = name;
        this.coordinates = coordinates;
        this.price = price;
        this.unitOfMeasure = unitOfMeasure;
        this.organization = organization;
    }

    /**
     * Валидирует правильность полей.
     *
     * @return true, если все верно, иначе false
     */
    @Override
    public boolean validate() {
        if (name == null || name.isEmpty()) return false;
        if (coordinates == null || !coordinates.validate()) return false;
        if (creationDate == null) return false;
        if (price != null && price <= 0) return false;
        if (unitOfMeasure == null) return false;
        if (organization == null) return false;
        return true;
    }

    @Override
    public int compareTo(Product product) {
        int comparison = Integer.compare(this.price, product.price);
        return comparison;
    }
}
