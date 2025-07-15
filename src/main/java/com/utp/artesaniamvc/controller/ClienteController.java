package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.security.*;
import com.utp.artesaniamvc.service.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {
    
     @Autowired
    private ProductoService productoService;
      @Autowired
    private UsuarioService usuarioService;
     
    @GetMapping("/home")
    public String homeCliente(Authentication authentication, Model model) {
        
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
        // Verificar que el usuario tiene rol CLIENTE
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            return "redirect:/login?error";
        }
        
        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        Usuario usuarioActual = userDetails.getUsuario();
        model.addAttribute("usuario", usuarioActual);
        return "cliente/homeCliente";

    }
    
}
