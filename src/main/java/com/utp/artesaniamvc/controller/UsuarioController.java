package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('CLIENTE')")
    public String mostrarPerfil(Model model, Authentication authentication) {
        // Obtener el username del usuario autenticado
        String username = authentication.getName();

        // Obtener los datos del usuario
        Usuario usuario = usuarioService.findByUsername(username);

        // Añadir el usuario al modelo
        model.addAttribute("usuario", usuario);

        return "cliente/perfil";
    }

    @PostMapping("/perfil/actualizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String actualizarPerfil(@ModelAttribute Usuario usuario,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            // Obtener el usuario actual
            String username = authentication.getName();
            Usuario usuarioActual = usuarioService.findByUsername(username);

            // Actualizar solo los campos permitidos
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setEmail(usuario.getEmail());
            usuarioActual.setDireccion(usuario.getDireccion());
            usuarioActual.setTelefono(usuario.getTelefono());
            usuarioActual.setUsername(usuario.getUsername());

            usuarioService.save(usuarioActual);
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }

        return "redirect:/cliente/perfil";
    }

    @GetMapping("/detalle")
    @PreAuthorize("hasRole('CLIENTE')")
    public String mostrarDetalle(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);
        model.addAttribute("usuario", usuario);
        return "cliente/detalle";
    }

    @PostMapping("/detalle/actualizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public String actualizarDetalle(
            @ModelAttribute Usuario usuario,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Usuario usuarioActual = usuarioService.findByUsername(username);

            // Actualizar datos básicos
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setDireccion(usuario.getDireccion());
            usuarioActual.setTelefono(usuario.getTelefono());
            usuarioActual.setEmail(usuario.getEmail());
            usuarioActual.setUsername(usuario.getUsername());

            // Si se proporcionó una nueva contraseña
            if (newPassword != null && !newPassword.isEmpty()) {
                // Verificar que se proporcionó la contraseña actual
                if (currentPassword == null || currentPassword.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe proporcionar su contraseña actual");
                    return "redirect:/cliente/detalle";
                }

                // Verificar que la contraseña actual es correcta
                if (!usuarioService.verificarContraseñaActual(usuarioActual, currentPassword)) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                    return "redirect:/cliente/detalle";
                }

                // Verificar que la nueva contraseña y su confirmación coinciden
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/cliente/detalle";
                }

                // Validar la complejidad de la nueva contraseña
                if (newPassword.length() < 8) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                    return "redirect:/cliente/detalle";
                }

                // Actualizar la contraseña
                usuarioActual.setPassword(passwordEncoder.encode(newPassword));
            }

            usuarioService.actualizarUsuario(usuarioActual);
            redirectAttributes.addFlashAttribute("mensaje", "Datos actualizados correctamente");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar los datos: " + e.getMessage());
        }
        return "redirect:/cliente/detalle";
    }

}

