package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    // Método para buscar detalles de pedido por nombre de local con paginación
    Page<DetallePedido> findByLocal_Nombre(String nombreLocal, Pageable pageable);
    
    // Método para filtrar por usuario (cliente)
    Page<DetallePedido> findByPedido_Usuario_Username(String username, Pageable pageable);
    
     // Método para filtrar por local y usuario
    Page<DetallePedido> findByLocal_NombreAndPedido_Usuario_Username(String nombreLocal, String username, Pageable pageable);

    List<DetallePedido> findByLocal(Local local);

    // Método para filtrar ventas por local y rango de fechas
    @Query("SELECT dp FROM DetallePedido dp "
            + "WHERE dp.local.nombre = :local "
            + "AND dp.pedido.fechaCreacion BETWEEN :fechaInicio AND :fechaFin "
            + "AND dp.estadoPago = 'PAGADO'")
    Page<DetallePedido> findVentasByLocalAndFechas(
            @Param("local") String local,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            Pageable pageable
    );

    // Método para obtener ventas por local
    @Query("SELECT dp FROM DetallePedido dp "
            + "WHERE dp.local.nombre = :local "
            + "AND dp.estadoPago = 'PAGADO'")
    Page<DetallePedido> findVentasByLocal(
            @Param("local") String local,
            Pageable pageable
    );

    @Query("SELECT dp FROM DetallePedido dp JOIN dp.pedido p "
            + "WHERE p.fechaCreacion BETWEEN :fechaInicio AND :fechaFin "
            + "ORDER BY p.fechaCreacion DESC")
    Page<DetallePedido> findVentasByFechas(
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            Pageable pageable
    );

    // Métodos sin paginación para generación de PDF
    @Query("SELECT dp FROM DetallePedido dp WHERE dp.pedido.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<DetallePedido> findVentasByFechasList(
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin
    );

    @Query("SELECT dp FROM DetallePedido dp WHERE dp.local.nombre = :local AND dp.pedido.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<DetallePedido> findVentasByLocalAndFechasList(
            @Param("local") String local,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin
    );
    
     @Query("SELECT dp.local.nombre, " +
           "SUM(dp.total) as totalVentas, " +
           "SUM(dp.cantidad) as cantidadProductos " +
           "FROM DetallePedido dp " +
           "WHERE dp.pedido.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
           "AND dp.estadoPago = 'PAGADO' " +
           "GROUP BY dp.local.nombre")
    List<Object[]> obtenerVentasPorLocal(
        @Param("fechaInicio") Date fechaInicio, 
        @Param("fechaFin") Date fechaFin
    );
    
   @Query("SELECT COALESCE(SUM(dp.total), 0.0) FROM DetallePedido dp JOIN dp.pedido p WHERE p.local.nombre = :local AND p.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
double calculateTotalVentasByLocalAndFechas(
    @Param("local") String local, 
    @Param("fechaInicio") Date fechaInicio, 
    @Param("fechaFin") Date fechaFin
);

}
