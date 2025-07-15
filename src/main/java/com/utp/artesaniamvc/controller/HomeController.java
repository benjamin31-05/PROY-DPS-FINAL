package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.Producto;
import com.utp.artesaniamvc.service.ProductoService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/artesaniamvc")
public class HomeController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("")
    public String home(Model model) {

      // Obtener solo productos disponibles por temporada
        List<Producto> productosDisponibles = productoService.findAllAvailable();
        
        // Obtener 6 productos aleatorios de los disponibles
        List<Producto> productosAleatorios = productosDisponibles.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collected -> {
                            Collections.shuffle(collected);
                            return collected.stream()
                                    .limit(6)
                                    .collect(Collectors.toList());
                        }));
                        
        model.addAttribute("productos", productosAleatorios);
        return "cliente/homeCliente";
    }

}
