package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LocalService {

    @Autowired
    private LocalRepository localRepository;

    @Transactional
    public Local crearLocal(Local local) {
        if (localRepository.findByNombre(local.getNombre()) != null) {
            throw new IllegalArgumentException("Ya existe un local con este nombre");
        }
        return localRepository.save(local);
    }

    public Optional<Local> getById(Integer id) {
        return localRepository.findById(id);
    }

    public List<Local> findAll() {
        return localRepository.findAll();
    }

    public Local findByNombre(String nombre) {
        return localRepository.findAll().stream()
                .filter(local -> local.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Local " + nombre + " no encontrado"));
    }
    
      // Método para obtener todos los locales
    public List<Local> obtenerTodosLocales() {
        return localRepository.findAll();
    }

    // Si quieres añadir más lógica, como ordenamiento o filtrado
    public List<Local> obtenerTodosLocales(Sort sort) {
        return localRepository.findAll(sort);
    }

}
