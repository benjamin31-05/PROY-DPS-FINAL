package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class VentaService {
    
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    
    public Page<DetallePedido> obtenerVentasPorLocal(
        String local, 
        Date fechaInicio, 
        Date fechaFin, 
        Pageable pageable
    ) {
        if (fechaInicio != null && fechaFin != null) {
            return detallePedidoRepository.findVentasByLocalAndFechas(
                local, fechaInicio, fechaFin, pageable
            );
        } else {
            return detallePedidoRepository.findVentasByLocal(local, pageable);
        }
    }
    
    // Método para calcular totales de ventas
    public Map<String, Object> calcularTotalesVentas(Page<DetallePedido> ventas) {
        double totalVentas = ventas.getContent().stream()
            .mapToDouble(DetallePedido::getTotal)
            .sum();
        
        int totalProductosVendidos = ventas.getContent().stream()
            .mapToInt(DetallePedido::getCantidad)
            .sum();
        
        Map<String, Object> totales = new HashMap<>();
        totales.put("totalVentas", totalVentas);
        totales.put("totalProductos", totalProductosVendidos);
        
        return totales;
    }
    
     // Método para obtener ventas de todos los locales para admin
    public Page<DetallePedido> obtenerTodasVentasAdmin(
        Date fechaInicio, 
        Date fechaFin, 
        Pageable pageable
    ) {
        return detallePedidoRepository.findVentasByFechas(
            fechaInicio, fechaFin, pageable
        );
    }

    // Método para obtener ventas por local específico para admin
    public Page<DetallePedido> obtenerVentasPorLocalAdmin(
        String local, 
        Date fechaInicio, 
        Date fechaFin, 
        Pageable pageable
    ) {
        return detallePedidoRepository.findVentasByLocalAndFechas(
            local, fechaInicio, fechaFin, pageable
        );
    }

    // Método para obtener lista de ventas sin paginación para PDF
   public List<DetallePedido> obtenerListaTodasVentasAdmin(
        Date fechaInicio, 
        Date fechaFin
    ) {
        return detallePedidoRepository.findVentasByFechasList(
            fechaInicio, fechaFin
        );
    }
    
    public List<DetallePedido> obtenerListaVentasPorLocalAdmin(
        String local, 
        Date fechaInicio, 
        Date fechaFin
    ) {
        return detallePedidoRepository.findVentasByLocalAndFechasList(
            local, fechaInicio, fechaFin
        );
    }
    
     public Map<String, Object> obtenerEstadisticasVentasPorLocal(Date fechaInicio, Date fechaFin) {
        // Obtener todos los locales y sus ventas
        List<Object[]> resultados = detallePedidoRepository.obtenerVentasPorLocal(fechaInicio, fechaFin);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Preparar datos para gráficas
        List<String> locales = new ArrayList<>();
        List<Double> ventas = new ArrayList<>();
        List<Integer> cantidadProductos = new ArrayList<>();
        
        for (Object[] resultado : resultados) {
            locales.add((String) resultado[0]);  // Nombre del local
            ventas.add((Double) resultado[1]);   // Total de ventas
            cantidadProductos.add(((Long) resultado[2]).intValue());  // Cantidad de productos vendidos
        }
        
        estadisticas.put("locales", locales);
        estadisticas.put("ventas", ventas);
        estadisticas.put("cantidadProductos", cantidadProductos);
        
        return estadisticas;
    }
}