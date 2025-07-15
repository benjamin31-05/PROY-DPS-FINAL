package com.utp.artesaniamvc.repository;

import com.utp.artesaniamvc.model.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrecioTamanoRepository extends JpaRepository<PrecioTamano, Integer> {
    List<PrecioTamano> findByProductoId(Integer productoId);
    PrecioTamano findByProductoIdAndTamanoId(Integer productoId, Integer tamanoId);
}