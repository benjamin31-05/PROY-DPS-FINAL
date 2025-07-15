package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.service.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public String misPedidos(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);
        List<Pedido> pedidos = pedidoService.findByUsuario(usuario);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("usuario", usuario);
        return "cliente/pedidos";
    }

    @GetMapping("/generar-boleta/{pedidoId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public void generarBoletaPDF(@PathVariable Integer pedidoId, HttpServletResponse response) throws IOException {
        Pedido pedido = pedidoService.findById(pedidoId);

        // Configurar la respuesta HTTP para la descarga del PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=boleta_pedido_" + pedido.getNumero() + ".pdf");

        // Usar el servicio de generación de PDF
        pdfGeneratorService.generarComprobantePDF(pedido, response);
    }

    @GetMapping("/detalle/{numero}")
    @PreAuthorize("hasRole('CLIENTE')")
    public String detallePedido(@PathVariable String numero,
            Model model,
            Authentication authentication) {
        // Verificar que el pedido pertenece al usuario autenticado
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);

        Pedido pedido = pedidoService.findByNumero(numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Verificar que el pedido pertenece al usuario
        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("pedido", pedido);
        return "pedidos/detallePedido";
    }
}
