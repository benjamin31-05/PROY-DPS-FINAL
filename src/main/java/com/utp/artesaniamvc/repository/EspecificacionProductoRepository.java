package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.EspecificacionProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecificacionProductoRepository extends JpaRepository<EspecificacionProducto, Integer> {
    
    // Buscar especificación por producto
    Optional<EspecificacionProducto> findByProductoId(Integer productoId);
    
    // Verificar si un producto tiene especificaciones
    boolean existsByProductoId(Integer productoId);
    
    // Eliminar especificación por producto
    void deleteByProductoId(Integer productoId);
    
    // Buscar productos con especificaciones completas (que tengan historia y origen)
    @Query("SELECT e FROM EspecificacionProducto e WHERE e.historia IS NOT NULL AND e.origen IS NOT NULL")
    List<EspecificacionProducto> findProductosConHistoriaCompleta();
    
    // Buscar por región de procedencia
    List<EspecificacionProducto> findByRegionProcedencia(String region);
    
    // Buscar por material principal
    List<EspecificacionProducto> findByMaterialPrincipal(String material);
}
