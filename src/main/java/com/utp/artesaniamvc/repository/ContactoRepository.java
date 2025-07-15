package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContactoRepository extends JpaRepository<Contacto, Integer> {

    // Método para encontrar contactos por asunto ordenados de forma descendente por id
    Page<Contacto> findAllByAsuntoOrderByIdDesc(Asunto asunto, Pageable pageable);

    // Método para listar todos los contactos ordenados por fecha de creación si añades un campo de fecha
    Page<Contacto> findAllByOrderByIdDesc(Pageable pageable);
}
