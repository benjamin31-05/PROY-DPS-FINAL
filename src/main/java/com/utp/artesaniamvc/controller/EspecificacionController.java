package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.service.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos1/especificaciones")
@PreAuthorize("hasRole('ADMIN')")
public class EspecificacionController {
    
    private final Logger LOGGER = LoggerFactory.getLogger(EspecificacionController.class);
    
    @Autowired
    private EspecificacionProductoService especificacionService;
    
    @Autowired
    private ProductoService productoService;
    
    // Mostrar formulario para crear especificaciones
    @GetMapping("/create/{productoId}")
    public String mostrarFormularioCrear(@PathVariable Integer productoId, Model model) {
        try {
            Producto producto = productoService.getProductoById(productoId);
            
            // Verificar si ya tiene especificaciones
            if (especificacionService.productoTieneEspecificaciones(productoId)) {
                model.addAttribute("error", "Este producto ya tiene especificaciones definidas. Use la opción de editar.");
                return "redirect:/productos1/especificaciones/edit/" + productoId;
            }
            
            model.addAttribute("producto", producto);
            model.addAttribute("especificacion", new EspecificacionProducto());
            model.addAttribute("accion", "crear");
            
            return "productos/especificaciones";
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/productos1/products";
        }
    }
    
    // Procesar creación de especificaciones
    @PostMapping("/create/{productoId}")
    public String crearEspecificacion(@PathVariable Integer productoId, 
                                    @ModelAttribute EspecificacionProducto especificacion,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            especificacionService.crearEspecificacion(productoId, especificacion);
            redirectAttributes.addFlashAttribute("success", "Especificaciones creadas exitosamente");
            return "redirect:/productos1/products";
            
        } catch (IllegalStateException e) {
            model.addAttribute("error", "Este producto ya tiene especificaciones definidas");
            return "redirect:/productos1/especificaciones/edit/" + productoId;
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/productos1/products";
            
        } catch (Exception e) {
            LOGGER.error("Error al crear especificaciones", e);
            model.addAttribute("error", "Error al crear las especificaciones: " + e.getMessage());
            model.addAttribute("producto", productoService.getProductoById(productoId));
            model.addAttribute("especificacion", especificacion);
            model.addAttribute("accion", "crear");
            return "productos/especificaciones";
        }
    }
    
    // Mostrar formulario para editar especificaciones
    @GetMapping("/edit/{productoId}")
    public String mostrarFormularioEditar(@PathVariable Integer productoId, Model model) {
        try {
            Producto producto = productoService.getProductoById(productoId);
            EspecificacionProducto especificacion = especificacionService.obtenerEspecificacionPorProducto(productoId);
            
            if (especificacion == null) {
                model.addAttribute("info", "Este producto no tiene especificaciones. Se creará una nueva.");
                return "redirect:/productos1/especificaciones/create/" + productoId;
            }
            
            model.addAttribute("producto", producto);
            model.addAttribute("especificacion", especificacion);
            model.addAttribute("accion", "editar");
            
            return "productos/especificaciones";
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/productos1/products";
        }
    }
    
    // Procesar edición de especificaciones
    @PostMapping("/edit/{productoId}")
    public String editarEspecificacion(@PathVariable Integer productoId,
                                     @ModelAttribute EspecificacionProducto especificacion,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        try {
            especificacionService.actualizarEspecificacion(productoId, especificacion);
            redirectAttributes.addFlashAttribute("success", "Especificaciones actualizadas exitosamente");
            return "redirect:/productos1/products";
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "No se encontraron especificaciones para actualizar");
            return "redirect:/productos1/especificaciones/create/" + productoId;
            
        } catch (Exception e) {
            LOGGER.error("Error al actualizar especificaciones", e);
            model.addAttribute("error", "Error al actualizar las especificaciones: " + e.getMessage());
            model.addAttribute("producto", productoService.getProductoById(productoId));
            model.addAttribute("especificacion", especificacion);
            model.addAttribute("accion", "editar");
            return "productos/especificaciones";
        }
    }
    
    // Ver especificaciones (solo lectura)
    @GetMapping("/view/{productoId}")
    public String verEspecificaciones(@PathVariable Integer productoId, Model model) {
        try {
            Producto producto = productoService.getProductoById(productoId);
            EspecificacionProducto especificacion = especificacionService.obtenerEspecificacionPorProducto(productoId);
            
            if (especificacion == null) {
                model.addAttribute("info", "Este producto no tiene especificaciones definidas");
                return "redirect:/productos1/especificaciones/create/" + productoId;
            }
            
            model.addAttribute("producto", producto);
            model.addAttribute("especificacion", especificacion);
            
            return "productos/verEspecificaciones";
            
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/productos1/products";
        }
    }
    
    // Eliminar especificaciones
    @GetMapping("/delete/{productoId}")
    public String eliminarEspecificacion(@PathVariable Integer productoId, 
                                       RedirectAttributes redirectAttributes) {
        try {
            especificacionService.eliminarEspecificacion(productoId);
            redirectAttributes.addFlashAttribute("success", "Especificaciones eliminadas exitosamente");
            
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "No se encontraron especificaciones para eliminar");
            
        } catch (Exception e) {
            LOGGER.error("Error al eliminar especificaciones", e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar las especificaciones");
        }
        
        return "redirect:/productos1/products";
    }
}