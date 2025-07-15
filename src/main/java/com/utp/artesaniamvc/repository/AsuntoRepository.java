package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.Asunto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsuntoRepository extends 
        JpaRepository<Asunto, Integer> {
    
}