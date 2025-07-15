package com.utp.artesaniamvc.model;

import jakarta.persistence.*;


@Entity
@Table(name = "asuntos")
public class Asunto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // Los IDs serán predefinidos, no autogenerados.
    
    private String description;
    
    // Constructor sin parámetros
    public Asunto() {}
    
    // Constructor para facilitar la creación de instancias
    public Asunto (Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
