package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private LocalRepository localRepository;
    @Autowired
    private TamanoRepository tamanoRepository;
    @Autowired
    private PrecioTamanoRepository precioTamanoRepository;
     // NUEVA DEPENDENCIA PARA ESPECIFICACIONES
    @Autowired
    private EspecificacionProductoRepository especificacionRepository;

    @Transactional
    public Producto saveProducto(Producto producto, Integer subcategoriaId) {
        Subcategoria subcategoria = subcategoriaRepository
                .findById(subcategoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Subcategoria no encontrada"));

        validarProducto(producto);

        producto.setSubcategoria(subcategoria);
        producto.setStockActual(producto.getStockOpenPlaza() + producto.getStockUDEP());
        return productoRepository.save(producto);

    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    private void validarProducto(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (producto.getStockOpenPlaza() < 0 || producto.getStockUDEP() < 0) {
            throw new IllegalArgumentException("Los stocks no pueden ser negativos");
        }
    }

  // MÉTODO ACTUALIZADO para incluir especificaciones
    public Producto getProductoById(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
        
        // Cargar especificaciones si existen
        EspecificacionProducto especificacion = especificacionRepository.findByProductoId(id).orElse(null);
        producto.setEspecificacion(especificacion);
        
        return producto;
    }
    
     // NUEVO MÉTODO para obtener todos los productos con información de especificaciones
    public Page<Producto> getAllProductos(Pageable pageable) {
        Page<Producto> productos = productoRepository.findAll(pageable);
        
        // Para cada producto, verificar si tiene especificaciones
        productos.forEach(producto -> {
            EspecificacionProducto especificacion = especificacionRepository
                    .findByProductoId(producto.getId()).orElse(null);
            producto.setEspecificacion(especificacion);
        });
        
        return productos;
    }
    
    // NUEVO MÉTODO para obtener productos con especificaciones completas
    public List<Producto> getProductosConEspecificacionesCompletas() {
        List<EspecificacionProducto> especificaciones = especificacionRepository.findProductosConHistoriaCompleta();
        return especificaciones.stream()
                .map(EspecificacionProducto::getProducto)
                .collect(Collectors.toList());
    }
    
    // NUEVO MÉTODO para buscar productos por características
    public List<Producto> buscarProductosPorMaterial(String material) {
        List<EspecificacionProducto> especificaciones = especificacionRepository.findByMaterialPrincipal(material);
        return especificaciones.stream()
                .map(EspecificacionProducto::getProducto)
                .collect(Collectors.toList());
    }
    
    // NUEVO MÉTODO para buscar productos por región
    public List<Producto> buscarProductosPorRegion(String region) {
        List<EspecificacionProducto> especificaciones = especificacionRepository.findByRegionProcedencia(region);
        return especificaciones.stream()
                .map(EspecificacionProducto::getProducto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrecioTamano asignarPrecioTamano(Integer productoId, Integer tamanoId, Double precio) {
        Producto producto = getProductoById(productoId);
        Tamano tamano = tamanoRepository.findById(tamanoId)
                .orElseThrow(() -> new EntityNotFoundException("Tamaño no encontrado"));

        PrecioTamano precioTamano = new PrecioTamano();
        precioTamano.setProducto(producto);
        precioTamano.setTamano(tamano);
        precioTamano.setPrecio(precio);

        return precioTamanoRepository.save(precioTamano);
    }

    @Transactional
    public Producto updateProducto(Integer id, Producto updatedProducto) {
        Producto existingProducto = getProductoById(id);

        validarProducto(updatedProducto);

        // Validar nombre duplicado
        Producto productoConMismoNombre = productoRepository.findByNombreProducto(updatedProducto.getNombreProducto());
        if (productoConMismoNombre != null && !productoConMismoNombre.getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }

        existingProducto.setNombreProducto(updatedProducto.getNombreProducto());
        existingProducto.setDescripcion(updatedProducto.getDescripcion());
        existingProducto.setPrecio(updatedProducto.getPrecio());
        existingProducto.setSubcategoria(updatedProducto.getSubcategoria());
        existingProducto.setStockOpenPlaza(updatedProducto.getStockOpenPlaza());
        existingProducto.setStockUDEP(updatedProducto.getStockUDEP());
        existingProducto.setStockActual(updatedProducto.getStockOpenPlaza() + updatedProducto.getStockUDEP());
        existingProducto.setDisponiblePorTemporada(updatedProducto.isDisponiblePorTemporada());

        return productoRepository.save(existingProducto);
    }

    @Transactional
    public void deleteProducto(Integer id) {
        // Eliminar especificaciones primero (si existen)
        if (especificacionRepository.existsByProductoId(id)) {
            especificacionRepository.deleteByProductoId(id);
        }
        
        // Luego eliminar el producto
        productoRepository.deleteById(id);
    }

    public List<Producto> getLatestProductos() {
        return productoRepository.findTop6ByOrderByCreatedAtDesc();
    }



    public Page<Producto> getAllProductosDisponibles(Pageable pageable) {
        Page<Producto> allProductos = productoRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<Producto> productosDisponibles = allProductos.getContent().stream()
                .filter(Producto::isDisponiblePorTemporada)
                .collect(Collectors.toList());

        // Obtener el total de productos disponibles
        long totalProductosDisponibles = productoRepository.findAll().stream()
                .filter(Producto::isDisponiblePorTemporada)
                .count();

        return new PageImpl<>(productosDisponibles, pageable, totalProductosDisponibles);
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public List<Producto> findAllAvailable() {
        return productoRepository.findAll()
                .stream()
                .filter(Producto::isDisponiblePorTemporada)
                .collect(Collectors.toList());
    }

    public List<Producto> findBySubcategoriaId(Integer subcategoriaId) {
        return productoRepository.findBySubcategoriaId(subcategoriaId);
    }

    public Page<Producto> findBySubcategoriaIdDisponibles(Integer subcategoriaId, Pageable pageable) {
        List<Producto> productos = productoRepository.findBySubcategoriaId(subcategoriaId);
        List<Producto> productosDisponibles = productos.stream()
                .filter(Producto::isDisponiblePorTemporada)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productosDisponibles.size());

        List<Producto> pageContent = productosDisponibles.subList(start, end);
        return new PageImpl<>(pageContent, pageable, productosDisponibles.size());
    }

    public Page<Producto> findByCategoriaIdDisponibles(Integer categoriaId, Pageable pageable) {
        List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(categoriaId);
        List<Producto> productosDeCategoria = new ArrayList<>();

        for (Subcategoria subcategoria : subcategorias) {
            List<Producto> productos = productoRepository.findBySubcategoriaId(subcategoria.getId());
            productosDeCategoria.addAll(productos.stream()
                    .filter(Producto::isDisponiblePorTemporada)
                    .collect(Collectors.toList()));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), productosDeCategoria.size());

        List<Producto> pageContent = productosDeCategoria.subList(start, end);
        return new PageImpl<>(pageContent, pageable, productosDeCategoria.size());
    }

    public Optional<Producto> get(Integer id) {
        return productoRepository.findById(id);

    }

    // ProductoService.java
    public Producto getProductoByNombre(String nombreProducto) {
        return productoRepository.findByNombreProducto(nombreProducto);
    }

    // Método para buscar productos por nombre
    public List<Producto> buscarProductos(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    

}
