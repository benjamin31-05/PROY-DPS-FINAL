package com.utp.artesaniamvc.controller;

import com.itextpdf.text.DocumentException;
import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.security.SecurityUserDetails;
import com.utp.artesaniamvc.service.*;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/udep")
public class UdepController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;  // Añadir esta inyección

    @Autowired
    private ContactoService contactoService; // Añadir esta inyección

    @Autowired
    private PagoPedidoRepository pagoPedidoRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private LocalService localService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ComprobantePDFService comprobantePDFService;
    
     @Autowired
    private VentaService ventasService;

    @GetMapping("/home")
    public String homeUdep(Authentication authentication, Model model) {
        // Verificar que el usuario tiene rol OPEN
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_UDEP"))) {
            return "redirect:/login?error";
        }

        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        Usuario usuarioActual = userDetails.getUsuario();

        // Obtener el local Open Plaza (asumiendo que tienes un método para encontrarlo)
        Local localUDEP = localService.findByNombre("UDEP");
        System.out.println("Local UDEP: " + localUDEP);

        // Obtener pedidos específicamente para Open Plaza
        List<Pedido> pedidosUDEP = pedidoService.findPedidosByLocal(localUDEP);
        long totalPedidos = pedidosUDEP.size();
        System.out.println("Total de pedidos: " + pedidosUDEP.size());
        
        // Imprimir detalles de cada pedido para verificar
        pedidosUDEP.forEach(pedido -> {
            System.out.println("Pedido ID: " + pedido.getId()
                    + ", Estado: " + pedido.getEstado()
                    + ", Total: " + pedido.getTotal());
        });
        
           // Calcular ingresos solo de pedidos PAGADOS
        long totalPedidosPagados = pedidosUDEP.stream()
                .filter(pedido -> pedido.getEstado() == EstadoPedido.PAGADO)
                .count();
        System.out.println("Total de pedidos PAGADOS: " + totalPedidosPagados);
        
        double totalIngresos = pedidosUDEP.stream()
                .filter(pedido -> pedido.getEstado() == EstadoPedido.PAGADO)
                .mapToDouble(Pedido::getTotal)
                .sum();
        System.out.println("Total Ingresos PAGADOS: " + totalIngresos);

        // Obtener estadísticas
        List<Usuario> clientes = usuarioService.findAllClientUsers();
        long totalContactos = contactoService.countAllContactos(); // Obtener total de contactos

        // Obtener últimos clientes (limitado a 5 por ejemplo)
        List<Usuario> ultimosClientes = clientes.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Obtener pedidos recientes específicamente para Open Plaza
        List<Pedido> pedidosRecientes = pedidoService.findRecentPedidosByLocal(localUDEP);

        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("totalClientes", clientes.size());
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalContactos", totalContactos);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("ultimosClientes", ultimosClientes);
        model.addAttribute("pedidosRecientes", pedidosRecientes);

        return "udep/homeUdep";  // Usando la misma plantilla
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Authentication authentication,
            Model model,
            @RequestParam(defaultValue = "0") int page) {
        // Verificar que el usuario tiene rol ADMIN
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_UDEP"))) {
            return "redirect:/login?error";
        }

        Page<Usuario> clientesPage = usuarioService.findAllClientUsers(PageRequest.of(page, 10));

        model.addAttribute("clientes", clientesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientesPage.getTotalPages());
        model.addAttribute("totalClientes", clientesPage.getTotalElements());

        return "udep/usuarios";
    }

    @GetMapping("/contacts")
    public String showContacts(Model model, @RequestParam(defaultValue = "0") int page) {

        Page<Contacto> contactosPage = contactoService.getAllContactos(PageRequest.of(page, 6));
        model.addAttribute("contactos", contactosPage);
        return "udep/verAsuntos";

    }

    @GetMapping("/deleteAsunto/{id}")
    public String deleteAsuntos(@PathVariable Integer id) {
        contactoService.deleteContacto(id);
        return "redirect:/udep/contacts";
    }

    @GetMapping("/responderAsunto/{id}")
    public String responderAsunto(@PathVariable Integer id, Model model) {
        Contacto contacto = contactoService.getContactoById(id);
        if (contacto == null) {
            return "redirect:/udep/contacts";  // Redirige si el contacto no existe
        }
        model.addAttribute("contacto", contacto);
        return "udep/responderAsunto"; // Vista para responder
    }

    @PostMapping("/responderAsunto/{id}")
    public String enviarRespuesta(@PathVariable Integer id, @RequestParam("respuesta") String respuesta) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía.");
        }

        // Buscar el contacto por ID
        Contacto contacto = contactoService.getContactoById(id);
        if (contacto == null) {
            return "redirect:/udep/contacts";  // Redirigir si no se encuentra el contacto
        }

        // Guardar la respuesta
        contacto.setRespuesta(respuesta);
        contactoService.updateContacto(contacto);

        // Enviar correo de respuesta
        emailService.enviarCorreoRespuesta(contacto.getEmail(), contacto.getNombre(),
                contacto.getAsunto().getDescription(), respuesta);

        return "redirect:/udep/contacts"; // Redirigir a la lista de contactos
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
        return "openPlaza/indexAdmin";
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
        return "udep/pedidos";
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
        return "udep/detalle-pedido";
    }

    @GetMapping("/allPedidos")
    public String showPedidos(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String local
    ) throws AccessDeniedException {
        // Obtener el usuario actual
        Usuario usuarioActual = usuarioService.findByUsername(authentication.getName());

        Pageable pageable = PageRequest.of(page, 6);
        Page<DetallePedido> detallesPedidosPage;

        // Verificar el rol del usuario
        if (usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("ADMIN"))) {
            // Si es ADMIN, permitir filtrar por local
            if (local != null && !local.isEmpty()) {
                detallesPedidosPage = detallePedidoService.getDetallesPedidosByLocal(local, pageable);
            } else {
                detallesPedidosPage = detallePedidoService.getAllDetallesPedidos(pageable);
            }
            model.addAttribute("locales", Arrays.asList("Open Plaza", "UDEP")); // Solo ADMIN puede ver esta opción
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
        return "udep/verPedidos";
    }

    @GetMapping("/procesar/{detallePedidoId}")
    public String mostrarFormularioPago(@PathVariable Integer detallePedidoId, Model model) {
        DetallePedido detallePedido = detallePedidoService.findById(detallePedidoId);
        model.addAttribute("detallePedido", detallePedido);
        return "udep/procesarPago"; // Nueva vista para procesar pago
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
            return "redirect:/udep/comprobante";
        } catch (IllegalArgumentException e) {
            // Manejar específicamente montos insuficientes
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/udep/procesar/" + detallePedidoId;
        } catch (Exception e) {
            // Log del error completo
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
            return "redirect:/udep/procesar/" + detallePedidoId;
        }
    }

    @GetMapping("/comprobante")
    public String mostrarComprobante(Model model, RedirectAttributes redirectAttributes) {
        // Recupera el ID del pago de los flash attributes
        Integer pagoPedidoId = (Integer) model.getAttribute("pagoPedidoId");

        if (pagoPedidoId == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el ID del pago");
            return "redirect:/udep/";  // Redirige a una página de inicio o error
        }

        try {
            PagoPedido pagoPedido = pagoPedidoRepository.findById(pagoPedidoId)
                    .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));

            model.addAttribute("pagoPedido", pagoPedido);
            return "udep/comprobante";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al recuperar el comprobante: " + e.getMessage());
            return "redirect:/udep/";
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
        return "udep/detalles-pago";
    }
    
    @GetMapping("/ventas")
    public String mostrarVentas(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
            @RequestParam(defaultValue = "0") int page
    ) throws AccessDeniedException {
        // Obtener el usuario actual
        Usuario usuarioActual = usuarioService.findByUsername(authentication.getName());

        // Verificar que sea un usuario de OPEN PLAZA
        if (!usuarioActual.getRoles().stream().anyMatch(r -> r.getNombre().equals("UDEP"))) {
            throw new AccessDeniedException("No tiene permisos para ver ventas");
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by("pedido.fechaCreacion").descending());

        // Añadir logging para depuración
        System.out.println("Fecha Inicio recibida: " + fechaInicio);
        System.out.println("Fecha Fin recibida: " + fechaFin);

        // Si no se proporcionan fechas, usar un rango predeterminado
        if (fechaInicio == null || fechaFin == null) {
            Calendar cal = Calendar.getInstance();
            fechaFin = cal.getTime();
            cal.add(Calendar.MONTH, -1);
            fechaInicio = cal.getTime();
        }

        // Establecer las horas para cubrir todo el día
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

        // Añadir más logging
        System.out.println("Fecha Inicio ajustada: " + calInicio.getTime());
        System.out.println("Fecha Fin ajustada: " + calFin.getTime());

        Page<DetallePedido> ventasPage = ventasService.obtenerVentasPorLocal(
                "UDEP", calInicio.getTime(), calFin.getTime(), pageable
        );

        // Logging de resultados
        System.out.println("Número de ventas encontradas: " + ventasPage.getContent().size());

        // Calcular totales de ventas
        Map<String, Object> totales = ventasService.calcularTotalesVentas(ventasPage);

        model.addAttribute("ventas", ventasPage);
        model.addAttribute("totales", totales);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "udep/ventas";
    }
    
    
    //verificar el pago de yape
    @GetMapping("/verificar/{detalleId}")
    @PreAuthorize("hasRole('UDEP')")
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
            return "udep/validar-pago";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró información de pago para este pedido");
            return "redirect:/udep/allPedidos";
        }
    }

    @PostMapping("/validar-pago")
    @PreAuthorize("hasRole('UDEP')")
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
        return "redirect:/udep/allPedidos";
    }



}
