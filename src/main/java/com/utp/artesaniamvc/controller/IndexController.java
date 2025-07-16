package com.utp.artesaniamvc.controller;

import com.utp.artesaniamvc.model.*;
import com.utp.artesaniamvc.repository.*;
import com.utp.artesaniamvc.service.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

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
    private EspecificacionProductoService especificacionService;

    //Para almacenar los detalles del Pedido
    List<DetallePedido> detalles = new ArrayList<DetallePedido>();

    //Detalles del pedido    
    Pedido pedido = new Pedido();

    /* PAGINA INDEX DE LOS PRODUCTOS  */
    @GetMapping("/index")
    public String index(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer subcategoriaId,
            @RequestParam(required = false) Integer categoriaId) {

        Pageable pageable = PageRequest.of(page, 6);
        Page<Producto> productoPage;

        // Obtener los últimos productos disponibles
        List<Producto> latestProductos = productoService.getLatestProductos().stream()
                .filter(Producto::isDisponiblePorTemporada)
                .collect(Collectors.toList());

        List<Subcategoria> subcategorias = subcategoriaService.getAllSubcategorias();
        List<Subcategoria> subcategoriasDeCategoria = new ArrayList<>();

        if (subcategoriaId != null) {
            productoPage = productoService.findBySubcategoriaIdDisponibles(subcategoriaId, pageable);

            if (!productoPage.getContent().isEmpty()) {
                Producto primerProducto = productoPage.getContent().get(0);
                categoriaId = primerProducto.getSubcategoria().getCategoria().getId();
                subcategoriasDeCategoria = subcategoriaService.findByCategoriaId(categoriaId);
            }
        } else if (categoriaId != null) {
            subcategoriasDeCategoria = subcategoriaService.findByCategoriaId(categoriaId);
            productoPage = productoService.findByCategoriaIdDisponibles(categoriaId, pageable);
        } else {
            productoPage = productoService.getAllProductosDisponibles(pageable);
        }

        model.addAttribute("latestProductos", latestProductos);
        model.addAttribute("productos", productoPage);
        model.addAttribute("subcategorias", subcategorias);
        model.addAttribute("subcategoriasDeCategoria", subcategoriasDeCategoria);
        model.addAttribute("categoriaId", categoriaId);

        return "productos/index6";

    }

    @GetMapping("/asuntos")
    public String listarAsuntos(Model model) {
        List<Asunto> asuntos = asuntoService.getAllAsuntos();
        model.addAttribute("asuntos", asuntos);
        return "asuntos";  // Vista que muestra la lista de asuntos
    }

    @GetMapping("/contacto")
    @PreAuthorize("hasRole('CLIENTE')")
    public String mostrarFormularioContacto(Model model, Authentication authentication) {
        String username = authentication.getName();
        Usuario usuario = usuarioService.findByUsername(username);
        model.addAttribute("usuario", usuario);
        model.addAttribute("contacto", new Contacto());
        model.addAttribute("asuntos", asuntoService.getAllAsuntos());  // Lista de asuntos para el formulario
        return "cliente/contacto";  // Vista del formulario de contacto
    }

    @PostMapping("/contactoRegister")
    @PreAuthorize("hasRole('CLIENTE')")
    public String registrarMensaje(@RequestParam String nombre,
            @RequestParam String email,
            @RequestParam Integer asuntoId,
            @RequestParam String mensaje,
            Model model) {
        // Crear el objeto Contacto
        Contacto contacto = new Contacto();
        contacto.setNombre(nombre);
        contacto.setEmail(email);
        contacto.setMensaje(mensaje);

        // Guardar el contacto en la base de datos con el asunto correspondiente
        contactoService.saveContacto(contacto, asuntoId);

        // Pasar los datos del cliente al modelo para la vista de agradecimiento
        model.addAttribute("nombre", nombre);
        model.addAttribute("email", email);

        // Redirigir a la página de agradecimiento
        return "cliente/agradecimiento";
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "cliente/nosotros"; //VISTA NOSOTROS
    }

    @GetMapping("/informacion")
    public String informacion() {
        return "cliente/informacion"; //VISTA INFORMACION
    }

    @PostMapping("/search")
    public String searchProduct(@RequestParam String nombre, Model model) {

        List<Producto> productos = productoService.findAll().stream().filter(p -> p.getNombreProducto().contains(nombre))
                .collect(Collectors.toList());
        model.addAttribute("productos", productos);

        return "cliente/homeCliente";
    }

    @GetMapping("/productoHome/{id}")
    public String productoHome(@PathVariable Integer id, Model model) {
        Producto producto = new Producto();
        Optional<Producto> productoOptional = productoService.get(id);
        producto = productoOptional.get();
        // Cargar especificaciones si existen
        EspecificacionProducto especificacion = especificacionService.obtenerEspecificacionPorProducto(id);
        producto.setEspecificacion(especificacion);
        // Explicitly add locales to the model
        model.addAttribute("locales", localService.findAll());
        model.addAttribute("producto", producto);

        return "cliente/caracteristicas";
    }
}
