package com.utp.artesaniamvc.service;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private LocalRepository localRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private TamanoRepository tamanoRepository;
    @Autowired
    private PrecioTamanoRepository precioTamanoRepository;

    public Pedido crearPedido(Pedido pedido, Integer localId, Usuario usuario, List<DetallePedido> detalles) {

        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new EntityNotFoundException("Local no encontrado"));

        pedido.setLocal(local);
        pedido.setUsuario(usuario);
        Date fechaCreacion = new Date();
        pedido.setFechaCreacion(fechaCreacion);

        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setNumero(generarNumeroPedido());

        return pedidoRepository.save(pedido);
    }

    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    // Método para buscar pedido por ID
    public Pedido findById(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido con ID " + pedidoId + " no encontrado"));
    }

    public List<Pedido> findUltimos10Pedidos() {
        return pedidoRepository.findTop10ByOrderByFechaCreacionDesc();
    }

    public List<Pedido> findUltimos10PedidosPorLocal(String nombreLocal) {
        Pageable limit = PageRequest.of(0, 10);
        return pedidoRepository.findTop10ByLocalNombreOrderByFechaCreacionDesc(nombreLocal, limit);
    }

    // Método para obtener una lista de contactos con paginación
    public Page<Pedido> getAllPedidos(Pageable pageable) {
        return pedidoRepository.findAllByOrderByIdDesc(pageable);
    }

    @Transactional
    public void agregarDetallePedido(Integer pedidoId, Integer productoId,
            Integer tamanoId, Integer cantidad) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        // Validación de stock por local y tamaño
        validarStockDisponible(producto, pedido.getLocal(), tamanoId, cantidad);

        // Calcular precio y reducir stock
        DetallePedido detalle = calcularDetallePedido(producto, pedido, tamanoId, cantidad);

        detallePedidoRepository.save(detalle);
        actualizarStockProducto(producto, pedido.getLocal(), tamanoId, cantidad);
    }

    private void validarStockDisponible(Producto producto, Local local,
            Integer tamanoId, Integer cantidad) {
        Double stockDisponible = obtenerStockPorLocal(producto, local);
        Tamano tamano = obtenerTamano(tamanoId);
        Double cantidadARestar = tamano.getFactorPrecio() * cantidad;

        if (stockDisponible < cantidadARestar) {
            throw new IllegalArgumentException("Stock insuficiente en el local seleccionado");
        }
    }

    private DetallePedido calcularDetallePedido(Producto producto, Pedido pedido,
            Integer tamanoId, Integer cantidad) {
        Tamano tamano = obtenerTamano(tamanoId);
        PrecioTamano precioTamano = obtenerPrecioTamano(producto, tamano);

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setTamano(tamano);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(precioTamano.getPrecio());
        detalle.setTotal(precioTamano.getPrecio() * cantidad);

        return detalle;
    }

    public PrecioTamano obtenerPrecioTamano(Producto producto, Tamano tamano) {
        // Buscar el precio por tamaño específico para este producto y tamaño
        PrecioTamano precioTamano = precioTamanoRepository
                .findByProductoIdAndTamanoId(producto.getId(), tamano.getId());

        // Si no existe un precio específico, calcular basado en el precio base del producto
        if (precioTamano == null) {
            Double precioCalculado = producto.getPrecio() * tamano.getFactorPrecio();
            precioTamano = new PrecioTamano();
            precioTamano.setProducto(producto);
            precioTamano.setTamano(tamano);
            precioTamano.setPrecio(precioCalculado);
        }

        return precioTamano;
    }

    private void actualizarStockProducto(Producto producto, Local local,
            Integer tamanoId, Integer cantidad) {
        Tamano tamano = obtenerTamano(tamanoId);
        Double cantidadARestar = tamano.getFactorPrecio() * cantidad;

        if (local.getNombre().equals("Open Plaza")) {
            producto.setStockOpenPlaza(producto.getStockOpenPlaza() - cantidadARestar);
        } else if (local.getNombre().equals("UDEP")) {
            producto.setStockUDEP(producto.getStockUDEP() - cantidadARestar);
        }

        producto.setStockActual(producto.getStockOpenPlaza() + producto.getStockUDEP());
        productoRepository.save(producto);
    }

    // Métodos de ayuda (deberían implementarse con las lógicas correspondientes)
    private Double obtenerStockPorLocal(Producto producto, Local local) {
        return local.getNombre().equals("Open Plaza")
                ? producto.getStockOpenPlaza() : producto.getStockUDEP();
    }

    private Tamano obtenerTamano(Integer tamanoId) {
        // Implementar la lógica para obtener el tamaño
        return tamanoRepository.findById(tamanoId)
                .orElseThrow(() -> new EntityNotFoundException("Tamaño no encontrado"));
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public long countAllPedidos() {
        return pedidoRepository.count();
    }
    
    // Método para obtener pedidos de un local específico
    public List<Pedido> findPedidosByLocal(Local local) {
        return pedidoRepository.findPedidosByLocal(local);
    }

    // Método para obtener pedidos recientes de un local específico
    public List<Pedido> findRecentPedidosByLocal(Local local) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("fechaCreacion").descending());
        return pedidoRepository.findRecentPedidosByLocal(local, pageable);
    }

    // Método para obtener los pedidos recientes (por ejemplo, los últimos 5)
    public List<Pedido> findRecentPedidos() {
        // Puedes usar un Sort para ordenar por fecha de creación en orden descendente
        Pageable pageable = PageRequest.of(0, 5, Sort.by("fechaCreacion").descending());
        return pedidoRepository.findAll(pageable).getContent();
    }

    public List<Pedido> findByUsuario(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }

    public Optional<Pedido> findByNumero(String numero) {
        return pedidoRepository.findByNumero(numero);
    }

    public String generarNumeroPedido() {
        // Genera un número único para el pedido
        int numero = 0;
        String numeroConcatenado = "";

        List<Pedido> pedidos = findAll();

        List<Integer> numeros = new ArrayList<Integer>();

        pedidos.stream().forEach(o -> numeros.add(Integer.parseInt(o.getNumero())));

        if (pedidos.isEmpty()) {
            numero = 1;
        } else {
            numero = numeros.stream().max(Integer::compare).get();
            numero++;
        }

        if (numero < 10) {
            numeroConcatenado = "000000000" + String.valueOf(numero);
        } else if (numero < 100) {
            numeroConcatenado = "00000000" + String.valueOf(numero);
        } else if (numero < 1000) {
            numeroConcatenado = "0000000" + String.valueOf(numero);
        } else if (numero < 10000) {
            numeroConcatenado = "0000000" + String.valueOf(numero);
        }

        return numeroConcatenado;
    }
    
      public List<Pedido> findPedidosAbiertos() {
        // Filtrar los pedidos cuyo estado sea 'ABIERTO'
        return pedidoRepository.findByEstado(EstadoPedido.PENDIENTE);
    }
      
       // Método para obtener los pedidos específicos para UDEP
    public List<Pedido> findPedidosUdep() {
        // Suponiendo que UDEP ve pedidos con un estado especial o pertenecientes a una categoría específica
        // Aquí solo como ejemplo, puedes adaptarlo a tu lógica de negocio
        return pedidoRepository.findByEstado(EstadoPedido.PROCESANDO); // O cualquier estado que defina UDEP
    }
    
     public Pedido findPedidoActivoByUsuario(Usuario usuario) {
        // Buscar pedido pendiente
        Pedido pedidoPendiente = pedidoRepository.findByUsuarioAndEstado(usuario, EstadoPedido.PENDIENTE);
        
        if (pedidoPendiente != null) {
            return pedidoPendiente;
        }
        
        // Si no hay pedido pendiente, buscar pedidos en proceso
        Pedido pedidoEnProceso = pedidoRepository.findByUsuarioAndEstado(usuario, EstadoPedido.PROCESANDO);
        
        if (pedidoEnProceso != null) {
            return pedidoEnProceso;
        }
        
        // Si no hay pedidos activos, devolver null o crear un nuevo pedido
        return null;
    }
    
    // Método alternativo si quieres buscar múltiples estados a la vez
    public Pedido findPedidoActivoByUsuarioFlexible(Usuario usuario) {
        List<EstadoPedido> estadosActivos = Arrays.asList(
            EstadoPedido.PENDIENTE, 
            EstadoPedido.PROCESANDO
        );
        
        List<Pedido> pedidosActivos = pedidoRepository.findByUsuarioAndEstadoIn(usuario, estadosActivos);
        
        return pedidosActivos.isEmpty() ? null : pedidosActivos.get(0);
    }
    

}
