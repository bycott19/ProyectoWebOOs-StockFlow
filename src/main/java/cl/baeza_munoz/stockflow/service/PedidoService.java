package cl.baeza_munoz.stockflow.service;

import cl.baeza_munoz.stockflow.dto.DetallePedidoDTO;
import cl.baeza_munoz.stockflow.dto.PedidoRequestDTO;
import cl.baeza_munoz.stockflow.exception.StockInsuficienteException;
import cl.baeza_munoz.stockflow.model.Cliente;
import cl.baeza_munoz.stockflow.model.DetallePedido;
import cl.baeza_munoz.stockflow.model.Pedido;
import cl.baeza_munoz.stockflow.model.Producto;
import cl.baeza_munoz.stockflow.repository.ClienteRepository;
import cl.baeza_munoz.stockflow.repository.PedidoRepository;
import cl.baeza_munoz.stockflow.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Método para listar los pedidos de un cliente específico
    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    // Método principal: Procesamiento de la compra con rollback automático en caso de error
    @Transactional
    public Pedido procesarPedido(PedidoRequestDTO requestDTO) {
        
        // 1. Buscar al cliente
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + requestDTO.getClienteId()));

        // 2. Crear el encabezado del pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setEstado("PENDIENTE");

        BigDecimal totalPedido = BigDecimal.ZERO;

        // 3. Procesar cada ítem del pedido
        for (DetallePedidoDTO detalleDTO : requestDTO.getDetalles()) {
            
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId()));

            // VALIDACIÓN CRÍTICA: ¿Hay stock suficiente?
            if (producto.getStockActual() < detalleDTO.getCantidad()) {
                // Al lanzar esta excepción, @Transactional cancela TODO y no guarda nada en la BD
                throw new StockInsuficienteException("Stock insuficiente para el producto: " + producto.getNombre() + 
                        ". Stock actual: " + producto.getStockActual() + ", Solicitado: " + detalleDTO.getCantidad());
            }

            // Descontar inventario
            producto.setStockActual(producto.getStockActual() - detalleDTO.getCantidad());
            // Guardar la actualización de stock en la BD
            productoRepository.save(producto);

            // Calcular subtotal de esta línea
            BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(detalleDTO.getCantidad()));
            totalPedido = totalPedido.add(subtotal);

            // Armar el detalle del pedido
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(nuevoPedido);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Guardamos el precio histórico
            detalle.setSubtotal(subtotal);

            // Agregar el detalle a la lista del pedido
            nuevoPedido.getDetalles().add(detalle);
        }

        // 4. Asignar el total final y guardar el pedido completo
        nuevoPedido.setTotal(totalPedido);
        
        // Al guardar el pedido, JPA también guarda todos sus detalles gracias a CascadeType.ALL
        return pedidoRepository.save(nuevoPedido);
    }
}