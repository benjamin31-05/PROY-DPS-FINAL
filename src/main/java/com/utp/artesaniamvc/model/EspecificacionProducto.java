
package com.utp.artesaniamvc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "especificaciones_producto")
public class EspecificacionProducto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    
    // Especificaciones técnicas
    @Column(name = "material_principal")
    private String materialPrincipal;
    
    @Column(name = "tecnica_elaboracion", columnDefinition = "TEXT")
    private String tecnicaElaboracion;
    
    @Column(name = "acabado")
    private String acabado;
    
    @Column(name = "altura")
    private String altura;
    
    @Column(name = "diametro_boca")
    private String diametroBoca;
    
    @Column(name = "diametro_superior")
    private String diametroSuperior;
    
    @Column(name = "capacidad")
    private String capacidad;
    
    @Column(name = "peso")
    private String peso;
    
    @Column(name = "color", columnDefinition = "TEXT")
    private String color;
    
    // Historia y origen
    @Column(name = "origen", columnDefinition = "TEXT")
    private String origen;
    
    @Column(name = "historia", columnDefinition = "TEXT")
    private String historia;
    
    @Column(name = "tradicion_cultural", columnDefinition = "TEXT")
    private String tradicionCultural;
    
    @Column(name = "artesano_creador")
    private String artesanoCreador;
    
    @Column(name = "region_procedencia")
    private String regionProcedencia;
    
    // Cuidados y mantenimiento
    @Column(name = "cuidados_generales", columnDefinition = "TEXT")
    private String cuidadosGenerales;
    
    @Column(name = "limpieza", columnDefinition = "TEXT")
    private String limpieza;
    
    @Column(name = "almacenamiento", columnDefinition = "TEXT")
    private String almacenamiento;
    
    @Column(name = "precauciones", columnDefinition = "TEXT")
    private String precauciones;
    
    @Column(name = "durabilidad", columnDefinition = "TEXT")
    private String durabilidad;
    
    // Información adicional
    @Column(name = "codigo_producto")
    private String codigoProducto;
    
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructores
    public EspecificacionProducto() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public EspecificacionProducto(Producto producto) {
        this();
        this.producto = producto;
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    
    public String getMaterialPrincipal() { return materialPrincipal; }
    public void setMaterialPrincipal(String materialPrincipal) { this.materialPrincipal = materialPrincipal; }
    
    public String getTecnicaElaboracion() { return tecnicaElaboracion; }
    public void setTecnicaElaboracion(String tecnicaElaboracion) { this.tecnicaElaboracion = tecnicaElaboracion; }
    
    public String getAcabado() { return acabado; }
    public void setAcabado(String acabado) { this.acabado = acabado; }
    
    public String getAltura() { return altura; }
    public void setAltura(String altura) { this.altura = altura; }
    
    public String getDiametroBoca() { return diametroBoca; }
    public void setDiametroBoca(String diametroBoca) { this.diametroBoca = diametroBoca; }
    
    public String getDiametroSuperior() { return diametroSuperior; }
    public void setDiametroSuperior(String diametroSuperior) { this.diametroSuperior = diametroSuperior; }
    
    public String getCapacidad() { return capacidad; }
    public void setCapacidad(String capacidad) { this.capacidad = capacidad; }
    
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    
    public String getHistoria() { return historia; }
    public void setHistoria(String historia) { this.historia = historia; }
    
    public String getTradicionCultural() { return tradicionCultural; }
    public void setTradicionCultural(String tradicionCultural) { this.tradicionCultural = tradicionCultural; }
    
    public String getArtesanoCreador() { return artesanoCreador; }
    public void setArtesanoCreador(String artesanoCreador) { this.artesanoCreador = artesanoCreador; }
    
    public String getRegionProcedencia() { return regionProcedencia; }
    public void setRegionProcedencia(String regionProcedencia) { this.regionProcedencia = regionProcedencia; }
    
    public String getCuidadosGenerales() { return cuidadosGenerales; }
    public void setCuidadosGenerales(String cuidadosGenerales) { this.cuidadosGenerales = cuidadosGenerales; }
    
    public String getLimpieza() { return limpieza; }
    public void setLimpieza(String limpieza) { this.limpieza = limpieza; }
    
    public String getAlmacenamiento() { return almacenamiento; }
    public void setAlmacenamiento(String almacenamiento) { this.almacenamiento = almacenamiento; }
    
    public String getPrecauciones() { return precauciones; }
    public void setPrecauciones(String precauciones) { this.precauciones = precauciones; }
    
    public String getDurabilidad() { return durabilidad; }
    public void setDurabilidad(String durabilidad) { this.durabilidad = durabilidad; }
    
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }
    
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}