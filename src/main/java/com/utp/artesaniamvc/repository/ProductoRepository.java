package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository 
        extends JpaRepository<Producto, Integer> {
    
    List<Producto> findTop6ByOrderByCreatedAtDesc();

    Page<Producto> findAllByOrderByCreatedAtDesc(Pageable p);
    
    List<Producto> findBySubcategoriaId(Integer subcategoriaId);
    
    Producto findByNombreProducto(String nombreProducto);
    
   // Nueva consulta para búsqueda parcial
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    
    @Query("SELECT p FROM Producto p WHERE p.stockOpenPlaza > 0 AND p.disponiblePorTemporada = true")
    List<Producto> findAvailableProductsOpenPlaza();
    
    @Query("SELECT p FROM Producto p WHERE p.stockUDEP > 0 AND p.disponiblePorTemporada = true")
    List<Producto> findAvailableProductsUDEP();
    
    @Query("SELECT p FROM Producto p WHERE p.stockOpenPlaza <= :minStock OR p.stockUDEP <= :minStock")
    List<Producto> findLowStockProducts(@Param("minStock") Double minStock);
       
}
