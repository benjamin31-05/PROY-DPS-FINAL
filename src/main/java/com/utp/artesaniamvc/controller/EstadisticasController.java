package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.service.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/estadisticas")
public class EstadisticasController {
    @Autowired
    private VentaService ventaService;
    @Autowired
    private LocalService localService;

    @GetMapping
    public String mostrarEstadisticas(
        Model model,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin
    ) {
        // Configurar fechas por defecto si no se proporcionan
        if (fechaInicio == null || fechaFin == null) {
            Calendar cal = Calendar.getInstance();
            fechaFin = cal.getTime();
            cal.add(Calendar.MONTH, -1);
            fechaInicio = cal.getTime();
        }

        // Ajustar horas de las fechas
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(fechaInicio);
        calInicio.set(Calendar.HOUR_OF_DAY, 0);
        calInicio.set(Calendar.MINUTE, 0);
        calInicio.set(Calendar.SECOND, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = Calendar.getInstance();
        calFin.setTime(fechaFin);
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        // Obtener estadísticas de ventas
        Map<String, Object> estadisticas = ventaService.obtenerEstadisticasVentasPorLocal(
            calInicio.getTime(), 
            calFin.getTime()
        );

        model.addAttribute("locales", estadisticas.get("locales"));
        model.addAttribute("ventas", estadisticas.get("ventas"));
        model.addAttribute("cantidadProductos", estadisticas.get("cantidadProductos"));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "administrador/estadisticas";
    }
}