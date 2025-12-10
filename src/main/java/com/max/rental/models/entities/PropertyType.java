package com.max.rental.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "property_type")
@Getter
@Setter
public class PropertyType extends BaseEntity {
    private String type;
}