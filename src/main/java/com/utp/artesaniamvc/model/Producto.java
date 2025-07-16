package com.utp.artesaniamvc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="productos")
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   
    @Column(nullable = false, unique = true)
    private String nombreProducto;
    
    @Column(columnDefinition = "TEXT")  
    private String descripcion;
    private Double precio;
    private String imageUrl;
    
    private LocalDateTime createdAt;
    
    private Double stockOpenPlaza;
    private Double stockUDEP;
    private Double stockActual;
    
    @Column(nullable = false)
    private boolean disponiblePorTemporada;
    
    @ManyToOne
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    // Nuevo campo para los tamaños de helado
    @OneToMany(mappedBy = "producto")
    private List<PrecioTamano> preciosPorTamano;
    
     // NUEVA RELACIÓN: Especificaciones del producto
    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EspecificacionProducto especificacion;

    public Producto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getStockOpenPlaza() {
        return stockOpenPlaza;
    }

    public void setStockOpenPlaza(Double stockOpenPlaza) {
        this.stockOpenPlaza = stockOpenPlaza;
    }

    public Double getStockUDEP() {
        return stockUDEP;
    }

    public void setStockUDEP(Double stockUDEP) {
        this.stockUDEP = stockUDEP;
    }

    public Double getStockActual() {
        return stockActual;
    }

    public void setStockActual(Double stockActual) {
        this.stockActual = stockActual;
    }

    public boolean isDisponiblePorTemporada() {
        return disponiblePorTemporada;
    }

    public void setDisponiblePorTemporada(boolean disponiblePorTemporada) {
        this.disponiblePorTemporada = disponiblePorTemporada;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Subcategoria getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(Subcategoria subcategoria) {
        this.subcategoria = subcategoria;
    }

    public List<PrecioTamano> getPreciosPorTamano() {
        return preciosPorTamano;
    }

    public void setPreciosPorTamano(List<PrecioTamano> preciosPorTamano) {
        this.preciosPorTamano = preciosPorTamano;
    }
 
    public EspecificacionProducto getEspecificacion() {
        return especificacion;
    }
    
    public void setEspecificacion(EspecificacionProducto especificacion) {
        this.especificacion = especificacion;
        if (especificacion != null) {
            especificacion.setProducto(this);
        }
    }
    
    // Método helper para verificar si tiene especificaciones
    public boolean tieneEspecificaciones() {
        return especificacion != null;
    }
  
}
