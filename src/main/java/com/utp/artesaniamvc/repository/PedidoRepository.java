package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    
    // Método para listar todos los contactos ordenados por fecha de creación si añades un campo de fecha
    Page<Pedido> findAllByOrderByIdDesc(Pageable pageable);

    Optional<Pedido> findByNumero(String numero);

    List<Pedido> findTop10ByOrderByFechaCreacionDesc();

    @Query("SELECT p FROM Pedido p JOIN p.detalles d WHERE d.local.nombre = :nombreLocal ORDER BY p.fechaCreacion DESC")
    List<Pedido> findTop10ByLocalNombreOrderByFechaCreacionDesc(@Param("nombreLocal") String nombreLocal, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Pedido p "
            + "LEFT JOIN FETCH p.detalles d "
            + "LEFT JOIN FETCH d.local "
            + "LEFT JOIN FETCH d.tamano "
            + "WHERE p.id = :id")
    Pedido findPedidoWithDetailsById(@Param("id") Integer id);
    
    
    // Método para obtener los pedidos con estado ABIERTO
    List<Pedido> findByEstado(EstadoPedido estado);
    
     // Método para encontrar pedidos por local usando DetallePedido
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "JOIN p.detalles dp " +
           "WHERE dp.local = :local")
    List<Pedido> findPedidosByLocal(@Param("local") Local local);

    // Método para obtener pedidos recientes de un local específico
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "JOIN p.detalles dp " +
           "WHERE dp.local = :local " +
           "ORDER BY p.fechaCreacion DESC")
    List<Pedido> findRecentPedidosByLocal(@Param("local") Local local, Pageable pageable);
    
      
    // Opcionalmente, puedes agregar un método más específico
    List<Pedido> findPedidosByLocalAndEstado(Local local, EstadoPedido estado);
    
     // Método para encontrar pedidos por usuario y estado
    Pedido findByUsuarioAndEstado(Usuario usuario, EstadoPedido estado);
    
    // Si quieres más flexibilidad, puedes usar un método que devuelva una lista
    List<Pedido> findByUsuarioAndEstadoIn(Usuario usuario, List<EstadoPedido> estados);

}
