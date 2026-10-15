package cl.baeza_munoz.stockflow.controller;

import cl.baeza_munoz.stockflow.model.Producto;
import cl.baeza_munoz.stockflow.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoRepository.save(producto);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // Endpoint clave para el requerimiento de negocio de alertas de inventario
    @GetMapping("/alertas-stock")
    public ResponseEntity<List<Producto>> obtenerAlertasDeStock() {
        List<Producto> alertas = productoRepository.findProductosConStockBajo();
        return ResponseEntity.ok(alertas);
    }
}