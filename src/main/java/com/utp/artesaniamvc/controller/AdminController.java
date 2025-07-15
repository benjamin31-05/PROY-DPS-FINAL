package com.utp.artesaniamvc.controller;

import com.itextpdf.text.DocumentException;
import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.service.*;
import com.utp.artesaniamvc.security.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;  // Añadir esta inyección

    @Autowired
    private ContactoService contactoService; // Añadir esta inyección

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PagoPedidoRepository pagoPedidoRepository;

    @Autowired
    private LocalService localService;

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ComprobantePDFService comprobantePDFService;

    @Autowired
    private VentaService ventasService;

    @Autowired
    private VentasPDFService ventasPdfGeneratorService;

    @GetMapping("/home")
    public String homeAdmin(Authentication authentication, Model model) {
        // Verificar que el usuario tiene rol ADMIN
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error";
        }

        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        Usuario usuarioActual = userDetails.getUsuario();

        // Obtener estadísticas
        List<Usuario> clientes = usuarioService.findAllClientUsers();
        int totalPedidos = (int) pedidoService.countAllPedidos();
        long totalContactos = contactoService.countAllContactos(); // Obtener total de contactos
        // Calcular total de ingresos SOLO de pedidos PAGADOS
        double totalIngresos = pedidoService.findAll().stream()
                .filter(pedido -> pedido.getEstado() == EstadoPedido.PAGADO)
                .mapToDouble(Pedido::getTotal)
                .sum();
        // Obtener últimos clientes (limitado a 5 por ejemplo)
        List<Usuario> ultimosClientes = clientes.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Obtener pedidos recientes
        List<Pedido> pedidosRecientes = pedidoService.findRecentPedidos();

        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("totalClientes", clientes.size());
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalContactos", totalContactos);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("ultimosClientes", ultimosClientes);
        model.addAttribute("pedidosRecientes", pedidosRecientes);

        return "administrador/homeAdmin";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Authentication authentication,
            Model model,
            @RequestParam(defaultValue = "0") int page) {
        // Verificar que el usuario tiene rol ADMIN
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error";
        }

        Page<Usuario> clientesPage = usuarioService.findAllClientUsers(PageRequest.of(page, 10));

        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientesPage.getTotalPages());
        model.addAttribute("totalClientes", clientesPage.getTotalElements());

        return "administrador/usuarios";
    }

    @GetMapping("/productos")
    public String index(Authentication authentication, Model model, @RequestParam(defaultValue = "0") int page) {
        // Verificar que el usuario tiene rol ADMIN
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error";
        }

        List<Producto> latestProductos = productoService.getLatestProductos();
        Page<Producto> productosPage = productoService.getAllProductos(PageRequest.of(page, 6));

        model.addAttribute("latestProductos", latestProductos);
        model.addAttribute("productos", productosPage);
        return "administrador/indexAdmin";
    }

    @GetMapping("/pedidos/recientes")
    public String pedidosRecientes(Model model, Authentication authentication) {
        // Obtener el usuario actual
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);

        List<Pedido> pedidosRecientes;

        // Si es admin, muestra todos los pedidos recientes
        if (usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMIN"))) {
            pedidosRecientes = pedidoService.findUltimos10Pedidos();
        } // Si es cajero de Open Plaza, muestra solo pedidos de Open Plaza
        else if (usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("OPEN"))) {
            pedidosRecientes = pedidoService.findUltimos10PedidosPorLocal("Open Plaza");
        } // Si es cajero de UDEP, muestra solo pedidos de UDEP
        else if (usuario.getRoles().stream().anyMatch(r -> r.getNombre().equals("UDEP"))) {
            pedidosRecientes = pedidoService.findUltimos10PedidosPorLocal("UDEP");
        } else {
            pedidosRecientes = new ArrayList<>();
        }

        model.addAttribute("pedidosRecientes", pedidosRecientes);
        return "administrador/pedidos";
    }

    @GetMapping("/pedidos/detalle/{id}")
    public String detallePedido(@PathVariable Integer id, Model model) {
        Pedido pedido = pedidoService.findById(id);

        // Find payment details for each order detail
        List<PagoPedido> pagosPedido = new ArrayList<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            pagoPedidoRepository.findByDetallePedido_Id(detalle.getId())
                    .ifPresent(pagosPedido::add);
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("pagosPedido", pagosPedido);
        return "administrador/detalle-pedido";
    }
//    @GetMapping("/allPedidos")
//    public String showPedidos(Model model, @RequestParam(defaultValue = "0") int page) {
//        Page<DetallePedido> detallesPedidosPage = detallePedidoService.getAllDetallesPedidos(PageRequest.of(page, 6));
//        model.addAttribute("detallesPedidos", detallesPedidosPage);
//        return "administrador/verPedidos";
//    }
//    @GetMapping("/allPedidos")
//    public String showPedidos(
//            Model model,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(required = false) String local
//    ) {
//        Pageable pageable = PageRequest.of(page, 6);
//        Page<DetallePedido> detallesPedidosPage;
//
//        if (local != null && !local.isEmpty()) {
//            // Filtrar por local específico
//            detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocal(local, pageable);
//        } else {
//            // Mostrar todos los pedidos
//            detallesPedidosPage = detallePedidoService.getAllDetallesPedidos(pageable);
//        }
//
//        model.addAttribute("detallesPedidos", detallesPedidosPage);
//        model.addAttribute("locales", Arrays.asList("Open Plaza", "UDEP"));
//        return "administrador/verPedidos";
//    }

    @GetMapping("/allPedidos")
    public String showPedidos(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String local,
            @RequestParam(required = false) String usuario
    ) throws AccessDeniedException {
        // Obtener el usuario actual
        Usuario usuarioActual = usuarioService.findByUsername(authentication.getName());
        Pageable pageable = PageRequest.of(page, 6);
        Page<DetallePedido> detallesPedidosPage;

        // Verificar el rol del usuario
        if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMIN"))) {
            // Si es ADMIN, permitir filtrar por local, usuario o ambos
            if (local != null && !local.isEmpty() && usuario != null && !usuario.isEmpty()) {
                // Filtrar por local y usuario
                detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocalAndUsuario(local, usuario, pageable);
            } else if (local != null && !local.isEmpty()) {
                // Filtrar solo por local
                detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocal(local, pageable);
            } else if (usuario != null && !usuario.isEmpty()) {
                // Filtrar solo por usuario
                detallesPedidosPage = detallePedidoService.getDetallesPedidosByUsuario(usuario, pageable);
            } else {
                // Sin filtros
                detallesPedidosPage = detallePedidoService.getAllDetallesPedidos(pageable);
            }

            // Agregar lista de locales y usuarios para los selectores
            model.addAttribute("locales", Arrays.asList("Open Plaza", "UDEP"));
            model.addAttribute("usuarios", detallePedidoService.getAllClientes());
        } else if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("OPEN"))) {
            // Personal de OPEN ve solo pedidos de OPEN
            detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocal("Open Plaza", pageable);
        } else if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("UDEP"))) {
            // Personal de UDEP ve solo pedidos de UDEP
            detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocal("UDEP", pageable);
        } else {
            throw new AccessDeniedException("No tiene permisos para ver pedidos");
        }

        // Agregar los pedidos al modelo
        model.addAttribute("detallesPedidos", detallesPedidosPage);
        return "administrador/verPedidos";
    }

    @GetMapping("/procesar/{detallePedidoId}")
    public String mostrarFormularioPago(@PathVariable Integer detallePedidoId, Model model) {
        DetallePedido detallePedido = detallePedidoService.findById(detallePedidoId);
        model.addAttribute("detallePedido", detallePedido);
        return "administrador/procesarPago"; // Nueva vista para procesar pago
    }

    @PostMapping("/procesar")
    public String procesarPago(
            @RequestParam Integer detallePedidoId,
            @RequestParam double montoPagado,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PagoPedido pagoGuardado = pagoService.procesarPago(detallePedidoId, montoPagado, authentication);
            // Usa addFlashAttribute en lugar de addAttribute
            redirectAttributes.addFlashAttribute("pagoPedidoId", pagoGuardado.getId());
            return "redirect:/admin/comprobante";
        } catch (IllegalArgumentException e) {
            // Manejar específicamente montos insuficientes
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/procesar/" + detallePedidoId;
        } catch (Exception e) {
            // Log del error completo
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
            return "redirect:/admin/procesar/" + detallePedidoId;
        }
    }

    @GetMapping("/comprobante")
    public String mostrarComprobante(Model model, RedirectAttributes redirectAttributes) {
        // Recupera el ID del pago de los flash attributes
        Integer pagoPedidoId = (Integer) model.getAttribute("pagoPedidoId");

        if (pagoPedidoId == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el ID del pago");
            return "redirect:/admin/";  // Redirige a una página de inicio o error
        }

        try {
            PagoPedido pagoPedido = pagoPedidoRepository.findById(pagoPedidoId)
                    .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

            model.addAttribute("pagoPedido", pagoPedido);
            return "administrador/comprobante";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al recuperar el comprobante: " + e.getMessage());
            return "redirect:/admin/";
        }
    }

    @GetMapping("/generarComprobantePDF/{pagoPedidoId}")
    public ResponseEntity<InputStreamResource> generarComprobantePDF(
            @PathVariable Integer pagoPedidoId,
            HttpServletResponse response
    ) throws IOException, DocumentException {
        ByteArrayInputStream bis = comprobantePDFService.generarComprobantePDF(pagoPedidoId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=comprobante.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/detalles-pago/{detallePedidoId}")
    public String verDetallesPago(@PathVariable Integer detallePedidoId, Model model) {
        DetallePedido detallePedido = detallePedidoService.findById(detallePedidoId);

        // Try to find existing PagoPedido
        PagoPedido pagoPedido = pagoPedidoRepository.findByDetallePedido_Id(detallePedidoId)
                .orElseGet(() -> {
                    // If no PagoPedido exists, create a new one
                    PagoPedido newPagoPedido = new PagoPedido();
                    newPagoPedido.setDetallePedido(detallePedido);
                    newPagoPedido.setMontoPagado(detallePedido.getTotal());
                    newPagoPedido.setVuelto(0.0);
                    newPagoPedido.setFechaPago(detallePedido.getPedido().getFechaCreacion());
                    newPagoPedido.setUsuarioPago(detallePedido.getPedido().getUsuario());
                    return pagoPedidoRepository.save(newPagoPedido);
                });

        model.addAttribute("pagoPedido", pagoPedido);
        model.addAttribute("metodoPago", detallePedido.getPedido().getMetodoPago());
        return "administrador/detalles-pago";
    }

    @GetMapping("/ventas")
    public String mostrarVentas(
            Model model,
            @RequestParam(required = false) String local,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
            @RequestParam(defaultValue = "0") int page
    ) {
        // Configurar fechas si no se proporcionan
        if (fechaInicio == null || fechaFin == null) {
            Calendar cal = Calendar.getInstance();
            fechaFin = cal.getTime();
            cal.add(Calendar.MONTH, -1);
            fechaInicio = cal.getTime();
        }

        // Ajustar horas de las fechas
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(fechaInicio);
        calInicio.set(Calendar.HOUR_OF_DAY, 0);
        calInicio.set(Calendar.MINUTE, 0);
        calInicio.set(Calendar.SECOND, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = Calendar.getInstance();
        calFin.setTime(fechaFin);
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        // Configurar paginación
        Pageable pageable = PageRequest.of(page, 10, Sort.by("pedido.fechaCreacion").descending());

        // Obtener ventas con filtros
        Page<DetallePedido> ventasPage;
        if (StringUtils.hasText(local)) {
            ventasPage = ventasService.obtenerVentasPorLocalAdmin(
                    local, calInicio.getTime(), calFin.getTime(), pageable
            );
        } else {
            ventasPage = ventasService.obtenerTodasVentasAdmin(
                    calInicio.getTime(), calFin.getTime(), pageable
            );
        }

        // Calcular totales
        Map<String, Object> totales = ventasService.calcularTotalesVentas(ventasPage);

        // Obtener lista de locales para el filtro
        List<Local> locales = localService.obtenerTodosLocales();

        model.addAttribute("ventas", ventasPage);
        model.addAttribute("totales", totales);
        model.addAttribute("locales", locales);
        model.addAttribute("localSeleccionado", local);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "administrador/ventas";
    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<InputStreamResource> generarVentasPDF(
            @RequestParam(required = false) String local,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin
    ) throws DocumentException {
        // Ajustar horas de las fechas (igual que en el método de ventas)
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(fechaInicio);
        calInicio.set(Calendar.HOUR_OF_DAY, 0);
        calInicio.set(Calendar.MINUTE, 0);
        calInicio.set(Calendar.SECOND, 0);
        calInicio.set(Calendar.MILLISECOND, 0);

        Calendar calFin = Calendar.getInstance();
        calFin.setTime(fechaFin);
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);
        calFin.set(Calendar.MILLISECOND, 999);

        ByteArrayInputStream bis = ventasPdfGeneratorService.generarReporteVentasPDF(
                local,
                calInicio.getTime(),
                calFin.getTime()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_ventas.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    
    //verificar el pago de yape
    @GetMapping("/verificar/{detalleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String verificarPagoYape(
            @PathVariable Integer detalleId,
            Model model,
            RedirectAttributes redirectAttributes,  Authentication authentication // Agrega esto
    ) {
        DetallePedido detalle = detallePedidoService.findById(detalleId);

        // Buscar el PagoPedido específicamente para este detalle
        Optional<PagoPedido> pagoPedidoOpt = pagoPedidoRepository.findByDetallePedido(detalle);

        if (pagoPedidoOpt.isPresent()) {
            PagoPedido pagoPedido = pagoPedidoOpt.get();
            model.addAttribute("detalle", detalle);
            model.addAttribute("pagoPedido", pagoPedido);
            return "administrador/validar-pago";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró información de pago para este pedido");
            return "redirect:/admin/allPedidos";
        }
    }

    @PostMapping("/validar-pago")
    @PreAuthorize("hasRole('ADMIN')")
    public String validarPago(
            @RequestParam Integer detalleId,
            @RequestParam String accion,
            RedirectAttributes redirectAttributes
    ) {
        DetallePedido detalle = detallePedidoService.findById(detalleId);
        Pedido pedido = detalle.getPedido();

        if ("aprobar".equals(accion)) {
            detalle.setEstadoPago("PAGADO");
            pedido.setEstado(EstadoPedido.PAGADO);
        } else {
            detalle.setEstadoPago("CANCELADO");
            pedido.setEstado(EstadoPedido.CANCELADO);
        }

        detallePedidoService.save(detalle);
        pedidoService.save(pedido);

        redirectAttributes.addFlashAttribute("successMessage", "Pago " + accion + " exitosamente");
        return "redirect:/admin/allPedidos";
    }


}
