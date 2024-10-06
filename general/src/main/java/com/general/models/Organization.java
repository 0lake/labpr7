package com.general.models;

import com.general.models.base.Validatable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Класс дома
 */
@Data
@NoArgsConstructor
public class Organization implements Validatable, Serializable {
    private static final transient Map<Integer, Organization> organizations = new HashMap<>();

    @NonNull
    private String name; // Поле не может быть null
    private Integer year; // Значение поля должно быть больше 0
    private String fullname;

    public Organization(@NonNull String name, String fullname, Integer year) {
        this.name = name;
        this.fullname = fullname;
        this.year = year;
    }

    /**
     * Валидирует правильность полей.
     * @return true, если все верно, иначе false
     */
    @Override
    public boolean validate() {
        return name != null && !name.isEmpty() &&
                year != null && year > 0 &&
                fullname != null && !fullname.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization organization = (Organization) o;
        return  Objects.equals(name, organization.name) &&
                Objects.equals(year, organization.year) &&
                Objects.equals(fullname, organization.fullname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fullname, year);
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", fullname='" + fullname + '\''+
                ", year=" + year +
                '}';
    }
}
