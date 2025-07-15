package com.utp.artesaniamvc.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name="categorias")
public class Categoria {
    
    @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   
    private String nombre;
   
   @OneToMany(mappedBy = "categoria") 
   private List<Subcategoria> subcategorias;
   
   //Constructores

    public Categoria() {
    }

    public Categoria(Integer id, String nombre, List<Subcategoria> subcategorias) {
        this.id = id;
        this.nombre = nombre;
        this.subcategorias = subcategorias;
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

    public List<Subcategoria> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<Subcategoria> subcategorias) {
        this.subcategorias = subcategorias;
    }
    
}

