package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EspecificacionProductoService {
    
    @Autowired
    private EspecificacionProductoRepository especificacionRepository;
    
    @Autowired
    private ProductoRepository productoRepository;
    
    // Crear especificación para un producto
    public EspecificacionProducto crearEspecificacion(Integer productoId, EspecificacionProducto especificacion) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));
        
        // Verificar si ya existe una especificación para este producto
        if (especificacionRepository.existsByProductoId(productoId)) {
            throw new IllegalStateException("El producto ya tiene especificaciones definidas");
        }
        
        especificacion.setProducto(producto);
        especificacion.setCreatedAt(LocalDateTime.now());
        especificacion.setUpdatedAt(LocalDateTime.now());
        
        return especificacionRepository.save(especificacion);
    }
    
    // Actualizar especificación existente
    public EspecificacionProducto actualizarEspecificacion(Integer productoId, EspecificacionProducto especificacionActualizada) {
        EspecificacionProducto especificacionExistente = especificacionRepository.findByProductoId(productoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontraron especificaciones para el producto con id: " + productoId));
        
        // Actualizar campos de especificaciones técnicas
        especificacionExistente.setMaterialPrincipal(especificacionActualizada.getMaterialPrincipal());
        especificacionExistente.setTecnicaElaboracion(especificacionActualizada.getTecnicaElaboracion());
        especificacionExistente.setAcabado(especificacionActualizada.getAcabado());
        especificacionExistente.setAltura(especificacionActualizada.getAltura());
        especificacionExistente.setDiametroBoca(especificacionActualizada.getDiametroBoca());
        especificacionExistente.setDiametroSuperior(especificacionActualizada.getDiametroSuperior());
        especificacionExistente.setCapacidad(especificacionActualizada.getCapacidad());
        especificacionExistente.setPeso(especificacionActualizada.getPeso());
        especificacionExistente.setColor(especificacionActualizada.getColor());
        
        // Actualizar campos de historia y origen
        especificacionExistente.setOrigen(especificacionActualizada.getOrigen());
        especificacionExistente.setHistoria(especificacionActualizada.getHistoria());
        especificacionExistente.setTradicionCultural(especificacionActualizada.getTradicionCultural());
        especificacionExistente.setArtesanoCreador(especificacionActualizada.getArtesanoCreador());
        especificacionExistente.setRegionProcedencia(especificacionActualizada.getRegionProcedencia());
        
        // Actualizar campos de cuidados
        especificacionExistente.setCuidadosGenerales(especificacionActualizada.getCuidadosGenerales());
        especificacionExistente.setLimpieza(especificacionActualizada.getLimpieza());
        especificacionExistente.setAlmacenamiento(especificacionActualizada.getAlmacenamiento());
        especificacionExistente.setPrecauciones(especificacionActualizada.getPrecauciones());
        especificacionExistente.setDurabilidad(especificacionActualizada.getDurabilidad());
        
        // Actualizar campos adicionales
        especificacionExistente.setCodigoProducto(especificacionActualizada.getCodigoProducto());
        especificacionExistente.setUpdatedAt(LocalDateTime.now());
        
        return especificacionRepository.save(especificacionExistente);
    }
    
    // Obtener especificación por producto
    public EspecificacionProducto obtenerEspecificacionPorProducto(Integer productoId) {
        return especificacionRepository.findByProductoId(productoId)
                .orElse(null); // Retorna null si no existe, para permitir crear una nueva
    }
    
    // Verificar si un producto tiene especificaciones
    public boolean productoTieneEspecificaciones(Integer productoId) {
        return especificacionRepository.existsByProductoId(productoId);
    }
    
    // Eliminar especificación
    public void eliminarEspecificacion(Integer productoId) {
        if (!especificacionRepository.existsByProductoId(productoId)) {
            throw new EntityNotFoundException("No se encontraron especificaciones para eliminar");
        }
        especificacionRepository.deleteByProductoId(productoId);
    }
    
    // Obtener productos con historia completa
    public List<EspecificacionProducto> obtenerProductosConHistoria() {
        return especificacionRepository.findProductosConHistoriaCompleta();
    }
    
    // Buscar por región
    public List<EspecificacionProducto> buscarPorRegion(String region) {
        return especificacionRepository.findByRegionProcedencia(region);
    }
    
    // Buscar por material
    public List<EspecificacionProducto> buscarPorMaterial(String material) {
        return especificacionRepository.findByMaterialPrincipal(material);
    }
    
    // Método helper para validar especificación
    private void validarEspecificacion(EspecificacionProducto especificacion) {
        if (especificacion == null) {
            throw new IllegalArgumentException("La especificación no puede ser nula");
        }
        
        // Validaciones básicas
        if (especificacion.getMaterialPrincipal() != null && especificacion.getMaterialPrincipal().trim().isEmpty()) {
            throw new IllegalArgumentException("El material principal no puede estar vacío");
        }
        
        if (especificacion.getOrigen() != null && especificacion.getOrigen().trim().isEmpty()) {
            throw new IllegalArgumentException("El origen no puede estar vacío");
        }
    }
}