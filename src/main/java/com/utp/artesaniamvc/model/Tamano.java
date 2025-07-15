package com.utp.artesaniamvc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tamanos")
public class Tamano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true)
    private String nombre; // "1L", "3/4L", "1/2L", "1/4L"
    
    private Double factorPrecio; // 1.0, 0.75, 0.5, 0.25

    public Tamano() {
    }

    public Tamano(Integer id, String nombre, Double factorPrecio) {
        this.id = id;
        this.nombre = nombre;
        this.factorPrecio = factorPrecio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getFactorPrecio() {
        return factorPrecio;
    }

    public void setFactorPrecio(Double factorPrecio) {
        this.factorPrecio = factorPrecio;
    }
    
    
}