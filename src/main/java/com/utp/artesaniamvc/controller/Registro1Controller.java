package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Registro1Controller {
    
     @Autowired
    private Usuario1Service usuarioService;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private SecurityLoggingService securityLoggingService;
    
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "administrador/login";
    }
    
    @PostMapping("/registrar")
    public String iniciarRegistro(@RequestParam String nombre,
                                @RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes,
                                HttpSession session,
                                HttpServletRequest request) {
        
        String ipAddress = getClientIpAddress(request);
        
        // Verificar rate limiting
        if (!rateLimitingService.isAllowed(ipAddress)) {
            securityLoggingService.logSuspiciousActivity(ipAddress, "Rate limit exceeded");
            redirectAttributes.addFlashAttribute("error", 
                "Demasiados intentos. Intente nuevamente en una hora.");
            return "redirect:/registro";
        }
        
        // Registrar el intento
        rateLimitingService.recordAttempt(ipAddress);
        
        try {
            // Sanitizar entradas
            nombre = sanitizeInput(nombre);
            email = sanitizeInput(email);
            
            // Procesar registro de forma segura
            RegistroResult resultado = usuarioService.procesarRegistroSeguro(nombre, email, password, ipAddress);
            
            if (resultado.isExito()) {
                // Generar y enviar código de verificación solo si es válido
                String codigoVerificacion = verificationCodeService.generarCodigoVerificacion();
                
                // Almacenar datos en sesión
                session.setAttribute("nombre", nombre);
                session.setAttribute("email", email);
                session.setAttribute("password", password);
                session.setAttribute("codigoVerificacion", codigoVerificacion);
                
                // Enviar código
                verificationCodeService.enviarCodigoVerificacion(email, codigoVerificacion);
            }
            
            // SIEMPRE mostrar el mismo mensaje (no revelar si el email existe)
            redirectAttributes.addFlashAttribute("info", resultado.getMensaje());
            return "redirect:/verificar-codigo";
            
        } catch (Exception e) {
            securityLoggingService.logSuspiciousActivity(ipAddress, "Registration error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Se ha enviado un código de verificación a su email");
            return "redirect:/verificar-codigo";
        }
    }
    
    @GetMapping("/verificar-codigo")
    public String mostrarVerificacion() {
        return "cliente/verificar-codigo";
    }
    
    @PostMapping("/verificar")
    public String verificarCodigo(@RequestParam String codigo,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        String codigoGuardado = (String) session.getAttribute("codigoVerificacion");
        
        if (codigo != null && codigo.equals(codigoGuardado)) {
            try {
                // Crear usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setNombre((String) session.getAttribute("nombre"));
                nuevoUsuario.setEmail((String) session.getAttribute("email"));
                nuevoUsuario.setUsername((String) session.getAttribute("email"));
                nuevoUsuario.setPassword((String) session.getAttribute("password"));
                nuevoUsuario.setEnabled(true);
                nuevoUsuario.setVerificado(true);
                
                // Asignar rol CLIENTE
                Rol rolCliente = rolRepository.findByNombre("CLIENTE");
                if (rolCliente == null) {
                    rolCliente = new Rol();
                    rolCliente.setNombre("CLIENTE");
                    rolRepository.save(rolCliente);
                }
                
                Set<Rol> roles = new HashSet<>();
                roles.add(rolCliente);
                nuevoUsuario.setRoles(roles);
                
                // Guardar usuario
                usuarioService.save(nuevoUsuario);
                
                // Limpiar sesión
                limpiarSesion(session);
                
                redirectAttributes.addFlashAttribute("registroExitoso",
                        "Registro exitoso. Por favor, inicia sesión.");
                return "redirect:/login";
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", 
                    "Error al completar el registro. Intente nuevamente.");
                return "redirect:/registro";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Código de verificación incorrecto");
            return "redirect:/verificar-codigo";
        }
    }
    
    // Métodos auxiliares
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.replaceAll("[<>\"'&]", "").trim();
    }
    
    private void limpiarSesion(HttpSession session) {
        session.removeAttribute("nombre");
        session.removeAttribute("email");
        session.removeAttribute("password");
        session.removeAttribute("codigoVerificacion");
    }
    
    
}
