package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private AsuntoRepository asuntoRepository;

    // Método para guardar un nuevo contacto con el asunto especificado
    @Transactional
    public Contacto saveContacto(Contacto contacto, Integer asuntoId) {
        Asunto asunto = asuntoRepository.findById(asuntoId)
                .orElseThrow(() -> new EntityNotFoundException("Asunto no encontrado id: " + asuntoId));
        contacto.setAsunto(asunto);
        return contactoRepository.save(contacto);
    }

    // Método para obtener una lista de contactos con paginación
    public Page<Contacto> getAllContactos(Pageable pageable) {
        return contactoRepository.findAllByOrderByIdDesc(pageable);
    }

    // Método para obtener los contactos de un asunto específico
    public List<Contacto> getContactosByAsunto(Asunto asunto) {
        return contactoRepository.findAllByAsuntoOrderByIdDesc(asunto, Pageable.unpaged()).getContent();
    }

    public void deleteContacto(Integer id) {
        contactoRepository.deleteById(id);
    }
    
     public Contacto getContactoById(Integer id) {
        return contactoRepository.findById(id).orElse(null); // Devolver null si no se encuentra
    }

    public void updateContacto(Contacto contacto) {
        contactoRepository.save(contacto); // Guarda los cambios en la base de datos
    }

    public long countAllContactos() {
        return contactoRepository.count();
    }
}

