package com.utp.artesaniamvc.model;

import jakarta.persistence.*;

@Entity
@Table(name = "precio_tamanos")
public class PrecioTamano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;
    
    @ManyToOne
    @JoinColumn(name = "tamano_id")
    private Tamano tamano;
    
    private Double precio;

    public PrecioTamano() {
    }

    public PrecioTamano(Integer id, Producto producto, Tamano tamano, Double precio) {
        this.id = id;
        this.producto = producto;
        this.tamano = tamano;
        this.precio = precio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Tamano getTamano() {
        return tamano;
    }

    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
    
    
}