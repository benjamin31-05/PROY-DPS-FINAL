package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.service.*;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos1")
@PreAuthorize("hasRole('ADMIN')") 
//ProductoController es para el Administrador
public class ProductoController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ContactoService contactoService;

    @Autowired
    private SubcategoriaService subcategoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private EmailService emailService;

    //vista para el administrador de agregar nuevos productos
    @GetMapping("/products")
    public String show(Model model, @RequestParam(defaultValue = "0") int page) {

        Page<Producto> productosPage = productoService.getAllProductos(PageRequest.of(page, 6));
        model.addAttribute("productos", productosPage);
        return "productos/show";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("subcategorias", subcategoriaService.getAllSubcategorias());
        return "productos/create2";
    }

    @PostMapping("/register")
    public String saveProducto(@ModelAttribute Producto producto,
            @RequestParam("file") MultipartFile file,
            @RequestParam("subcategoria_id") Integer subcategoriaID, Model model) {

        // Procesar y guardar imagen
        if (!file.isEmpty()) {
            try {
                // Definir directorio de imágenes
                String uploadsDir = "./uploads/";
                Path uploadPath = Paths.get(uploadsDir);

                // Crear directorio si no existe
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Guardar imagen
                String fileName = file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);

                // Guardar ruta en el producto
                producto.setImageUrl("/uploads/" + fileName);

            } catch (IOException e) {
                LOGGER.error("Error al procesar imagen", e);
            }
        }

        productoService.saveProducto(producto, subcategoriaID);

        return "redirect:/productos1/products";
        
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Producto product = productoService.getProductoById(id);
        model.addAttribute("producto", product);
        model.addAttribute("subcategorias", subcategoriaService.getAllSubcategorias());
        return "administrador/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProducto(@PathVariable Integer id,
            @ModelAttribute Producto producto,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            // Validaciones específicas para el stock antes de llamar al service
            if (producto.getStockActual() != null
                    && producto.getStockOpenPlaza() != null
                    && producto.getStockUDEP() != null) {

                // Validar que los stocks no sean negativos
                if (producto.getStockActual() < 0
                        || producto.getStockOpenPlaza() < 0
                        || producto.getStockUDEP() < 0) {
                    throw new IllegalArgumentException("Los valores de stock no pueden ser negativos");
                }

                // Validar que la suma de stocks físico y virtual sea igual al total
                if (producto.getStockOpenPlaza() + producto.getStockUDEP()
                        != producto.getStockActual()) {
                    throw new IllegalArgumentException(
                            "El stock total debe ser igual a la suma del stock físico y virtual"
                    );
                }
            }

            // Intentar actualizar el producto
            Producto updatedProduct = productoService.updateProducto(id, producto);

            redirectAttributes.addFlashAttribute("success",
                    "Producto actualizado exitosamente. Stock actual: "
                    + updatedProduct.getStockActual() + " unidades"
            );

            return "redirect:/productos1/products";

        } catch (IllegalArgumentException e) {
            // Errores de validación (incluye validaciones de stock)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            model.addAttribute("subcategorias", subcategoriaService.getAllSubcategorias());
            return "administrador/edit";

        } catch (EntityNotFoundException e) {
            // Producto no encontrado
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos1/products";

        } catch (Exception e) {
            // Otros errores inesperados
            model.addAttribute("error", "Error inesperado al actualizar el producto");
            model.addAttribute("producto", producto);
            model.addAttribute("subcategorias", subcategoriaService.getAllSubcategorias());
            return "administrador/edit";
        }

    }

    @GetMapping("/delete/{id}")
    public String deleteProductos(@PathVariable Integer id) {
        productoService.deleteProducto(id);
        return "redirect:/productos1/products";
    }

    @GetMapping("/contacts")
    public String showContacts(Model model, @RequestParam(defaultValue = "0") int page) {

        Page<Contacto> contactosPage = contactoService.getAllContactos(PageRequest.of(page, 6));
        model.addAttribute("contactos", contactosPage);
        return "administrador/verAsuntos";

    }

    @GetMapping("/deleteAsunto/{id}")
    public String deleteAsuntos(@PathVariable Integer id) {
        contactoService.deleteContacto(id);
        return "redirect:/productos1/contacts";
    }

    @GetMapping("/responderAsunto/{id}")
    public String responderAsunto(@PathVariable Integer id, Model model) {
        Contacto contacto = contactoService.getContactoById(id);
        if (contacto == null) {
            return "redirect:/productos1/contacts";  // Redirige si el contacto no existe
        }
        model.addAttribute("contacto", contacto);
        return "administrador/responderAsunto"; // Vista para responder
    }

    @PostMapping("/responderAsunto/{id}")
    public String enviarRespuesta(@PathVariable Integer id, @RequestParam("respuesta") String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía.");
        }

        // Buscar el contacto por ID
        Contacto contacto = contactoService.getContactoById(id);
        if (contacto == null) {
            return "redirect:/productos1/contacts";  // Redirigir si no se encuentra el contacto
        }

        // Guardar la respuesta
        contacto.setRespuesta(respuesta);
        contactoService.updateContacto(contacto);

        // Enviar correo de respuesta
        emailService.enviarCorreoRespuesta(contacto.getEmail(), contacto.getNombre(),
                contacto.getAsunto().getDescription(), respuesta);

        return "redirect:/productos1/contacts"; // Redirigir a la lista de contactos
    }

}

