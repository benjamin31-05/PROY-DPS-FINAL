package com.utp.artesaniamvc.model;

public enum MetodoPago {
    EFECTIVO("Pago en Efectivo"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    YAPE("Yape"),
    PLIN("Plin");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}