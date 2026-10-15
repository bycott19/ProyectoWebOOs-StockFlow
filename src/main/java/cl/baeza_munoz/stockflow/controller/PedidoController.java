package cl.baeza_munoz.stockflow.controller;

import cl.baeza_munoz.stockflow.dto.PedidoRequestDTO;
import cl.baeza_munoz.stockflow.model.Pedido;
import cl.baeza_munoz.stockflow.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Procesamiento transaccional de un nuevo pedido
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody PedidoRequestDTO requestDTO) {
        Pedido nuevoPedido = pedidoService.procesarPedido(requestDTO);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    // Listar historial de un cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorCliente(@PathVariable Long clienteId) {
        List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(clienteId);
        return ResponseEntity.ok(pedidos);
    }
}