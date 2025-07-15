package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TamanoService {

    @Autowired
    private TamanoRepository tamanoRepository;

    @Transactional
    public Tamano crearTamano(Tamano tamano) {
        if (tamanoRepository.findByNombre(tamano.getNombre()) != null) {
            throw new IllegalArgumentException("Ya existe un tamaño con este nombre");
        }
        return tamanoRepository.save(tamano);
    }

    public Optional<Tamano> getById(Integer id) {
        return tamanoRepository.findById(id);
    }

    public List<Tamano> findAll() {
        return tamanoRepository.findAll();
    }
}
