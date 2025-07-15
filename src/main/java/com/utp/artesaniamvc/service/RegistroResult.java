package com.utp.artesaniamvc.service;

public class RegistroResult {
    private boolean exito;
    private String mensaje;
    
    public RegistroResult(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }
    
    // Getters y setters
    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}