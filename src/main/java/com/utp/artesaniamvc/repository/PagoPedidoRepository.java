package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoPedidoRepository extends JpaRepository<PagoPedido, Integer> {
    Optional<PagoPedido> findByDetallePedido_Id(Integer detallePedidoId);

    public Object findByDetallePedidoId(Integer detallePedidoId);
    
     Optional<PagoPedido> findByDetallePedido(DetallePedido detallePedido);
}
