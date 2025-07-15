package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    
      @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LocalRepository localRepository; // Repositorio para la entidad Local
    
     // Método para obtener todos los usuarios para el filtro
    public List<Usuario> getAllClientes() {
        return usuarioService.findAllClientUsers(); // Asume que tienes un método para obtener todos los usuarios
    }
    
     // Método para filtrar por usuario
    public Page<DetallePedido> getDetallesPedidosByUsuario(String username, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("pedido.fechaCreacion").descending()
        );
        return detallePedidoRepository.findByPedido_Usuario_Username(username, sortedPageable);
    }
    
     // Método para filtrar por local y usuario
    public Page<DetallePedido> getDetallesPedidosByLocalAndUsuario(String nombreLocal, String username, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("pedido.fechaCreacion").descending()
        );
        return detallePedidoRepository.findByLocal_NombreAndPedido_Usuario_Username(nombreLocal, username, sortedPageable);
    }


    public DetallePedido save(DetallePedido detallePedido) {
        return detallePedidoRepository.save(detallePedido);
    }

    public Page<DetallePedido> getAllDetallesPedidos(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("pedido.fechaCreacion").descending()
        );
        return detallePedidoRepository.findAll(sortedPageable);
    }

    public DetallePedido obtenerDetallePedidoPorId(Integer id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de Pedido no encontrado"));
    }

    // Método para obtener detalles de pedido por local con paginación
    public Page<DetallePedido> getDetallesPedidosByLocal(String nombreLocal, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("pedido.fechaCreacion").descending()
        );
        return detallePedidoRepository.findByLocal_Nombre(nombreLocal, sortedPageable);
    }

    // Método que añadirás
    public DetallePedido findById(Integer id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de Pedido con ID " + id + " no encontrado"));
    }

}
