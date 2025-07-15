package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
   Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    
    // Nuevos métodos usando convención de nombres
    List<Usuario> findByRoles_Nombre(String rolNombre);
    Page<Usuario> findByRoles_Nombre(String rolNombre, Pageable pageable);
    
    // Alternativamente, usando @Query
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = :rolNombre")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);
    
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = :rolNombre")
    Page<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre, Pageable pageable);

}
