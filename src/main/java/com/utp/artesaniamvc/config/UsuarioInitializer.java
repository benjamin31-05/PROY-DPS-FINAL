package com.utp.artesaniamvc.config;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioInitializer {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @PostConstruct
    public void init() {

        // Crear roles si no existen
        Rol rolAdmin = rolRepository.findByNombre("ADMIN");
        if (rolAdmin == null) {
            rolAdmin = new Rol("ADMIN");
            rolRepository.save(rolAdmin);
        }

        Rol rolTienda1 = rolRepository.findByNombre("TIENDA1");
        if (rolTienda1 == null) {
            rolTienda1 = new Rol("TIENDA1");
            rolRepository.save(rolTienda1);
        }

        Rol rolTienda2 = rolRepository.findByNombre("TIENDA2");
        if (rolTienda2 == null) {
            rolTienda2 = new Rol("TIENDA2");
            rolRepository.save(rolTienda2);
        }

        // Añadir rol CLIENTE
        Rol rolCliente = rolRepository.findByNombre("CLIENTE");
        if (rolCliente == null) {
            rolCliente = new Rol("CLIENTE");
            rolRepository.save(rolCliente);
        }

        // Crear usuario administrador si no existe
        if (!usuarioRepository.existsByUsername("admin11")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin11");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("administrador@gmail.com");
            admin.setNombre("Administrador Luis");
            admin.setEnabled(true);
            admin.setRoles(new HashSet<>());
            admin.getRoles().add(rolAdmin);

            usuarioRepository.save(admin);
        }
        
        // Crear usuario TIENDA 1 si no existe
        if (!usuarioRepository.existsByUsername("tienda1")) {
            Usuario tienda1 = new Usuario();
            tienda1.setUsername("tienda1");
            tienda1.setPassword(passwordEncoder.encode("tienda111"));
            tienda1.setEmail("tienda1@gmail.com");
            tienda1.setNombre("Tienda 1");
            tienda1.setEnabled(true);
            tienda1.setRoles(new HashSet<>());
            tienda1.getRoles().add(rolTienda1);

            usuarioRepository.save(tienda1);
        }
        
       // Crear usuario TIENDA 2 si no existe
        if (!usuarioRepository.existsByUsername("tienda2")) {
            Usuario tienda2 = new Usuario();
            tienda2.setUsername("tienda2");
            tienda2.setPassword(passwordEncoder.encode("tienda222"));
            tienda2.setEmail("tienda2@gmail.com");
            tienda2.setNombre("Tienda 2");
            tienda2.setEnabled(true);
            tienda2.setRoles(new HashSet<>());
            tienda2.getRoles().add(rolTienda2);

            usuarioRepository.save(tienda2);
        }

    }

}
