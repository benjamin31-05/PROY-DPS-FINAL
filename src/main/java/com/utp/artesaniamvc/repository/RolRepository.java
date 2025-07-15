package com.utp.artesaniamvc.repository;
import com.utp.artesaniamvc.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Rol findByNombre(String nombre);
}
