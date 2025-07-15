package com.utp.artesaniamvc.model;

import jakarta.persistence.*;

@Entity
@Table(name="subcategorias")
public class Subcategoria {
    
    @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   
    private String nombre;
    
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    
    //Constructores
    public Subcategoria() {
    }

    public Subcategoria(Integer id, String nombre, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }
    
    //Getters and Setters 

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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
 
}

