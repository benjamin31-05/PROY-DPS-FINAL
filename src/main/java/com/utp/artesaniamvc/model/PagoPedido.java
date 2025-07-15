package com.utp.artesaniamvc.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pagos_pedidos")
public class PagoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @OneToOne
    @JoinColumn(name = "detalle_pedido_id", unique = true)
    private DetallePedido detallePedido;
    
    private double montoPagado;
    private double vuelto;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaPago;
    
      @Column(name = "imagen_comprobante")
    private String imagenComprobante;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioPago; // Quien realizó el pago

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DetallePedido getDetallePedido() {
        return detallePedido;
    }

    public void setDetallePedido(DetallePedido detallePedido) {
        this.detallePedido = detallePedido;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public double getVuelto() {
        return vuelto;
    }

    public void setVuelto(double vuelto) {
        this.vuelto = vuelto;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Usuario getUsuarioPago() {
        return usuarioPago;
    }

    public void setUsuarioPago(Usuario usuarioPago) {
        this.usuarioPago = usuarioPago;
    }

    public String getImagenComprobante() {
        return imagenComprobante;
    }

    public void setImagenComprobante(String imagenComprobante) {
        this.imagenComprobante = imagenComprobante;
    }
    
    
}
