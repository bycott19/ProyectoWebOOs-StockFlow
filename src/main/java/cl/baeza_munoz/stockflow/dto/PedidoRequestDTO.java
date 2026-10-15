package cl.baeza_munoz.stockflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class PedidoRequestDTO {
    private Long clienteId;
    private List<DetallePedidoDTO> detalles;
}