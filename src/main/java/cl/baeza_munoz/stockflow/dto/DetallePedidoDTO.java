package cl.baeza_munoz.stockflow.dto;

import lombok.Data;

@Data
public class DetallePedidoDTO {
    private Long productoId;
    private Integer cantidad;
}