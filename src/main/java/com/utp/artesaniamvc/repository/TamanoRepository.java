package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TamanoRepository extends JpaRepository<Tamano, Integer> {
    Tamano findByNombre(String nombre);
}
