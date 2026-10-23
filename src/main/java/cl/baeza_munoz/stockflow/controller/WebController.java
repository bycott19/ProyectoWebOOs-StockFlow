package cl.baeza_munoz.stockflow.controller;

import cl.baeza_munoz.stockflow.model.Producto;
import cl.baeza_munoz.stockflow.repository.ClienteRepository;
import cl.baeza_munoz.stockflow.repository.PedidoRepository;
import cl.baeza_munoz.stockflow.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // 1. Vista de Inicio / Dashboard (Parte de Bairon)
    @GetMapping("/")
    public String inicio(Model model) {
        List<Producto> productos = productoService.listarTodos();
        List<Producto> stockBajo = productoService.obtenerProductosStockBajo();

        model.addAttribute("productosCount", productos.size());
        model.addAttribute("clientesCount", clienteRepository.count());
        model.addAttribute("pedidosPendientesCount", pedidoRepository.findAll().stream()
                .filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstado())).count());
        model.addAttribute("stockBajoCount", stockBajo.size());
        model.addAttribute("ultimosPedidos", pedidoRepository.findAll());
        model.addAttribute("productosBajoStock", stockBajo);

        return "index";
    }

    // 2. Vista de Catálogo de Productos (Parte de Bairon)
    @GetMapping("/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "productos";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        if (producto.getId() != null) {
            productoService.actualizarProducto(producto.getId(), producto);
        } else {
            productoService.crearProducto(producto);
        }
        return "redirect:/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    public String eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return "redirect:/productos";
    }

    // 3. Vista de Alertas de Stock Bajo (Parte de Bairon)
    @GetMapping("/stock-bajo")
    public String stockBajo(Model model) {
        List<Producto> bajo = productoService.obtenerProductosStockBajo();
        model.addAttribute("productos", bajo);
        model.addAttribute("totalBajoStock", bajo.size());
        return "stock-bajo";
    }
}