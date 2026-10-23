package cl.baeza_munoz.stockflow;

import cl.baeza_munoz.stockflow.dto.DetallePedidoDTO;
import cl.baeza_munoz.stockflow.dto.PedidoRequestDTO;
import cl.baeza_munoz.stockflow.exception.StockInsuficienteException;
import cl.baeza_munoz.stockflow.model.Cliente;
import cl.baeza_munoz.stockflow.model.Pedido;
import cl.baeza_munoz.stockflow.model.Producto;
import cl.baeza_munoz.stockflow.repository.ClienteRepository;
import cl.baeza_munoz.stockflow.repository.ProductoRepository;
import cl.baeza_munoz.stockflow.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Cliente clienteTest;
    private Producto productoTest;

    @BeforeEach
    void setUp() {
        clienteTest = new Cliente("Test User", "99999999-9", "test@stockflow.cl", "+56911112222");
        clienteTest = clienteRepository.save(clienteTest);

        productoTest = new Producto("TEST-001", "Teclado Test", "Descripción", "Periféricos",
                new BigDecimal("50000.00"), 10, 2);
        productoTest = productoRepository.save(productoTest);
    }

    @Test
    void procesarPedido_ConStockSuficiente_DebeCrearPedidoYDescontarStock() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(clienteTest.getId());

        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(productoTest.getId());
        detalle.setCantidad(3);
        request.setDetalles(List.of(detalle));

        Pedido pedidoCreado = pedidoService.procesarPedido(request);

        assertNotNull(pedidoCreado.getId());
        assertEquals("PENDIENTE", pedidoCreado.getEstado());
        assertEquals(new BigDecimal("150000.00"), pedidoCreado.getTotal());
        assertEquals(1, pedidoCreado.getDetalles().size());

        // Verificar descuento en base de datos
        Producto prodActualizado = productoRepository.findById(productoTest.getId()).orElseThrow();
        assertEquals(7, prodActualizado.getStockActual());
    }

    @Test
    void procesarPedido_ConStockInsuficiente_DebeLanzarExcepcionYNoDescontar() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(clienteTest.getId());

        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(productoTest.getId());
        detalle.setCantidad(15); // Hay 10 disponibles
        request.setDetalles(List.of(detalle));

        assertThrows(StockInsuficienteException.class, () -> {
            pedidoService.procesarPedido(request);
        });

        // El stock debe permanecer intacto en 10
        Producto prodActualizado = productoRepository.findById(productoTest.getId()).orElseThrow();
        assertEquals(10, prodActualizado.getStockActual());
    }

    @Test
    void actualizarEstado_ACancelado_DebeRestituirStock() {
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(clienteTest.getId());

        DetallePedidoDTO detalle = new DetallePedidoDTO();
        detalle.setProductoId(productoTest.getId());
        detalle.setCantidad(4);
        request.setDetalles(List.of(detalle));

        Pedido pedido = pedidoService.procesarPedido(request);

        // Stock bajó a 6
        Producto prodPostPedido = productoRepository.findById(productoTest.getId()).orElseThrow();
        assertEquals(6, prodPostPedido.getStockActual());

        // Cancelar pedido
        pedidoService.actualizarEstado(pedido.getId(), "CANCELADO");

        // Stock debe regresar a 10
        Producto prodPostCancelacion = productoRepository.findById(productoTest.getId()).orElseThrow();
        assertEquals(10, prodPostCancelacion.getStockActual());
    }
}