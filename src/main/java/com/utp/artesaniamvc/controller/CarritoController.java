package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.service.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CarritoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private AsuntoService asuntoService;

    @Autowired
    private ContactoService contactoService;

    @Autowired
    private SubcategoriaService subcategoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PagoPedidoRepository pagoPedidoRepository;

    @Autowired
    private SubcategoriaRepository subcategoriaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private DetallePedidoService detallePedidoService;

    @Autowired
    private LocalService localService;

    @Autowired
    private TamanoService tamanoService;

    //Para almacenar los detalles del Pedido
    List<DetallePedido> detalles = new ArrayList<DetallePedido>();

    //Detalles del pedido    
    Pedido pedido = new Pedido();

    @PostMapping("/cart")
    public String addCart(
            @RequestParam Integer id,
            @RequestParam Integer cantidad,
            @RequestParam Integer tamanoId,
            @RequestParam Integer localId,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Producto> optionalProducto = productoService.get(id);
        Optional<Tamano> optionalTamano = tamanoService.getById(tamanoId);
        Optional<Local> optionalLocal = localService.getById(localId);

        if (optionalProducto.isPresent() && optionalTamano.isPresent() && optionalLocal.isPresent()) {
            Producto producto = optionalProducto.get();
            Tamano tamano = optionalTamano.get();
            Local local = optionalLocal.get();

            // Obtener stock disponible según el local
            Double stockDisponible = (local.getNombre().equals("Open Plaza"))
                    ? producto.getStockOpenPlaza() : producto.getStockUDEP();

            // Calcular stock a restar (tamaño * cantidad)
            Double stockARestar = tamano.getFactorPrecio() * cantidad;

            // Validaciones de stock
            if (stockARestar > stockDisponible) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No hay suficiente stock en " + local.getNombre());
                return "redirect:/productoHome/" + id;
            }

            // Encontrar el precio del tamaño
            PrecioTamano precioTamano = producto.getPreciosPorTamano().stream()
                    .filter(pt -> pt.getTamano().getId().equals(tamanoId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Precio de tamaño no encontrado"));

            // Buscar si el producto ya existe en el carrito
            Optional<DetallePedido> productoExistente = detalles.stream()
                    .filter(dp -> dp.getProducto().getId().equals(id)
                    && dp.getLocal().getId().equals(localId)
                    && dp.getTamano().getId().equals(tamanoId))
                    .findFirst();

            if (productoExistente.isPresent()) {
                // Actualizar cantidad y total del producto existente
                DetallePedido detalleExistente = productoExistente.get();
                int nuevaCantidad = detalleExistente.getCantidad() + cantidad;
                detalleExistente.setCantidad(nuevaCantidad);

                // Recalcular total
                Double precioTotal = precioTamano.getPrecio() * nuevaCantidad;
                detalleExistente.setTotal(precioTotal);
            } else {
                // Crear nuevo detalle de pedido
                DetallePedido detallePedido = new DetallePedido();
                detallePedido.setProducto(producto);
                detallePedido.setNombre(producto.getNombreProducto());
                detallePedido.setCantidad(cantidad);
                detallePedido.setTamano(tamano);
                detallePedido.setLocal(local);
                detallePedido.setPrecio(precioTamano.getPrecio());

                // Calcular total considerando cantidad
                Double precioTotal = precioTamano.getPrecio() * cantidad;
                detallePedido.setTotal(precioTotal);

                // Agregar al carrito
                detalles.add(detallePedido);
            }

            // Actualizar stock del local
            actualizarStockLocal(producto, local, stockARestar);

            // Recalcular total del pedido
            double sumaTotal = detalles.stream().mapToDouble(DetallePedido::getTotal).sum();
            pedido.setTotal(sumaTotal);

            model.addAttribute("cart", detalles);
            model.addAttribute("pedido", pedido);
            return "cliente/carrito";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Error al agregar al carrito");
        return "redirect:/";
    }

    private void actualizarStockLocal(Producto producto, Local local, Double stockARestar) {
        if (local.getNombre().equals("Open Plaza")) {
            producto.setStockOpenPlaza(producto.getStockOpenPlaza() - stockARestar);
        } else {
            producto.setStockUDEP(producto.getStockUDEP() - stockARestar);
        }
        producto.setStockActual(producto.getStockOpenPlaza() + producto.getStockUDEP());
        productoService.saveProducto(producto);
    }

    //Quitar un producto del carrito 
    @GetMapping("/delete/cart/{id}")
    public String deleteProductoCart(@PathVariable Integer id, Model model) {

        // Buscar el detalle del pedido a eliminar
        // Buscar el detalle del pedido a eliminar
        DetallePedido detalleAEliminar = detalles.stream()
                .filter(d -> d.getProducto().getId().equals(id))
                .findFirst()
                .orElse(null);

        if (detalleAEliminar != null) {
            // Recuperar el producto de la base de datos para asegurar la información más actualizada
            Optional<Producto> optionalProducto = productoService.get(id);
            if (optionalProducto.isPresent()) {
                Producto producto = optionalProducto.get();
                Local local = detalleAEliminar.getLocal();
                Tamano tamano = detalleAEliminar.getTamano();

                // Calcular el stock a devolver (similar a como se calculó al agregar)
                Double stockADevolver = tamano.getFactorPrecio() * detalleAEliminar.getCantidad();

                // Devolver el stock al local específico
                if (local.getNombre().equals("Open Plaza")) {
                    producto.setStockOpenPlaza(producto.getStockOpenPlaza() + stockADevolver);
                } else {
                    producto.setStockUDEP(producto.getStockUDEP() + stockADevolver);
                }

                // Actualizar el stock actual
                producto.setStockActual(producto.getStockOpenPlaza() + producto.getStockUDEP());

                // Guardar el producto actualizado
                productoService.saveProducto(producto);
            }

            // Eliminar el producto del carrito
            detalles.removeIf(d -> d.getProducto().getId().equals(id));

            // Recalcular total
            double sumaTotal = detalles.stream()
                    .mapToDouble(DetallePedido::getTotal)
                    .sum();
            pedido.setTotal(sumaTotal);
        }

        model.addAttribute("cart", detalles);
        model.addAttribute("pedido", pedido);
        return "cliente/carrito";
    }

    //Llevar el carro a todos lados
    @GetMapping("/getCart")
    public String getCart(Model model
    ) {

        model.addAttribute("cart", detalles);
        model.addAttribute("pedido", pedido);
        return "/cliente/carrito";
    }

    //Vista hacia el detalle del Pedido 
    @GetMapping("/order")
    @PreAuthorize("hasRole('CLIENTE')")
    public String order(Model model, Authentication authentication) {
        // Obtener el nombre de usuario del usuario autenticado
        String username = authentication.getName();

        // Buscar el usuario por su username
        Usuario usuario = usuarioService.findByUsername(username);

        // Verificar si el pedido existe
        if (pedido == null) {
            // Manejar el caso cuando no hay pedido
            return "redirect:/error"; // o a una página de error personalizada
        }

        // Verificar si hay detalles en el pedido
        if (detalles == null || detalles.isEmpty()) {
            // Manejar el caso cuando no hay detalles
            return "redirect:/error"; // o a una página de error personalizada
        }

        // Añadir atributos al modelo
        model.addAttribute("cart", detalles);
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);

        return "cliente/resumenorden";
    }

    @GetMapping("/credit-card")
    @PreAuthorize("hasRole('CLIENTE')")
    public String showCreditCardPaymentPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);

        model.addAttribute("cart", detalles);
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);
        return "cliente/pago-tarjeta";
    }

    @PostMapping("/process-credit-card")
    @PreAuthorize("hasRole('CLIENTE')")
    public String processCreditCardPayment(
            @RequestParam String cardNumber,
            @RequestParam String cardName,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        // Validaciones básicas de tarjeta
        if (!validateCreditCard(cardNumber, expiryDate, cvv)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de tarjeta inválidos");
            return "redirect:/credit-card";
        }

        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);

        // Guardar pedido con método de pago de tarjeta
        Date fechaCreacion = new Date();
        pedido.setFechaCreacion(fechaCreacion);
        pedido.setNumero(pedidoService.generarNumeroPedido());
        pedido.setUsuario(usuario);
        pedido.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pedido.setEstado(EstadoPedido.PAGADO);

        pedidoService.save(pedido);

        // Guardar detalles del pedido
        for (DetallePedido dt : detalles) {
            dt.setPedido(pedido);
            dt.setEstadoPago("PAGADO");  // Add this line to set the payment status
            detallePedidoService.save(dt);
        }

        // Create PagoPedido for credit card payment
        for (DetallePedido dt : detalles) {
            PagoPedido pagoPedido = new PagoPedido();
            pagoPedido.setDetallePedido(dt);
            pagoPedido.setMontoPagado(dt.getTotal());
            pagoPedido.setVuelto(0.0); // No change for credit card
            pagoPedido.setFechaPago(new Date());
            pagoPedido.setUsuarioPago(usuario);

            // Save the PagoPedido
            pagoPedidoRepository.save(pagoPedido);
        }

        // Limpiar pedido y detalles
        pedido = new Pedido();
        detalles.clear();

        redirectAttributes.addFlashAttribute("successMessage", "Pago con tarjeta realizado exitosamente");
        return "redirect:/pedidos/mis-pedidos";
    }

    private boolean validateCreditCard(String cardNumber, String expiryDate, String cvv) {
        // Validación de número de tarjeta
        if (!isValidCardNumber(cardNumber)) {
            return false;
        }

        // Validación de fecha de expiración
        if (!isValidExpiryDate(expiryDate)) {
            return false;
        }

        // Validación de CVV
        if (!isValidCVV(cvv)) {
            return false;
        }

        return true;
    }

    private boolean isValidCardNumber(String cardNumber) {
        // Validaciones de número de tarjeta:
        // 1. Debe contener solo dígitos
        // 2. Longitud entre 13 y 19 dígitos
        // 3. Algoritmo de Luhn para validación
        if (cardNumber == null || !cardNumber.matches("\\d+")) {
            return false;
        }

        // Verificación de longitud
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }

        // Algoritmo de Luhn (validación de tarjetas)
        return validateLuhnAlgorithm(cardNumber);
    }

    private boolean validateLuhnAlgorithm(String cardNumber) {
        int sum = 0;
        boolean isEven = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEven = !isEven;
        }

        return (sum % 10 == 0);
    }

    private boolean isValidExpiryDate(String expiryDate) {
        // Validaciones de fecha de expiración:
        // 1. Formato MM/YY
        // 2. No expirada
        if (expiryDate == null || !expiryDate.matches("\\d{2}/\\d{2}")) {
            return false;
        }

        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);

            // Validar rango de mes
            if (month < 1 || month > 12) {
                return false;
            }

            // Obtener año actual y año de la tarjeta
            LocalDate currentDate = LocalDate.now();
            int currentYear = currentDate.getYear() % 100;
            int currentMonth = currentDate.getMonthValue();

            // Validar que la tarjeta no esté expirada
            if (year < currentYear
                    || (year == currentYear && month < currentMonth)) {
                return false;
            }

            // Opcional: Limitar a tarjetas no vencidas en los próximos 10 años
            if (year > currentYear + 10) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCVV(String cvv) {
        // Validaciones de CVV:
        // 1. Solo dígitos
        // 2. Longitud de 3 o 4 dígitos (dependiendo del tipo de tarjeta)
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            return false;
        }

        return true;
    }

    //Guardar la orden en EFECTIVO
    @GetMapping("/saveOrder")
    @PreAuthorize("hasRole('CLIENTE')")
    public String saveOrder(Authentication authentication, RedirectAttributes redirectAttributes) {

        // Obtener el nombre de usuario del usuario autenticado
        String username = authentication.getName();

        // Buscar el usuario por su username
        Usuario usuario = usuarioService.findByUsername(username);

        Date fechaCreacion = new Date();
        pedido.setFechaCreacion(fechaCreacion);
        pedido.setNumero(pedidoService.generarNumeroPedido());

        // Usar el usuario autenticado
        pedido.setUsuario(usuario);
        pedidoService.save(pedido);

        pedido.setMetodoPago(MetodoPago.EFECTIVO);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        //guardar detalles del pedido
        for (DetallePedido dt : detalles) {
            dt.setPedido(pedido);
            detallePedidoService.save(dt);
        }

        //limpiar, listar y orden
        pedido = new Pedido();
        detalles.clear();

        // Pasar el mensaje de éxito con el método de pago
        redirectAttributes.addFlashAttribute("successMessage", "¡Pedido realizado con éxito! Acércate a pagar el pedido en el local seleccionado!");

        // Redirigir a la página de "Mis pedidos" con un mensaje de éxito
        return "redirect:/pedidos/mis-pedidos?success=true";

    }

    @GetMapping("/yape-payment")
    @PreAuthorize("hasRole('CLIENTE')")
    public String showYapePaymentPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);

        model.addAttribute("cart", detalles);
        model.addAttribute("pedido", pedido);
        model.addAttribute("usuario", usuario);

        return "cliente/pago-yape";
    }
    
     @PostMapping("/process-yape-payment")
    @PreAuthorize("hasRole('CLIENTE')")
    public String processYapePayment(
            @RequestParam("comprobante") MultipartFile comprobante,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String username = authentication.getName();
            Usuario usuario = usuarioService.findByUsername(username);

            // Guardar la imagen del comprobante
            String fileName = saveComprobante(comprobante);

            // Guardar pedido con método de pago Yape
            Date fechaCreacion = new Date();
            pedido.setFechaCreacion(fechaCreacion);
            pedido.setNumero(pedidoService.generarNumeroPedido());
            pedido.setUsuario(usuario);
            pedido.setMetodoPago(MetodoPago.YAPE);
            pedido.setEstado(EstadoPedido.PROCESANDO);
            pedidoService.save(pedido);

            // Guardar detalles del pedido
            for (DetallePedido dt : detalles) {
                dt.setPedido(pedido);
                dt.setEstadoPago("PROCESANDO");
                detallePedidoService.save(dt);

                // Crear PagoPedido para Yape
                PagoPedido pagoPedido = new PagoPedido();
                pagoPedido.setDetallePedido(dt);
                pagoPedido.setMontoPagado(dt.getTotal());
                pagoPedido.setVuelto(0.0);
                pagoPedido.setFechaPago(new Date());
                pagoPedido.setUsuarioPago(usuario);
                pagoPedido.setImagenComprobante(fileName);
                pagoPedidoRepository.save(pagoPedido);
            }

            // Limpiar pedido y detalles
            pedido = new Pedido();
            detalles.clear();

            redirectAttributes.addFlashAttribute("successMessage", "Pago por Yape procesando. Espera 6 min por favor.");
            return "redirect:/pedidos/mis-pedidos";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pago.");
            return "redirect:/yape-payment";
        }
    }

    private String saveComprobante(MultipartFile comprobante) throws IOException {
        String uploadsDir = "./uploads/pagos/";
        Path uploadPath = Paths.get(uploadsDir);

        // Crear directorio si no existe
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre de archivo único
        String fileName = System.currentTimeMillis() + "_" + comprobante.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(comprobante.getInputStream(), filePath);

        return "/uploads/pagos/" + fileName;
    }

}
