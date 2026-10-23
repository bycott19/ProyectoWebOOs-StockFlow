package cl.baeza_munoz.stockflow.controller;

import cl.baeza_munoz.stockflow.dto.DetallePedidoDTO;
import cl.baeza_munoz.stockflow.dto.PedidoRequestDTO;
import cl.baeza_munoz.stockflow.exception.StockInsuficienteException;
import cl.baeza_munoz.stockflow.model.Cliente;
import cl.baeza_munoz.stockflow.model.Pedido;
import cl.baeza_munoz.stockflow.model.Producto;
import cl.baeza_munoz.stockflow.repository.PedidoRepository;
import cl.baeza_munoz.stockflow.service.ClienteService;
import cl.baeza_munoz.stockflow.service.PedidoService;
import cl.baeza_munoz.stockflow.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    // ==========================================
    // 1. INICIO / DASHBOARD
    // ==========================================
    @GetMapping("/")
    public String inicio(Model model) {
        List<Producto> productos = productoService.listarTodos();
        List<Producto> stockBajo = productoService.obtenerProductosStockBajo();

        model.addAttribute("productosCount", productos.size());
        model.addAttribute("clientesCount", clienteService.listarTodos().size());
        model.addAttribute("pedidosPendientesCount", pedidoRepository.countByEstadoIgnoreCase("PENDIENTE"));
        model.addAttribute("stockBajoCount", stockBajo.size());
        model.addAttribute("ultimosPedidos", pedidoRepository.findTop5ByOrderByFechaCreacionDesc());
        model.addAttribute("productosBajoStock", stockBajo);

        return "index";
    }

    // ==========================================
    // 2. PRODUCTOS
    // ==========================================
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

    // ==========================================
    // 3. ALERTAS DE STOCK BAJO (RF6) Y EXPORTACIÓN CSV
    // ==========================================
    @GetMapping("/stock-bajo")
    public String stockBajo(Model model) {
        List<Producto> bajo = productoService.obtenerProductosStockBajo();
        model.addAttribute("productos", bajo);
        model.addAttribute("totalBajoStock", bajo.size());
        return "stock-bajo";
    }

    @GetMapping(value = "/stock-bajo/exportar-csv", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportarStockBajoCsv() {
        List<Producto> bajo = productoService.obtenerProductosStockBajo();
        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 para apertura inmediata en Excel sin errores de tildes
        csv.append('\uFEFF');
        csv.append("SKU;Producto;Categoría;Precio CLP;Stock Actual;Stock Mínimo;Unidades Faltantes;Urgencia\n");

        for (Producto p : bajo) {
            int faltantes = Math.max(0, p.getStockMinimo() - p.getStockActual());
            String urgencia = p.getStockActual() == 0 ? "Crítica" : (p.getStockActual() <= p.getStockMinimo() / 2 ? "Alta" : "Media");
            csv.append(escapeCsv(p.getSku())).append(";")
               .append(escapeCsv(p.getNombre())).append(";")
               .append(escapeCsv(p.getCategoria() != null ? p.getCategoria() : "-")).append(";")
               .append(p.getPrecio()).append(";")
               .append(p.getStockActual()).append(";")
               .append(p.getStockMinimo()).append(";")
               .append(faltantes).append(";")
               .append(urgencia).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-stock-bajo.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(bytes);
    }

    // ==========================================
    // 4. CLIENTES (CON VALIDACIÓN DE RUT)
    // ==========================================
    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            if (cliente.getId() != null) {
                clienteService.actualizarCliente(cliente.getId(), cliente);
                redirectAttributes.addFlashAttribute("exito", "Cliente actualizado correctamente.");
            } else {
                clienteService.crearCliente(cliente);
                redirectAttributes.addFlashAttribute("exito", "Cliente registrado exitosamente.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("exito", "Cliente eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // ==========================================
    // 5. NUEVO PEDIDO
    // ==========================================
    @GetMapping("/pedidos/nuevo")
    public String nuevoPedidoForm(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("productos", productoService.listarTodos());
        return "nuevo-pedido";
    }

    @PostMapping("/pedidos/guardar")
    public String guardarPedido(@RequestParam Long clienteId,
                                @RequestParam(value = "productoId", required = false) List<Long> productoIds,
                                @RequestParam(value = "cantidad", required = false) List<Integer> cantidades,
                                RedirectAttributes redirectAttributes) {
        try {
            if (productoIds == null || productoIds.isEmpty() || cantidades == null || cantidades.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe agregar al menos un producto al pedido.");
                return "redirect:/pedidos/nuevo";
            }

            PedidoRequestDTO requestDTO = new PedidoRequestDTO();
            requestDTO.setClienteId(clienteId);

            List<DetallePedidoDTO> detalles = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                DetallePedidoDTO det = new DetallePedidoDTO();
                det.setProductoId(productoIds.get(i));
                det.setCantidad(cantidades.get(i));
                detalles.add(det);
            }
            requestDTO.setDetalles(detalles);

            Pedido nuevoPedido = pedidoService.procesarPedido(requestDTO);
            redirectAttributes.addFlashAttribute("exito", "¡Pedido #" + nuevoPedido.getId() + " registrado exitosamente!");
            return "redirect:/pedidos/" + nuevoPedido.getId();

        } catch (StockInsuficienteException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/pedidos/nuevo";
        }
    }

    // ==========================================
    // 6. HISTORIAL DE PEDIDOS Y EXPORTACIÓN CSV
    // ==========================================
    @GetMapping("/pedidos")
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoRepository.findAllByOrderByFechaCreacionDesc());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("clienteSeleccionado", null);
        return "pedidos";
    }

    @GetMapping("/pedidos/cliente/{clienteId}")
    public String listarPedidosPorCliente(@PathVariable Long clienteId, Model model) {
        Cliente cliente = clienteService.obtenerPorId(clienteId);
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId);

        model.addAttribute("cliente", cliente);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("clienteSeleccionado", cliente);
        return "pedidos";
    }

    @GetMapping(value = "/pedidos/exportar-csv", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportarTodosPedidosCsv() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        return generarCsvPedidos(pedidos, "reporte-pedidos.csv");
    }

    @GetMapping(value = "/pedidos/cliente/{clienteId}/exportar-csv", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportarPedidosClienteCsv(@PathVariable Long clienteId) {
        Cliente cliente = clienteService.obtenerPorId(clienteId);
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId);
        String filename = "pedidos-" + cliente.getNombre().toLowerCase().replaceAll("[^a-z0-9]", "-") + ".csv";
        return generarCsvPedidos(pedidos, filename);
    }

    // ==========================================
    // 7. DETALLE DE PEDIDO Y CAMBIO DE ESTADO
    // ==========================================
    @GetMapping("/pedidos/{id}")
    public String detallePedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        model.addAttribute("pedido", pedido);
        return "pedido-detalle";
    }

    @PostMapping("/pedidos/{id}/estado")
    public String actualizarEstadoPedido(@PathVariable Long id,
                                         @RequestParam String nuevoEstado,
                                         RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = pedidoService.actualizarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("exito", "Estado del pedido #" + id + " actualizado a " + pedido.getEstado());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado: " + e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    // ==========================================
    // MÉTODOS AUXILIARES CSV
    // ==========================================
    private ResponseEntity<byte[]> generarCsvPedidos(List<Pedido> pedidos, String filename) {
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF'); // BOM UTF-8
        csv.append("N° Pedido;Cliente;RUT;Email;Fecha Registro;Total CLP;Estado;Cantidad Ítems\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Pedido p : pedidos) {
            csv.append("#").append(p.getId()).append(";")
               .append(escapeCsv(p.getCliente().getNombre())).append(";")
               .append(escapeCsv(p.getCliente().getRut())).append(";")
               .append(escapeCsv(p.getCliente().getEmail())).append(";")
               .append(p.getFechaCreacion() != null ? p.getFechaCreacion().format(dtf) : "-").append(";")
               .append(p.getTotal()).append(";")
               .append(p.getEstado()).append(";")
               .append(p.getDetalles() != null ? p.getDetalles().size() : 0).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(bytes);
    }

    private String escapeCsv(String valor) {
        if (valor == null) return "";
        return valor.replace(";", ",");
    }
}
